# Current local package — build 12 (2026-10-20)

Package: `lsp4ij-0.21.1-SNAPSHOT-12.zip`, with the same embedded plugin version.
The complete upstream version remains `pluginVersion=0.21.1-SNAPSHOT`;
`pluginBuild=12` is the local package counter. The work branch is rebuilt from
upstream/main `ef2b18c3` with only local build configuration, documentation and
test adaptations. Production source matches upstream exactly. UP-044/#1703 and
UP-055/#1704 are both merged upstream and are not replayed as local fixes.
The previous work head `30225337` is preserved at
`refs/archive/factorio-build-before-build12-20261020`.

The upstream base now includes the final workspace-diagnostics review fixes:
per-file matching reuse for unchanged reports, bounded asynchronous rechecks,
exception isolation for stamps and invalidation predicates, and retries after
`ContentModified`. Local IDEA 262 packaging, the diagnostic callback integration
test and inline-completion test API adaptation remain. Upstream build tool
versions are retained.

Validation evidence belongs in `build/investigations/build12-20261020/`.
Copy the validated ZIP to `upstream/lsp4ij/build/distributions/` and workspace
`plugins/`, then update FDE's development dependency pin and prepare
`dev/plugins/lsp4ij`. FDE retains its version and ZIP for this dependency-only
refresh. The receipt is `upstream/memory/LSP4IJ_BUILD12_2026-10-20.md`.
CodeRabbit is not run on internal build branches.

# Previous package — build 11 (2026-10-20)

Package: `lsp4ij-0.21.1-SNAPSHOT-11.zip`, with the same embedded plugin version.
The upstream version remains `pluginVersion=0.21.1-SNAPSHOT`; `pluginBuild=11`
is the local package counter. Rebuilt from upstream/main `0c0f4bf9` plus
UP-044 / PR #1703 through `8a6d22a7`. UP-055 is already supplied by upstream
merge `755ba4d7` and is not replayed. The previous work head `7628736a` is
preserved at `refs/archive/factorio-build-before-build11-20261020`.

Repeated invalidation of the same document cache now replaces its filter in
long-lived workspace diagnostic requests. The branch also retains upstream's
new document-content support and connection/stop lifecycle fixes. Production
sources match the reviewed contribution exactly. Local differences are IDEA 262
packaging support, the combined diagnostic callback test and the inline
completion test API adaptation. Upstream's updated build tools are retained.

Validation evidence belongs in `build/investigations/build11-20261020/`.
Copy the validated archive to `upstream/lsp4ij/build/distributions/` and workspace
`plugins/`, then update FDE's dependency pin and prepare `dev/plugins/lsp4ij`.
FDE retains its version and ZIP for this dependency-only refresh. The receipt is
`upstream/memory/LSP4IJ_BUILD11_2026-10-20.md`. No CodeRabbit run is required
for an internal build; its upstream contribution was reviewed before publication.

# Previous package — build 10 (2026-10-19)

Package: `lsp4ij-0.21.1-SNAPSHOT-10.zip`, with the same embedded plugin version.
The upstream version remains `pluginVersion=0.21.1-SNAPSHOT`; `pluginBuild=10`
is the local package counter. The work branch integrates UP-044 / PR #1703
through `d7a0b53b`, on the existing upstream base `88b9ece3` (including UP-055).
The previous work head `a14983eb` is retained at
`refs/archive/factorio-build-before-build10-20261019`.

Document cache changes now restart only providers whose cached result IDs or
active matcher are affected. Other requests continue while request-specific
filters reject stale reports. Provider removal preserves open-file diagnostics
owned by an active document provider with the same identifier. Empty reports
retain result IDs without allocating caches for unopened, uncached files.
Production sources match the reviewed PR branch exactly. Local IDEA 262 build
support and the combined diagnostic callback tests remain.

Validation evidence belongs in `build/investigations/build10-20261019/`.
Copy the validated ZIP to `upstream/lsp4ij/build/distributions/` and workspace
`plugins/`, then update FDE's dependency pin and prepare `dev/plugins/lsp4ij`.
FDE retains its version and ZIP for this dependency-only refresh. The receipt is
`upstream/memory/LSP4IJ_BUILD10_2026-10-19.md`. Use local validation for this
internal build; CodeRabbit reviews are limited to upstream contribution branches.

# Previous package — build 9 (2026-10-19)

Package: `lsp4ij-0.21.1-SNAPSHOT-9.zip`, with the same embedded plugin version.
The upstream version remains `pluginVersion=0.21.1-SNAPSHOT`; `pluginBuild=9`
is the local package counter. The work branch integrates UP-044 / PR #1703
through `cbd1679a`, on its existing upstream base `88b9ece3`. The previous
work head `0520d240` is retained at
`refs/archive/factorio-build-before-build9-20261019`.

