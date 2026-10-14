/*******************************************************************************
 * Copyright (c) 2026 Contributors to the LSP4IJ project.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.redhat.devtools.lsp4ij.features.diagnostics;

import com.intellij.openapi.Disposable;
import com.redhat.devtools.lsp4ij.JSONUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiPredicate;

/**
 * One workspace diagnostic pull loop per provider, scoped to one server connection.
 * All state and report delivery are serialized off the JSON-RPC reader thread.
 * A completed request is pulled again; a streaming server may keep it pending.
 */
@ApiStatus.Internal
public final class WorkspaceDiagnosticManager implements Disposable {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceDiagnosticManager.class);
    private static final long RETRIGGER_DELAY_MS = 1000;

    private final WorkspaceService server;
    private final ScheduledExecutorService executor;
    private final BiPredicate<String, WorkspaceDocumentDiagnosticReport> reports;
    private final Map<String, Provider> providers = new HashMap<>();
    private volatile boolean disposed;

    /** The executor must serialize tasks and is owned by this manager. */
    public WorkspaceDiagnosticManager(WorkspaceService server, ScheduledExecutorService executor,
                                      BiPredicate<String, WorkspaceDocumentDiagnosticReport> reports) {
        this.server = server;
        this.executor = executor;
        this.reports = reports;
    }

    public void updateProviders(Set<String> identifiers) {
        var copy = new HashSet<>(identifiers); // null denotes the default diagnostic provider
        execute(() -> {
            var iterator = providers.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (!copy.contains(entry.getKey())) {
                    cancel(entry.getValue());
                    iterator.remove();
                }
            }
            for (String identifier : copy) {
                if (!providers.containsKey(identifier)) {
                    var provider = new Provider(identifier);
                    providers.put(identifier, provider);
                    pull(provider);
                }
            }
        });
    }

    public void refresh() {
        execute(() -> {
            for (var provider : providers.values()) {
                cancel(provider);
                pull(provider);
            }
        });
    }

    public void notifyProgress(ProgressParams params) {
        if (params.getToken() == null || params.getValue() == null || !params.getValue().isRight()) {
            return;
        }
        execute(() -> {
            for (var provider : providers.values()) {
                if (params.getToken().isLeft() && Objects.equals(provider.token, params.getToken().getLeft())) {
                    var gson = JSONUtils.getLsp4jGson();
                    var report = gson.fromJson(gson.toJsonTree(params.getValue().getRight()), WorkspaceDiagnosticReport.class);
                    accept(provider, report);
                    break;
                }
            }
        });
    }

    private void pull(Provider provider) {
        if (disposed) {
            return;
        }
        provider.scheduled = null;
        String token = UUID.randomUUID().toString();
        provider.token = token;
        var params = new WorkspaceDiagnosticParams(provider.resultIds.entrySet().stream()
                .map(entry -> new PreviousResultId(entry.getKey(), entry.getValue())).toList());
        params.setIdentifier(provider.identifier);
        params.setPartialResultToken(token);
        try {
            provider.request = server.diagnostic(params);
            provider.request.whenComplete((report, error) -> execute(() -> {
                if (!Objects.equals(provider.token, token)) {
                    return; // a refreshed/unregistered request completed late
                }
                provider.token = null;
                provider.request = null;
                if (error == null) {
                    accept(provider, report);
                    schedule(provider);
                } else if (shouldRetrigger(error)) {
                    schedule(provider);
                }
            }));
        } catch (RuntimeException error) {
            provider.token = null;
            provider.request = null;
            LOGGER.warn("Unable to request workspace diagnostics", error);
        }
    }

    private void accept(Provider provider, WorkspaceDiagnosticReport report) {
        if (report == null || report.getItems() == null) {
            return;
        }
        for (var item : report.getItems()) {
            if (disposed) {
                return;
            }
            String uri = item.isLeft() ? item.getLeft().getUri() : item.getRight().getUri();
            String resultId = item.isLeft() ? item.getLeft().getResultId() : item.getRight().getResultId();
            if (item.isRight() && !provider.resultIds.containsKey(uri)) {
                continue; // no cached full report to retain
            }
            if (reports.test(provider.identifier, item) && resultId != null) {
                provider.resultIds.put(uri, resultId);
            } else {
                provider.resultIds.remove(uri);
            }
        }
    }

    private boolean shouldRetrigger(Throwable error) {
        while (error instanceof CompletionException && error.getCause() != null) {
            error = error.getCause();
        }
        if (error instanceof ResponseErrorException response
                && response.getResponseError().getCode() == ResponseErrorCode.ServerCancelled.getValue()) {
            var data = response.getResponseError().getData();
            if (data == null) {
                return true;
            }
            var gson = JSONUtils.getLsp4jGson();
            var cancellation = gson.fromJson(gson.toJsonTree(data), DiagnosticServerCancellationData.class);
            return cancellation == null || cancellation.isRetriggerRequest();
        }
        if (!(error instanceof CancellationException)) {
            // Do not repeatedly hammer servers which advertise but fail this method.
            // A later refresh or registration can restart the loop.
            LOGGER.warn("Workspace diagnostics stopped", error);
        }
        return false;
    }

    private void schedule(Provider provider) {
        if (!disposed) {
            provider.scheduled = executor.schedule(() -> pull(provider), RETRIGGER_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void cancel(Provider provider) {
        provider.token = null;
        if (provider.request != null) {
            provider.request.cancel(true);
            provider.request = null;
        }
        if (provider.scheduled != null) {
            provider.scheduled.cancel(false);
            provider.scheduled = null;
        }
    }

    private void execute(Runnable task) {
        if (disposed) {
            return;
        }
        try {
            executor.execute(() -> {
                if (!disposed) {
                    task.run();
                }
            });
        } catch (RejectedExecutionException e) {
            if (!disposed) {
                throw e;
            }
        }
    }

    @Override
    public synchronized void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        // Do not wait for a report consumer/read action while stopping a server.
        executor.execute(() -> {
            providers.values().forEach(this::cancel);
            providers.clear();
        });
        executor.shutdown();
    }

    private static final class Provider {
        final String identifier;
        final Map<String, String> resultIds = new HashMap<>();
        String token;
        CompletableFuture<WorkspaceDiagnosticReport> request;
        ScheduledFuture<?> scheduled;

        Provider(String identifier) {
            this.identifier = identifier;
        }
    }
}
