# SisCome — Native Android Demo

A modular Kotlin + Jetpack Compose prototype for browsing fictitious university dining menus, selecting one variant per consumption date/service, and understanding payment and ticket-issuance outcomes separately. All UI copy is neutral Spanish. No real money, credentials, institutional tickets, or backend are involved.

## Run in Android Studio

1. Open this folder (`LABORATORIO 4`) as the Android project and sync Gradle.
2. Use Android Studio's bundled JBR as the Gradle JDK. Install the SDK/platform requested by the existing project: compileSdk/targetSdk 37, minSdk 24.
3. Select the `app` configuration and run on an explicitly chosen device/emulator. The launcher opens `MainActivity`.

Command-line verification from this folder (PowerShell):

```powershell
# Only if Java is not configured for this terminal; process-local, not a global change:
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --console=plain
.\gradlew.bat :app:lintDebug --console=plain
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`. No emulator or connected-device execution was performed during implementation.

## Modules and behavior

| Module | Responsibility |
|---|---|
| `domain/Models.kt`, `domain/DemoSession.kt`, `domain/QrRefreshState.kt` | Full ISO date/service keys; immutable operation snapshots; independent payment/issuance/lifecycle; exact operation ticket recovery and immutable same-QR refresh states. |
| `data/DemoCatalog.kt` | Explicit published dates, variants, prices, fixed clock, fictitious student/wallet and seeded history. |
| `ui/SisComeViewModel.kt` | Shared in-memory state, guarded progress fixtures, distinct references, menu loading and connectivity observation/simulation. Survives Activity recreation, not process death. |
| `ui/SisComeApp.kt` | Five destinations, compact navigation bar/expanded rail, inset handling and contextual overlays. |
| `ui/home/` | Active-ticket shortcut; date/service browsing; reference menu photographs with loading/error states; variant selection and distinct eligibility states. |
| `ui/selection/` | Home-only floating cart, wide summary, modal bottom sheet and removable lines. |
| `ui/checkout/` | Frozen review, five deterministic fixtures, progress and outcome-specific recovery. |
| `ui/tickets/` | History/filter, context-scoped picker/detail, expired/consumed read-only records, same-QR query fixtures and PNG export through the native document picker. No storage permission required. |
| `ui/payments/` | History/filter/detail; queries reuse the original reference and purchased lines. |
| `ui/theme/`, `ui/components/` | Material 3 light/dark green identity, yellow count accents, custom task vectors and shared presentation. |

### Safe operation fixtures

| Fixture | Charge / issuance | Selection and safe action |
|---|---|---|
| Success | Simulated charge confirmed; tickets emitted | Issued lines removed; recover only this operation's tickets without changing destination. Expired/consumed records have no QR or export action. |
| Unknown | Charge unresolved; no issuance | Snapshot and selection preserved; same-reference query only. |
| Issuance delay | Charge confirmed; issuance pending | Selection preserved; query issuance, never pay again. |
| Rejected | No charge; no issuance | Selection preserved; review a new reference. **This retry is a demo fixture, not agreed institutional policy.** |
| Sold out | Payment never started; no issuance | Selection preserved until explicit removal of affected line. That key stays exhausted for this session. No payment-history entry is shown for the verification-only attempt. |

Unresolved keys cannot be changed, removed, or paid again. Other distinct eligible keys may still be selected. Query resolves only the operation's frozen lines and does not consume unrelated selection. Repeated query/submission of a reference cannot create another payment or ticket. Variants are stored on each ticket rather than reconstructed from the most recent purchase. Consumed/expired tickets never release a key automatically.

Use the top-bar connection control for Normal, Slow, Offline, or Server-error fixtures. Real device connectivity also gates payments. Offline tickets remain visible as saved fictitious codes, never as recently validated credentials. Network loss before payment preserves selection without a new charge; loss after processing begins is represented as unknown and requires same-reference query.

### Contextual ticket recovery and QR refresh

The checkout result's **Ver tickets de esta operación** action matches the exact reference's frozen date/service keys. A single result opens that ticket directly; multiple results open a picker limited to that operation. Querying `DEMO-78355` therefore recovers its expired 1 October record, not unrelated available tickets. The destination underneath remains unchanged. Expired and consumed detail is read-only: no QR display, refresh or export.

On an available ticket, **Recargar mismo QR** offers deterministic local Success, Failure and Offline fixtures. Loading text accompanies the wait while the saved QR remains visible. Success retains the exact ticket ID/content and records a fixed-clock **demo consultation**, not institutional validation. Failure/offline retains the same saved code and the last successful demo-consultation label, explicitly marking the code as not recently checked. Real offline state at request start or completion forces the offline outcome. No refresh creates a ticket or payment.

