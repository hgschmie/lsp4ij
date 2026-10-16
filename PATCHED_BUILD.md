# Local package counter — build 6 (2026-10-16)

The installable archive is `lsp4ij-0.21.1-6.zip`; its plugin version is `0.21.1-6`.
`gradle.properties` keeps the upstream base in `pluginVersion` and a monotonic
local package counter in `pluginBuild`. Increase the counter for the next local
package. The diagnostic and patch-name suffixes are no longer part of the version.

The work branch includes the signed documentation updates from UP-044
(`41f08b00`) and UP-055 (`bc613d84`). Runtime code is unchanged from patched5.
`buildPlugin` passed against the installed IDEA 262 SDK; the ZIP CRC and embedded
plugin descriptor were verified. Build log: `build/investigations/build6-20261016/gradle.log`.

# Upstream and review refresh — patched5 (2026-10-16)

Package: `0.21.1-SNAPSHOT-workspace-diagnostics-patched5`, for IDEA 262.
The work branch merges current upstream/main `cbe50d3d` and both independent
contributions: UP-044 / #1703 at `f18c78d0`, and UP-055 / #1704 at `132d557b`.
The prior patched4 work head is preserved at
`refs/archive/factorio-build-before-patched5-20261016`.

This incorporates upstream process-startup, file-operation, diagnostic-disposal
and editor-refresh fixes. Workspace diagnostic matching now completes
asynchronously without blocking lifecycle operations. Matcher failures reject
only the affected report; unregistration clears the removed provider's closed
diagnostics and notifies diagnostic consumers while retaining other providers.
An integration regression in this work branch protects that cleanup callback.
Both contribution branches remain independent and unchanged.

The merged production tree matches the two PR patches combined on current main;
local differences are build configuration, this document and the integration
test. Validation evidence is under `build/investigations/patched5-20261016/`.
The selected suite covers diagnostics, matching, restart, disposal, refresh,
document stamps, process startup and breakpoints. FDE compatibility checks and
archive checksums are recorded in workspace
`upstream/LSP4IJ_PATCHED5_2026-10-16.md`.

# Previous diagnostic notification hook — patched4 (2026-10-14)

Package: `0.21.1-SNAPSHOT-workspace-diagnostics-patched4`. Adds the independent
`feat/diagnostic-update-hook` contribution branch (upstream/main `12703a33`),
allowing clients to observe accepted/merged diagnostic cache snapshots for both
open and closed files. Existing workspace diagnostic support remains unchanged.
FDE 0.8.0-dev uses this hook for its Factorio Problems tab; the callback itself
adds no UI and makes no language-server requests. Lifecycle clearing belongs to
the consuming client. Source and test follow-up are integrated with signed
provenance cherry-picks, avoiding unrelated upstream changes. No push or PR.

Build/check commands below still apply. Feature regressions cover closed-file
warning replacement/clearing and multiple providers in open files. Final package
and FDE integration evidence: workspace `upstream/FACTORIO_PROBLEMS_2026-10-14.md`.

---

# Local LSP4IJ build

Build branch: `work/factorio-build`, checked out at
`upstream/lsp4ij/build/checkouts/patched`. The contribution branch
`feat/workspace-diagnostics` is based directly on upstream/main `cbe50d3d` and
contains the workspace diagnostic implementation, tests and protocol documentation.
The work branch retains local IDEA 262/Gradle build accommodations and includes
upstream's accepted breakpoint and file-selection fixes.

Archive: `lsp4ij-0.21.1-SNAPSHOT-workspace-diagnostics-patched5.zip`, copied into
`upstream/lsp4ij/build/distributions/` after validation. FDE's `lsp4ij.lock` pins
the signed work revision, archive version and SHA-256. Run
`intellij-factorio/scripts/build-lsp4ij.sh` to verify and prepare
`dev/plugins/lsp4ij`. This does not install into the regular IDE profile.

Use JDK 25, `GRADLE_USER_HOME=~/ai/fmtk/dev/cache/gradle`, and Java home/temp
properties under `dev/build-home` and `dev/tmp`. Build with:

```sh
./gradlew --no-daemon --no-configuration-cache \
  '-PideaPath=/Users/henning/Applications/IntelliJ IDEA.app' \
  -PbuildJavaVersion=25 \
  '-Dorg.gradle.java.installations.paths=/Users/henning/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home' \
  test --tests '*features.diagnostics.*' --tests '*LanguageServerWrapper*' \
  --tests '*LanguageServerFileEnablementTest' --tests '*DAPBreakpoint*' \
  --tests '*DocumentContentSynchronizerDisposalTest' --tests '*EditorRefreshBatchTest' \
  --tests '*DocumentStampValidationTest' --tests '*OSProcessStreamConnectionProviderTest' \
  buildPlugin
```

## Historical validation scope (patched1–patched3)

The feature has ten request/cache tests and seven real-transport/IntelliJ cache
tests. All five integration tests fail with upstream's unmodified production
hooks and pass with the feature. The selected suite also covers existing Rust
and TypeScript diagnostics, server restart, per-file routing and DAP breakpoint
removal (26 tests total). Outputs are under each checkout's `build/`; the
before/after evidence is in the feature checkout's `build/reports/`.

The patched2 build also publishes each closed document's diagnostic list and
error status together through one immutable snapshot. The provider regression
checks error status as reports are added and cleared, and retains an older list
to verify later updates do not mutate it.

Workspace results are scoped per server and diagnostic provider. Closed files
receive full/unchanged/streamed reports; open files retain document-pull priority.
Refresh, dynamic unregistration and shutdown cancel outstanding requests. A
completed request is pulled again after one second to avoid a tight request loop.

This client feature is independent of the pending analyzer workspace scheduling
work. Analyzer UP-045 replaces UP-008 in the active development build;
UP-008 remains on its independent fallback branch. No regular-IDE live test is implied by the controlled integration tests.

## Patched3 document routing (2026-10-14)

Workspace reports now honor the same synchronous/asynchronous DocumentMatchers
as document connections. The generic EmmyLua server can scan all project roots
when an external Lua file is opened; its results must not leak into FDE-routed
mod files. Filtering does not start a server or connect a document.

Two transport regressions fail before the routing fix and pass afterward; the
selected 26-test suite passes. Before/after logs and the failing XML are retained
under build/logs and build/reports/workspace-matcher-before.