This includes open/closed provider-removal cleanup, cache-lifetime result-ID
invalidation, cancellation of initial requests and pending full-report matches,
isolated cleanup failures, stable client references and snapshot-safe dynamic
capability iteration. Production sources match the reviewed PR branch. Local
IDEA 262 build support and both closed/open diagnostic callback tests remain.

Build and validation evidence belongs in `build/investigations/build9-20261019/`.
Copy the validated ZIP to `upstream/lsp4ij/build/distributions/` and workspace
`plugins/`, then update FDE's dependency pin and prepare `dev/plugins/lsp4ij`.
FDE retains its version and ZIP for this dependency-only refresh. The workspace
receipt is `upstream/memory/LSP4IJ_BUILD9_2026-10-19.md`.

# Previous package — build 8 (2026-10-19)

Package: `lsp4ij-0.21.1-SNAPSHOT-8.zip`, with the same embedded plugin version.
The upstream base stays in `pluginVersion=0.21.1-SNAPSHOT`; `pluginBuild=8` is
our local package counter. The work branch is rebuilt on upstream/main
`88b9ece3` plus UP-044 `feat/workspace-diagnostics` at `e534e0f0`.
UP-055 is now supplied by upstream merge `755ba4d7`, so its local commits are
not replayed. Previous work is preserved at
`refs/archive/factorio-build-before-refresh-20261019` (`7d11cfe1`).

Workspace-report eligibility checks use the project's cancellable read-action
helper before matching and before publication. Retained local changes are IDEA
262/Gradle build configuration and the combined provider-cleanup notification
test. A local test-only editor argument adapts upstream's new inline-completion
test to IDEA 262; it is not included in the contribution branch.

Build logs, fresh test XML and archive validation receipts belong in
`build/investigations/build8-20261019/`. Copy the validated archive to
`upstream/lsp4ij/build/distributions/` and workspace `plugins/`, then update
FDE's pin and prepare `dev/plugins/lsp4ij` before running compatibility tests.
FDE's version and ZIP remain unchanged for this dependency-only update.

## Previous packages

# Local package counter — build 7 (2026-10-16)

Package: `lsp4ij-0.21.1-SNAPSHOT-7.zip`; embedded version `0.21.1-SNAPSHOT-7`.
The complete upstream version, including its prerelease marker, stays in
`pluginVersion=0.21.1-SNAPSHOT`; the separate local counter is `pluginBuild=7`.
This implements the workspace HANDOFF naming convention. Build 6 omitted
`SNAPSHOT`; retain its archive as history and use build 7 for installation.

Production code is unchanged from the validated combined build 6: upstream/main
`cbe50d3d`, UP-044 `08475429` and UP-055 `bc613d84`, including all four completed
review fixes and both Javadoc updates. Local IDEA 262 build support remains.
All 57 selected diagnostic/lifecycle/routing/process tests pass, with no skips.
ZIP and nested JAR integrity, plugin ID/version/minimum IDE build and absence of
test classes are verified. Identical archives are copied to the main repository's
`build/distributions/` and workspace `plugins/`. Build and validation evidence:
`build/investigations/build7-20261016/`.

# Local package counter — build 6 (2026-10-16)

The installable archive is `lsp4ij-0.21.1-6.zip`; its plugin version is `0.21.1-6`.
`gradle.properties` keeps the upstream base in `pluginVersion` and a monotonic
local package counter in `pluginBuild`. Increase the counter for the next local
package. The diagnostic and patch-name suffixes are no longer part of the version.

Validation of the final combined package: 54 diagnostic/lifecycle/routing tests
pass with no failures or skips. Reports: `build/investigations/build6-20261016/`.

The work branch includes the signed documentation updates from UP-044
(`41f08b00`) and UP-055 (`bc613d84`), plus the completed UP-044 review
fix `08475429` clearing a rejected closed-file provider after routing changes.
It retains UP-055 diagnostic notifications on the combined work branch.
`buildPlugin` passed against the installed IDEA 262 SDK; the ZIP CRC and embedded
plugin descriptor were verified. Build log: `build/investigations/build6-20261016/gradle-final.log`.

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
`upstream/memory/LSP4IJ_PATCHED5_2026-10-16.md`.

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
and FDE integration evidence: workspace `upstream/memory/FACTORIO_PROBLEMS_2026-10-14.md`.

---

# Local LSP4IJ build

Build branch: `work/factorio-build`, checked out at
`upstream/lsp4ij/build/checkouts/patched`. The contribution branch
`feat/workspace-diagnostics` is based directly on upstream/main `0c0f4bf9` and
contains the workspace diagnostic implementation, tests and protocol documentation.
The work branch retains local IDEA 262/Gradle build accommodations and includes
upstream's accepted breakpoint and file-selection fixes.

Archive: `lsp4ij-0.21.1-SNAPSHOT-11.zip`, copied into
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