### External menu photos

All six menu variant assignments reuse the reference's five distinct HTTPS Unsplash URLs, not fabricated imagery. Coil 3.6.0 Compose plus its OkHttp network module loads a cropped photo above the description/price; the manifest's Internet permission is used for these image requests. Loading/error text reserves the photo area, while description and selection remain available. Photos are illustrative reference assets, not evidence of actual portions, ingredients or availability.

Images are external, can become unavailable, and are not bundled or guaranteed offline. Coil may serve cached images, but offline image availability is not promised; the existing offline catalog/payment restriction remains. Five bounded URL HEAD checks returned HTTP 200 and image/jpeg, but device image decoding/rendering was not tested.

## Verification and remaining device checks

The local suite covers existing domain invariants plus exact-operation overlay targets, historical read-only recovery, reference photo assignments, and same-identity QR loading/success/failure/offline transitions. `CorrectionTest.kt` adds six focused checks. Gradle-generated test and lint reports live under `app/build/`.

The initial implementation checks succeeded with 11 domain tests plus the template test. The bounded correction pass adds six tests; final build/test and lint outcomes are recorded in `IMPLEMENTATION_PLAN.md`. No device success is inferred from Gradle reports.

Device validation is **not performed**. Before a presentation, check:

- Phone and tablet layouts, portrait/landscape, dark mode, large text and system insets.
- TalkBack, hardware keyboard focus, Android Back and dialog/sheet dismissal.
- Every fixture, closing/reopening pending results from Payments, offline tickets and network recovery.
- Historical `DEMO-78355` recovery, scoped multi-ticket pickers, read-only expired records, and QR refresh success/failure/offline with unchanged identity/content.
- External photo loading, decoding, error and offline behavior.
- PNG document-picker cancellation, permissions/error feedback, Activity recreation and selected-file output.

`DESIGN.md` records source-derived styling, not a rendered review. Impeccable visual-review disposition: `recapture` (phone/tablet screenshots unavailable; emulator launch not authorized). No native HTML/CSS detector was run, since it cannot evaluate Compose. Review/documentation were handled inline under the no-delegation instruction; no independent reviewer pass is claimed.

## Deliberate limitations

- No backend, authentication, wallet provider, stock integration, real reconciliation, QR encoding/validation/redemption, refund policy or concurrency guarantee.
- All records are session-only. Killing/restarting the process resets cart, references, tickets and operations; configuration changes retain the ViewModel session.
- Fixed demo clock: 7 October 2026, 16:40. The seeded same-day active ticket follows the reference fixture; its state is not a real consumption-window validation. Dates are explicitly published for 7, 8, 9 and 12 October; 10 October is visibly unpublished.
- Seeded history is fictitious. Querying the old pending operation produces an expired ticket, not a usable past-date ticket.
- Wallets and Account have honest informational destinations, not hidden Home fallbacks or invented editable fields. No wallet secrets are collected.
- Native Material typography is used rather than downloading the reference's Inter font. The reference's illustrative photos are loaded externally; offline availability and future URL health are not guaranteed.
- QR refresh is a local fixture only: no real query/validation endpoint, admin publication, ticket transfer/change, profile preferences or real notification feature is provided.
- Publication-price-change review, modified/suspended-menu fixtures, and broader `FINALIDAD.md` account/wallet/policy cases remain explicitly pending and were not added in this correction pass.

## Toolchain and reference reconciliation

The original AGP 9.4.1 and Gradle 9.6.0 / SDK versions are preserved. Kotlin compilation is provided by AGP built-in Kotlin; the Compose compiler plugin is 2.4.10, with Compose BOM 2026.09.00, Activity Compose 1.13.0 and Lifecycle 2.11.0, resolved centrally in the catalog. Unused AppCompat/Material Views libraries were removed. No `kotlin-android`, router, Hilt or database was added.

Current guidance was checked through Context7 and official Android documentation mirrors after developer.android.com requests timed out. The generic plugin advice in `ANDROID_COMPONENTS.md` is corrected for AGP 9. Reference `App.tsx` duplicate imports/exports and current-cart query/latest-purchase variant/reference-reuse defects are not ported. `FINALIDAD.md` contains an older static gap inventory (quantity/random rejection) that does not describe the current reference flow; its confirmed product rules remain authoritative.
