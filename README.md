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
| `data/DemoCatalog.kt` | Explicit published dates, variants, prices, fixed clock, fictitious student, two masked demo wallet choices and seeded history. |
| `ui/SisComeViewModel.kt` | Shared in-memory state, guarded progress fixtures, distinct references, menu loading and connectivity observation/simulation. Survives Activity recreation, not process death. |
| `ui/SisComeApp.kt` | Five destinations, compact navigation bar/expanded rail, inset handling and contextual overlays. |
| `ui/home/` | Active-ticket shortcut; date/service browsing; reference menu photographs with loading/error states; variant selection and distinct eligibility states; recent ticket records open their exact read-only/available detail and link to My Tickets. |
| `ui/selection/` | Home-only floating cart, wide summary, modal bottom sheet and removable lines. |
| `ui/checkout/` | Frozen line review with shared selectable demo wallet, five deterministic fixtures, separate progress and outcome-specific recovery. Each operation stores its selected masked demo wallet; queries preserve it. |
| `ui/tickets/` | History/filter, context-scoped picker/detail, expired/consumed read-only records, same-QR query fixtures and PNG export through the native document picker. No storage permission required. |
| `ui/payments/` | History/filter/detail; queries reuse the original reference and purchased lines. |
| `ui/theme/`, `ui/components/` | Bundled Inter, explicit Material light/dark container roles, green/yellow tokens, custom task vectors, photo fallbacks and system-scale-aware motion. |

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

The checkout result's **Ver mis tickets** action matches the exact reference's frozen date/service keys. A single result opens that ticket directly; multiple results open a picker limited to that operation. Querying `DEMO-78355` therefore recovers its expired 1 October record, not unrelated available tickets. The destination underneath remains unchanged. Expired and consumed detail is read-only: no QR display, refresh or export.

On an available ticket, **Recargar mismo QR** offers deterministic local Success, Failure and Offline fixtures. Loading text accompanies the wait while the saved QR remains visible. Success retains the exact ticket ID/content and records a fixed-clock **demo consultation**, not institutional validation. Failure/offline retains the same saved code and the last successful demo-consultation label, explicitly marking the code as not recently checked. Real offline state at request start or completion forces the offline outcome. No refresh creates a ticket or payment.

### External menu photos

All six menu variant assignments reuse the reference's five distinct HTTPS Unsplash URLs, not fabricated imagery. Coil 3.6.0 Compose plus its OkHttp network module loads a cropped photo above the description/price; the manifest's Internet permission is used for these image requests. Loading/error text reserves the photo area, while description and selection remain available. Photos are illustrative reference assets, not evidence of actual portions, ingredients or availability.

Images are external, can become unavailable, and are not bundled or guaranteed offline. Coil may serve cached images, but offline image availability is not promised; the existing offline catalog/payment restriction remains. Device image decoding/rendering has not been tested. Prior photo documentation described the desired wiring before it existed; the visual refinement now connects `MenuVariantCard` to `MenuPhoto`, which calls `SubcomposeAsyncImage` with crop/loading/error slots. Selection/review rows use the same URLs as thumbnails.

### Native visual refinement

