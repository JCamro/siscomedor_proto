# SisCome Native Implementation Plan

**Goal:** Implement the authorized Kotlin + Compose dining-ticket demo without changing the reference web project.

**Architecture:** A pure Kotlin `DemoSession` enforces selection, operation, and ticket rules. A single `SisComeViewModel` owns shared in-memory state and timed fixtures. Screen-specific composables own presentation; navigation and overlays remain separate.

**Tech stack:** Existing AGP 9.4.1, Gradle 9.6.0, minSdk 24, compileSdk/targetSdk 37; built-in Kotlin, Compose compiler, Material 3, Activity Compose, Lifecycle ViewModel, JUnit 4. No router, database, DI framework, or QR dependency.

## Constraints
- All edits stay in this Android project. No commits, Git initialization, delegation, or SDD.
- English code/docs; neutral Spanish product UI. Preserve dark green, pale surfaces, yellow accents, meaningful vector icons.
- Single fictitious student; full ISO consumption date + service is the unique key, including consumed history.
- Freeze reviewed lines into each operation; unresolved keys cannot be repaid or edited. Query the same reference idempotently.
- Payment and issuance remain independent. Rejected-payment retries are explicitly a demo fixture, not institutional policy.
- No real backend, authentication, wallet integration, money, stock guarantee, institutional QR, or process-death persistence.
- Do not start an emulator. Runtime/visual validation remains explicitly unperformed.

## Task 1 — Domain rules (test first)
**Files:** `app/src/test/java/com/example/siscomedor/DemoSessionTest.kt`, `domain/Models.kt`, `domain/DemoSession.kt`, `data/DemoCatalog.kt` under the main package.
**Interface:** `toggle(SelectionLine)`, `remove(SelectionKey)`, `submit(reference, snapshot, scenario)`, `query(reference)`, `recoverUnavailable(reference)`; read-only selection, operation, ticket collections.
- [x] Write tests for replacement/toggle, full-date uniqueness, duplicate submission, stable pending queries, issuance delay, frozen snapshots, retained variants, and explicit sold-out recovery.
- [x] Run `:app:testDebugUnitTest`; verify missing implementation before introducing domain code.
- [x] Implement immutable records and guarded deterministic transitions; rerun the same tests.

## Task 2 — Compose toolchain and state owner
**Files:** root/app Gradle scripts, version catalog, manifest, themes, `MainActivity.kt`, `ui/SisComeViewModel.kt`.
- [x] Reconcile the guide with official AGP built-in Kotlin documentation. Apply only Compose compiler plugin, matching the resolved Kotlin toolchain; preserve existing platform versions.
- [x] Add Material 3/UI, Activity Compose, ViewModel dependencies; remove unused View libraries and use a platform no-action-bar startup theme.
- [x] Implement ViewModel-owned domain session, processing-stage guards, menu loading, connectivity fixtures, real Android network observation, and overlay/back behavior.

## Task 3 — Native screens and distinct overlays
**Files:** `ui/SisComeApp.kt`, `ui/theme/Theme.kt`, `ui/components/Components.kt`, `ui/home/HomeScreen.kt`, `ui/selection/SelectionUi.kt`, `ui/checkout/CheckoutDialog.kt`, `ui/tickets/TicketsUi.kt`, `ui/tickets/QrExport.kt`, `ui/payments/PaymentsUi.kt`.
- [x] Build compact navigation bar / wide navigation rail; Home-only floating selection bar above navigation; scrollable inset-safe screens.
- [x] Build Home date/service/variant controls, distinct loading/closed/owned/unpublished/unresolved states, active-ticket shortcut and recent activity.
- [x] Build selection sheet, checkout review, five deterministic fixtures, stage feedback, explicit result facts and safe recovery actions.
- [x] Build ticket history/filter, separate picker/detail, stable fictitious QR and Storage Access Framework PNG export without storage permissions.
- [x] Build payment history/filter/detail with separate payment/issuance facts and same-reference queries. Wallet/account destinations explicitly disclose missing integrations.

