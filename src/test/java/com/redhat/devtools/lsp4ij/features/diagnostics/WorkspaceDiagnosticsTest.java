/*******************************************************************************
 * Copyright (c) 2026 Contributors to the LSP4IJ project.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.redhat.devtools.lsp4ij.features.diagnostics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.redhat.devtools.lsp4ij.*;
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl;
import com.redhat.devtools.lsp4ij.client.features.LSPClientFeatures;
import com.redhat.devtools.lsp4ij.fixtures.LSPCodeInsightTestFixture;
import com.redhat.devtools.lsp4ij.fixtures.LSPTestFixtureFactory;
import com.redhat.devtools.lsp4ij.mock.MockLanguageServerDefinition;
import com.redhat.devtools.lsp4ij.templates.ServerMappingSettings;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Real JSON-RPC and IntelliJ diagnostic-cache integration, independent of any Lua server. */
public class WorkspaceDiagnosticsTest extends UsefulTestCase {
    private LSPCodeInsightTestFixture fixture;
    private ScopedServer definition;
    private LanguageServerWrapper wrapper;
    private final BlockingQueue<Request> requests = new LinkedBlockingQueue<>();

    @Override protected void setUp() throws Exception {
        super.setUp();
        var builder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getName());
        fixture = LSPTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(builder.getFixture());
        fixture.setUp();
        definition = new ScopedServer();
        definition.getServer().getWorkspaceService().setDiagnosticHandler(params -> {
            var future = new CompletableFuture<WorkspaceDiagnosticReport>();
            requests.add(new Request(params, future));
            return future;
        });
        LanguageServersRegistry.getInstance().addServerDefinition(fixture.getProject(), definition,
                List.of(ServerMappingSettings.createFileNamePatternsMappingSettings(List.of("*.workspace"), null)));
    }

    @Override protected void tearDown() throws Exception {
        try {
            LanguageServersRegistry.getInstance().removeServerDefinition(fixture.getProject(), definition);
            fixture.tearDown();
        } finally {
            super.tearDown();
        }
    }

    private void start(boolean workspace) throws Exception {
        var options = new DiagnosticRegistrationOptions();
        options.setIdentifier("test");
        options.setWorkspaceDiagnostics(workspace);
        definition.getServer().getInitializeResult().getCapabilities().setDiagnosticProvider(options);
        wrapper = LanguageServerManager.getInstance(fixture.getProject()).getLanguageServer(definition.getId())
                .get(10, TimeUnit.SECONDS).getServerWrapper();
    }

    public void testClosedFilesStreamAndClearWithoutTouchingOtherModules() throws Exception {
        var owned = fixture.addFileToProject("owned.workspace", "value").getVirtualFile();
        var foreign = fixture.addFileToProject("foreign.workspace", "value").getVirtualFile();
        start(true);
        var request = next();
        stream(request, report(owned, "old", null), report(foreign, "foreign", null));
        await(() -> wrapper.getClosedDocument(LSPIJUtils.toUri(owned), false) != null);
        await(() -> wrapper.getClosedDocument(LSPIJUtils.toUri(owned), false).getDiagnostics().size() == 1);
        assertNull(wrapper.getClosedDocument(LSPIJUtils.toUri(foreign), false));
        stream(request, report(owned, null, null));
        await(() -> wrapper.getClosedDocument(LSPIJUtils.toUri(owned), false).getDiagnostics().isEmpty());
    }

    public void testWorkspaceResultsRefreshOpenDocumentWithoutReplacingDocumentDiagnostics() throws Exception {
        start(true);
        definition.getServer().setPullDiagnostics(new DocumentDiagnosticReport(
                new RelatedFullDocumentDiagnosticReport(List.of(diagnostic("document")))));
        var file = fixture.configureByText("owned.workspace", "value").getVirtualFile();
        LanguageServiceAccessor.getInstance(fixture.getProject()).getLanguageServers(fixture.getFile(), null, null)
                .get(10, TimeUnit.SECONDS);
        await(() -> wrapper.getOpenedDocument(LSPIJUtils.toUri(file)) != null
                && wrapper.getOpenedDocument(LSPIJUtils.toUri(file)).getDiagnostics().size() == 1);
        var request = next();
        definition.getServer().setPullDiagnostics(new DocumentDiagnosticReport(
                new RelatedFullDocumentDiagnosticReport(List.of(diagnostic("updated document")))));
        stream(request, report(file, "workspace", 1));
        await(() -> wrapper.getOpenedDocument(LSPIJUtils.toUri(file)).getDiagnostics().stream()
                .anyMatch(d -> "updated document".equals(d.getMessage().getLeft())));
        assertEquals(1, wrapper.getOpenedDocument(LSPIJUtils.toUri(file)).getDiagnostics().size());
    }

    public void testOlderWorkspaceVersionCannotRefreshCurrentDocument() throws Exception {
        var count = new java.util.concurrent.atomic.AtomicInteger();
        definition.getServer().setTextDocumentService(new com.redhat.devtools.lsp4ij.mock.MockTextDocumentService(
                definition.getServer()::buildMaybeDelayedFuture) {
            @Override public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
                count.incrementAndGet();
                return CompletableFuture.completedFuture(new DocumentDiagnosticReport(
                        new RelatedFullDocumentDiagnosticReport(List.of(WorkspaceDiagnosticsTest.diagnostic("current")))));
            }
        });
        start(true);
        var file = fixture.configureByText("owned.workspace", "value").getVirtualFile();
        LanguageServiceAccessor.getInstance(fixture.getProject()).getLanguageServers(fixture.getFile(), null, null)
                .get(10, TimeUnit.SECONDS);
        await(() -> wrapper.getOpenedDocument(LSPIJUtils.toUri(file)) != null
                && wrapper.getOpenedDocument(LSPIJUtils.toUri(file)).getDiagnostics().size() == 1);
        int before = count.get();
        var request = next();
        stream(request, report(file, "stale", 0));
        request.future.complete(new WorkspaceDiagnosticReport(List.of()));
        next(); // the previous report has been consumed before the next request
        assertEquals(before, count.get());
        assertEquals("current", wrapper.getOpenedDocument(LSPIJUtils.toUri(file)).getDiagnostics().iterator().next().getMessage().getLeft());
    }

    public void testDynamicRegistrationStartsAndUnregistrationCancels() throws Exception {
        start(false);
        assertNull(requests.poll(150, TimeUnit.MILLISECONDS));
        var options = JSONUtils.getLsp4jGson().fromJson(
                "{\"identifier\":\"dynamic\",\"workspaceDiagnostics\":true,\"interFileDependencies\":true}",
                com.google.gson.JsonObject.class);
        definition.client.registerCapability(new RegistrationParams(List.of(
                new Registration("dynamic", "textDocument/diagnostic", options)))).get(5, TimeUnit.SECONDS);
        var request = next();
        assertEquals("dynamic", request.params.getIdentifier());
        definition.client.unregisterCapability(new UnregistrationParams(List.of(
                new Unregistration("dynamic", "textDocument/diagnostic")))).get(5, TimeUnit.SECONDS);
        await(request.future::isCancelled);
        assertNull(requests.poll(150, TimeUnit.MILLISECONDS));
    }

    public void testRefreshAndStopCancelRequestsThroughTransport() throws Exception {
        start(true);
        var first = next();
        definition.getServer().getRemoteProxies().get(0).refreshDiagnostics().get(5, TimeUnit.SECONDS);
        var second = next();
        await(first.future::isCancelled);
        wrapper.stop().get(10, TimeUnit.SECONDS);
        await(second.future::isCancelled);
    }

    private Request next() throws InterruptedException {
        var request = requests.poll(10, TimeUnit.SECONDS);
        assertNotNull("Expected workspace diagnostic request", request);
        return request;
    }

    private void stream(Request request, WorkspaceDocumentDiagnosticReport... reports) {
        definition.getServer().getRemoteProxies().get(0).notifyProgress(new ProgressParams(
                request.params.getPartialResultToken(), Either.forRight(new WorkspaceDiagnosticReport(List.of(reports)))));
    }

    private static Diagnostic diagnostic(String message) {
        return new Diagnostic(new Range(new Position(0, 0), new Position(0, 1)), message, DiagnosticSeverity.Warning, "test");
    }

    private static WorkspaceDocumentDiagnosticReport report(VirtualFile file, String message, Integer version) {
        var report = new WorkspaceFullDocumentDiagnosticReport(
                message == null ? List.of() : List.of(diagnostic(message)), LSPIJUtils.toUri(file).toString(), version);
        return new WorkspaceDocumentDiagnosticReport(report);
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean()) {
            assertTrue("Timed out waiting for diagnostic update", System.nanoTime() < deadline);
            Thread.sleep(10);
        }
    }

    private record Request(WorkspaceDiagnosticParams params, CompletableFuture<WorkspaceDiagnosticReport> future) { }

    private static class ScopedServer extends MockLanguageServerDefinition {
        LanguageClientImpl client;
        ScopedServer() { super("workspace-diagnostics", false); }
        @Override public @NotNull LanguageClientImpl createLanguageClient(@NotNull Project project) {
            return client = new LanguageClientImpl(project);
        }
        @Override public @NotNull LSPClientFeatures createClientFeatures() {
            return new LSPClientFeatures() {
                @Override public boolean isEnabled(@NotNull VirtualFile file) {
                    return file.getName().equals("owned.workspace");
                }
            };
        }
    }
}
