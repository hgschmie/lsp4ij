/*******************************************************************************
 * Copyright (c) 2025 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package com.redhat.devtools.lsp4ij;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static com.redhat.devtools.lsp4ij.features.diagnostics.LSPDiagnosticUtils.isDiagnosticsChanged;

/**
 * LSP closed document for a given language server.
 *
 * @author Angelo ZERR
 */
public class ClosedDocument extends LSPDocumentBase {

    private final Map<String, List<Diagnostic>> diagnosticsPerIdentifier = new HashMap<>();
    private volatile List<Diagnostic> diagnostics = List.of();
    private volatile boolean hasErrors;

    @Override
    public synchronized boolean updateDiagnostics(@NotNull String identifier,
                                     @NotNull List<Diagnostic> diagnostics, @Nullable Integer version) {
        if (diagnostics.isEmpty()) {
            diagnosticsPerIdentifier.remove(identifier);
        } else {
            diagnosticsPerIdentifier.put(identifier, List.copyOf(diagnostics));
        }
        var combined = diagnosticsPerIdentifier.values().stream().flatMap(List::stream).distinct().toList();
        boolean changed = isDiagnosticsChanged(this.diagnostics, combined);
        this.diagnostics = combined;
        hasErrors = combined
                .stream()
                .anyMatch(diagnostic -> diagnostic.getSeverity() != null && diagnostic.getSeverity() == DiagnosticSeverity.Error);
        return changed;
    }

    @Override
    public Collection<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    @Override
    public boolean hasErrors() {
        return hasErrors;
    }
}