## Task 4 — Verification and handoff
**Files:** `README.md`, this plan.
- [x] Normalize Kotlin/XML/Markdown before final checks; inspect domain invariants and overlay entry/exit paths.
- [x] Run foreground: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --console=plain`.
- [x] Run foreground: `.\gradlew.bat :app:lintDebug --console=plain`.
- [x] Record exact results and limitations; never infer runtime success from compilation. Device checks pending: phone/tablet, dark mode, large text, TalkBack, Back, PNG picker/export, all five scenarios/offline recovery.

## Final evidence and remaining gate
- Build/test command: success in 52s; 11 domain tests plus one existing template test pass.
- Lint command: success in 22s; no errors, three non-blocking warnings (newer Compose compiler available; two unused original launcher mipmaps).
- APK exists at `app/build/outputs/apk/debug/app-debug.apk`.
- Regression assertions reproduced sold-out repurchase and historical expired-ticket bugs before fixes, then passed.
- Required Gradle gates are complete. Runtime and visual approval remain pending; Impeccable disposition is `recapture` because phone/tablet captures were not authorized.

## Evidence gathered before edits
- Baseline `:app:testDebugUnitTest` passed with process-only `JAVA_HOME` pointing to Android Studio JBR; no Java on PATH.
- Official Android built-in Kotlin migration mirror: `https://github.com/android/skills/blob/main/build-system/agp/agp-9-upgrade/references/android/build/migrate-to-built-in-kotlin.md`.
- Official Compose setup mirror: `https://github.com/android/skills/blob/main/jetpack-compose/migration/migrate-xml-views-to-jetpack-compose/references/android/develop/ui/compose/setup-compose-dependencies-and-compiler.md`.
- Context7 was consulted, but returned older Compose setup and no built-in Kotlin match. Direct developer.android.com requests timed out; official Android repository mirrors supplied the current guidance.
- Reference `App.tsx` has duplicate imports/exports, queries from the mutable current cart, loses earlier variants through a latest-purchase fallback, and reuses references for distinct successful purchases. These are not product requirements and will not be ported.

## Bounded direct correction pass
1. Add focused test-first checks for exact operation ticket recovery, expired read-only detail, retained reference photos, and same-identity QR refresh states.
2. Scope operation overlays to the original reference/keys, while retaining generic available-ticket actions separately.
3. Restore the six reference menu photo assignments with Coil Compose plus its network module; expose loading/error text and external-photo limits.
4. Add deterministic QR refresh success/failure/offline states and loading feedback. Preserve saved content and last successful demo consultation; never claim institutional validation.
5. Update documentation and run the two required foreground Gradle commands. No emulator, delegation, commits, web edits, publication-change/suspension fixtures, or account/wallet expansion.

### Correction pass result
- Source corrections completed: operation-scoped recovery including expired read-only detail; original external photo assignments/loading/error states; same-identity QR loading/success/failure/offline fixtures.
- Test-first checks initially failed compilation on the absent correction APIs; the implementation then passed all six new checks. No assertion-based reproduction is claimed for that initial compile barrier.
- Final foreground `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --console=plain`: **BUILD SUCCESSFUL**, 42s. Reports show 18 tests (11 domain + 6 focused correction + 1 template), zero failures/errors/skips.
- Final foreground `.\gradlew.bat :app:lintDebug --console=plain`: **BUILD SUCCESSFUL**, 32s. No errors; five non-blocking warnings: newer Compose compiler, newer versions of both Coil modules, and two unused original launcher mipmaps.
- Assembly packages the dependency `libandroidx.graphics.path.so` unstripped; this is not a build failure.
- Five distinct reference photo URLs returned HTTP 200 / image/jpeg in bounded HEAD checks. Network image decoding/rendering was not device-tested.
- Process-only Android Studio JBR was used; no global environment or SDK/local.properties changes.
- Delivery status remains **partial solely because device/runtime/visual validation is unperformed**. Required commands pass and all three requested source corrections are complete; no emulator was launched.
