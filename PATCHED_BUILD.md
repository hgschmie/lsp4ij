# Local LSP4IJ build

Build branch: `work/factorio-build`, checked out at
`upstream/lsp4ij/build/checkouts/patched`. The contribution branch
`feat/workspace-diagnostics` is based directly on upstream/main `f3825955` and
contains the workspace diagnostic implementation, tests and protocol documentation.
The work branch retains local IDEA 262/Gradle build accommodations and includes
upstream's accepted breakpoint and file-selection fixes.

Archive: `lsp4ij-0.21.1-SNAPSHOT-workspace-diagnostics-patched2.zip`, copied into
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
  --tests '*LanguageServerFileEnablementTest' --tests '*DAPBreakpoint*' buildPlugin
```

## Validation scope

The feature has ten request/cache tests and five real-transport/IntelliJ cache
tests. All five integration tests fail with upstream's unmodified production
hooks and pass with the feature. The selected suite also covers existing Rust
and TypeScript diagnostics, server restart, per-file routing and DAP breakpoint
removal (24 tests total). Outputs are under each checkout's `build/`; the
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
work. Keep analyzer UP-008 in the development build until its replacement is
verified. No regular-IDE live test is implied by the controlled integration tests.