- Inter is bundled in `res/font/inter.ttf` from the official Inter project, under SIL OFL 1.1. `assets/fonts/SOURCE.md` records the upstream URL/blob, size and SHA-256; `OFL.txt` ships the license. Binary inspection confirms `wght` 100–900/default 400 and `opsz` 14–32/default 14. Android 26+ explicitly loads weights 400/500/600/700/800 with optical size 14; Android 24–25 uses the regular default instance and native synthesis. Menu title/description/price roles use 15/12/14sp, explicit line heights and zero tracking; section headings use semibold rather than bold. This is native scalable hierarchy, not a pixel-to-dp equivalence or rendered font approval.
- Explicit typography/shape/color roles keep navigation and sheets white/neutral in light mode and green-neutral in dark mode. The compact yellow S brand, dark-green ticket hero, white QR action, photo-first menu and dark-green/yellow-count cart carry the web identity.
- Three service cards share available width inside a pale neutral rounded group, with an utensils-labeled filter, owned/selected vectors and separate calendar consumption / clock sale-end metadata. Font-scale/effective-width fallback stacks them instead of clipping the third service. Dates remain scrollable and integrate their count on the day row, without an extra text line or overlapping badge. Photo-first menu cards retain their 12dp corners, bold left price and far-right outlined add pill with a 48dp minimum touch height. Selection retains thumbnails, right-aligned prices and pending-aware `Vaciar`; its sheet now has a fixed title/count/clear/close header and one scrolling body, including the total and payment action. There is no nested line-list scroller or fixed footer competing for short-window height.
- Checkout uses **Confirmación de pago** / **Confirmar pago**, with one explicit no-real-charge notice and the persistent top prototype disclosure. Demo hints appear only when options are expanded. Review groups introduction/reference, service lines, wallet/total and confirmation with dividers. Every outcome shows **Importe de la operación** from `operation.cents`, including unresolved, issuance-pending, rejected and not-started results; this label does not guarantee a charge. A centered task icon, one outcome heading, safe consequences, original reference and emitted IDs replace repeated outcome labels. The exact-operation ticket action remains. Shared dialogs use a fixed header/48dp close row followed by a separately scrolling body, rather than an overlay X with only title padding. Native Back/outside/processing restrictions remain.
- Home activity now summarizes issued ticket records (service/variant, consumption date, stored line amount, lifecycle status and ticket ID), opens the exact ticket record, and routes “Ver historial” to My Tickets. Payment operations remain in Pagos. The checkout and selection sheet share one of two generic masked demo accounts; each submitted operation snapshots its selected account for payment history and detail.
- Existing fixture waits drive skeleton shimmer and menu arrival; selection/check, cart values, stages/results and native sheets animate state changes. Compose duration scaling applies to finite/native motion; custom infinite shimmer/spinner explicitly use a static branch when system motion is disabled. No artificial waits were added.
- A compact prototype disclosure precedes interactions, with expandable limitations. Simulated-payment confirmation and QR detail carry scoped notices; routine labels no longer repeat `ficticio`. Unknown-payment/no-repeat warnings and saved/unchecked QR distinctions remain.

These are source-level implementation facts, not rendered fidelity or accessibility approval.

### Whole-app semantic grouping

The authorized UI-only pass preserves the theme, catalog, ViewModel, payment rules and QR/export behavior. It groups title/description at 4–6dp, related content at 8–12dp and meaningful boundaries at 16–24dp rather than shrinking typography globally.

| Area | Source-level change |
|---|---|
| Home | Grouped greeting/hero context; distinct menu heading, dates, service filter, metadata and divided content. Fixed demo time appears only for today's menu. Owned/closed states and pending/offline/server/sold-out notices contain their associated actions. Availability context is low-emphasis and still states revalidation/no reservation. Tomorrow's action checks publication. |
| Activity and selection | One activity header with flat divided ticket rows; no repeated `REGISTRO` labels. Cart shows count/full service names/total with an opening affordance and a separate payment action. Selection groups header controls, divided lines, wallet/total/payment and subdued uniqueness guidance. |
| Tickets and QR | Compact summaries and grouped filters/records. Service/status, consumption/variant and secondary identity use proximity/dividers instead of nested metadata cards. Picker rows retain ID/state/scope. Detail places the QR before secondary identity; saved-code freshness, last demo consultation and refresh action remain grouped. Read-only guards and mandatory invalidity notice remain. |
| Payments and placeholders | Compact summaries/history groups; adjacent but independent payment/issuance facts. Detail uses two explicit state rows instead of giant generic notices, frozen wallet/amount and divided services. Wallet/account screens retain specific limitations without a duplicate global demo paragraph. |

Educational comments around these Compose groups remain neutral Spanish; code identifiers and documentation remain English.

