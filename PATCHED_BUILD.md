# Local LSP4IJ build

Build branch: `work/factorio-build`, checked out under
`upstream/lsp4ij/build/checkouts/patched`. This branch includes upstream main
`4796cf99` and `fix/file-scoped-formatting` (`4ef6aa5c`), plus local IDEA 262
build support. The contribution branch contains only the selection fix and tests.

Current archive: `lsp4ij-0.21.1-SNAPSHOT-factorio-formatting-patched2.zip`.
Use JDK 25, the shared Gradle cache under `dev/cache/gradle`, and an isolated
Java home/temp directory under `dev/`. Supply `-PideaPath=/path/to/IntelliJ\ IDEA.app`,
`-PbuildJavaVersion=25`, and
`-Dorg.gradle.java.installations.paths=/path/to/IntelliJ\ IDEA.app/Contents/jbr/Contents/Home`
to `./gradlew --no-daemon --no-configuration-cache test buildPlugin`. Quote paths
containing spaces. The selected SDK is the installed IDEA 2026.2.3 (262).

The Factorio repository's `scripts/build-lsp4ij.sh` verifies the pinned local
source/ZIP, copies the archive into `upstream/lsp4ij/build/distributions`, and
prepares `dev/plugins/lsp4ij`. Its `lsp4ij.lock` records the exact source revision,
version and SHA-256. No regular IDE profile is modified.

## Regression evidence

The independent branch's four new tests fail on unpatched upstream and pass
with the fix. They cover `hasAny`, `processLanguageServers`, full-document
formatting and selection formatting with two servers sharing a file pattern
but owning different files. Runtime enablement changes are checked too.

On the local IDEA 262 SDK, the 66-test formatting/lifecycle/DAP selection has
29 failures without the fix and 25 with it. The same 25 pre-existing typing and
automatic indentation failures remain; the patch introduces no new failures.
Comparison evidence is in the fix worktree's `build/reports/file-scoped-comparison.json`.
The passing formatter, lifecycle and DAP tests are run for the local release.

## Review follow-up (2026-10-08)

PR #1688 follow-up `4ef6aa5c` shares the matching predicate between both methods
and checks for a missing virtual file before consulting client enablement. The
filter parameter remains non-null; `processLanguageServers` passes an accepting
predicate. Five file-enablement tests now cover missing virtual files and filter
rejection as well as ownership and formatting. All 19 selected regression tests
pass on the independent fix branch. No CI/toolchain changes are in the PR.

The original PR's Windows CI passed all 456 tests (three skipped), including the
four original regressions. Linux/macOS, archive building and verification failed
before compilation because automatic provisioning could not supply JetBrains
Runtime 21. This is independent of the patch. The broader local IDEA 262 failures
above remain the previous comparison; that suite was not repeated for this small
review follow-up. Local logs use `build/logs/file-scoped-review-*`.