## Learning entry points

Owned Kotlin application sources contain neutral Spanish educational comments; identifiers remain English. Follow this short reading path:

1. `MainActivity.kt` → `ui/theme/Theme.kt` → `ui/SisComeApp.kt`: Compose entrypoint, inherited roles, state-driven rendering, window adaptation, modifier order, insets and safe Back.
2. `domain/Models.kt` → `data/DemoCatalog.kt` → `domain/DemoSession.kt`: full date/service uniqueness, integer amounts, eligibility, immutable collection replacement and frozen operation lines/wallets.
3. `ui/SisComeViewModel.kt`: shared observable state versus local `remember`/`rememberSaveable`, lifecycle-bound coroutines, cancellation, guarded confirmation and same-reference query.
4. `ui/components/Components.kt`, `ui/home/`, `ui/selection/`, `ui/checkout/`, `ui/payments/`, `ui/tickets/`: layout weights, scalable controls, accessibility semantics, loading/offline states, dialog framing and independent payment/issuance outcomes.
5. `domain/DemoQr.kt`, `domain/QrRefreshState.kt`, `ui/tickets/QrExport.kt`: deterministic saved identity, read-only expiry guards, document-picker authorization, IO dispatching, stream closure and bitmap cleanup.

Comments explain meaningful blocks rather than imports, braces or every assignment. Unit tests demonstrate domain rules; source-contract assertions guard wiring, not rendered pixels.

## Verification and remaining device checks

The local suite covers existing domain invariants plus exact-operation overlay targets, historical read-only recovery, reference photo assignments, same-identity QR loading/success/failure/offline transitions, frozen wallet selection across query, and Home ticket-activity wiring. These source-wiring checks do not execute Compose or certify pixels. Gradle-generated test and lint reports live under `app/build/`.

The semantic-grouping baseline ran 32 unit tests and reproduced three expected source-contract failures (all-outcome amounts, dialog structure and status/sheet grouping). After implementation and an added all-scenario frozen-amount check, the suite passes **33 tests**: 13 domain, 6 correction, 13 source contracts and 1 template test, with zero failures/errors/skips. Foreground `:app:testDebugUnitTest :app:assembleDebug --console=plain` and `:app:lintDebug --console=plain` exited successfully. Lint reports zero errors and five advisory warnings: three newer dependency versions and two unused launcher resources. Source contracts check wiring, not rendered Compose layout; the template arithmetic test is not product coverage. No device success is inferred from Gradle reports.

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
- Inter is bundled, but actual font loading/weight rendering (especially API 24–25), large text, dark theme and animation-disable behavior still need device checks. The reference's illustrative photos are loaded externally; offline availability and future URL health are not guaranteed.
- QR refresh is a local fixture only: no real query/validation endpoint, admin publication, ticket transfer/change, profile preferences or real notification feature is provided.
- Publication-price-change review, modified/suspended-menu fixtures, and broader `FINALIDAD.md` account/wallet/policy cases remain explicitly pending and were not added in this correction pass.

## Toolchain and reference reconciliation

The original AGP 9.4.1 and Gradle 9.6.0 / SDK versions are preserved. Kotlin compilation is provided by AGP built-in Kotlin; the Compose compiler plugin is 2.4.10, with Compose BOM 2026.09.00, Activity Compose 1.13.0 and Lifecycle 2.11.0, resolved centrally in the catalog. Unused AppCompat/Material Views libraries were removed. No `kotlin-android`, router, Hilt or database was added.

Current guidance was checked through Context7 and official Android documentation mirrors after developer.android.com requests timed out. The generic plugin advice in `ANDROID_COMPONENTS.md` is corrected for AGP 9. Reference `App.tsx` duplicate imports/exports and current-cart query/latest-purchase variant/reference-reuse defects are not ported. `FINALIDAD.md` contains an older static gap inventory (quantity/random rejection) that does not describe the current reference flow; its confirmed product rules remain authoritative.
