# SisCome Android — UI Components and Modularization Guide

This document maps the existing SisCome prototype into Android UI modules using Kotlin and Jetpack Compose. It is a guide for the later implementation, not a request to change the current project now. The existing web interface is evidence of current behavior and visual language; it is not a binding screen-by-screen design where it conflicts with the product rules in `FINALIDAD.md`.

> **Implementation update:** The direct native implementation is now in this project; see `README.md` and `IMPLEMENTATION_PLAN.md`. Statements below describing an empty package, a Views-only Gradle setup, and future implementation describe the original pre-implementation inventory. AGP 9.4.1 supplies built-in Kotlin; the implementation applies only the Compose compiler plugin, not `kotlin-android`. Device validation is still pending.

> **Bounded correction update:** Native menu cards now load the original illustrative Unsplash photos with explicit loading/error states. Checkout recovery is limited to the exact operation's ticket keys, including expired read-only detail without QR/export. Available-ticket QR refresh has local success/failure/offline fixtures that preserve identity/content and distinguish saved codes from a successful demo consultation. External-photo/offline limits and device validation remain open. Publication-price-change/suspension fixtures and broader account/wallet integrations remain unimplemented; this guide is not a claim of complete `FINALIDAD.md` coverage.

> **Recent UI correction:** Home's recent-activity summary presents issued ticket records and routes to My Tickets; operation history remains separate in Payments. Checkout and the selection sheet share selectable masked demo wallet accounts, with the chosen label frozen into each operation. These are local fixtures, not wallet integrations.

## Quick path

1. Prepare the Android app module for Kotlin + Jetpack Compose and Material 3. The current Gradle module uses AppCompat and Material Views and does not yet declare Compose dependencies.
2. Implement domain types and the independent payment/issuance states before connecting screens.
3. Build screens from the screen-specific composables below; extract a shared component only when its behavior and meaning are genuinely the same.
4. Use deterministic demo data and verify the acceptance checklist at the end.

## Project context and source of truth

The Android Studio project at this folder is the implementation target. Its current namespace is `com.example.siscomedor`, `minSdk` is 24, and the main source package currently contains no Kotlin app source. The web prototype lives in a separate project directory and is used as a reference.

Use these sources in this order when details disagree:

1. `FINALIDAD.md` in the web prototype: confirmed product purpose, business rules, safe outcomes, accessibility and constraints. It intentionally leaves the new interface and navigation open.
2. The current web implementation (`src/App.tsx`, `src/index.css`): observed screen content, visual patterns, and demo interactions—not proof that every behavior is a confirmed product rule.
3. `AGENTS.md` and `README.md` in the web prototype: useful implementation context, but their implementation-status descriptions can differ. Check the actual source before treating a feature as present.

The interface should communicate an operational student service, not a promotion. All demo identities, menus, wallets, payments, references, tickets, availability and QR codes are fictitious. Never imply real payment, institutional ticket validity, or AI functionality.

## Product map: screens, overlays, and navigation

Use a single app shell with five destinations. A destination is a full screen; dialogs and sheets are temporary overlays and must not be represented as separate navigation destinations.

| Destination / overlay | Purpose | Current prototype content |
|---|---|---|
| **Home / Menu** | Browse a published menu and assemble eligible, distinct date-service selections. | Greeting, active-ticket shortcut, connectivity/demo controls, date selector, service selector, menu variants, owned/closed states, selection summary, recent activity. |
| **My Tickets** | Find issued tickets and distinguish usable from consumed history. | Summary counts, filters, ticket cards, QR action, help notice. |
| **Payments** | Inspect payment operations without confusing them with ticket issuance. | Confirmed/pending/rejected summaries, filters, operation rows, detail action, explanatory notice. |
| **Wallet management** | Represent the digital-wallet setup task. | Navigation entry exists; dedicated management is not implemented in the web prototype. Model this as a future/demo state, not as a functioning integration. |
| **Account** | Represent profile and preferences. | Navigation entry exists; account management is not implemented in the web prototype. Do not invent editable fields or real identity behavior. |
| **Selection sheet** | Expand the current selection while staying on Home. | Full list of selected date-service-variant lines, total, demo wallet, remove/clear actions, checkout action, one-ticket rule. |
| **Checkout dialog** | Review the entire operation and choose a deterministic demo outcome. | Line review, wallet summary, total, demo scenario picker and confirmation. |
| **Operation progress/result** | Explain the active stage and final known outcome. | Verification, payment, ticket issuance; result-specific safe action and reference. Progress and result belong to checkout, not independent destinations. |
| **Ticket picker dialog** | Choose which existing ticket to inspect when the caller has not selected one. | Appears after a multi-ticket success or a generic ticket action. |
| **Ticket detail / QR dialog** | Show one ticket's identity, state and fictitious QR. | Date, service, hours, student, ticket ID, demo warning, QR download, close/back. |
| **Payment detail dialog** | Explain one operation's financial state and issuance state separately. | Reference, timestamp, wallet label, amount, payment status, issuance status, included services. |

Navigation must retain the selected destination underneath an overlay. Opening or closing a ticket dialog must not redirect the user to My Tickets. The Home active-ticket shortcut opens its ticket over Home. A successful checkout may open the ticket picker while leaving the current destination unchanged.

## Suggested package structure

Keep this prototype understandable; do not introduce networking, persistence, dependency injection, or extra architecture layers just to fill folders. Split by responsibility and screen, and keep demo-only data visibly separate from domain rules.

```text
app/src/main/java/com/example/siscomedor/
├── MainActivity.kt
├── navigation/
│   ├── AppNavigation.kt
│   └── AppDestination.kt
├── domain/
│   ├── model/
│   │   ├── Service.kt
│   │   ├── Menu.kt
│   │   ├── Selection.kt
│   │   ├── PaymentOperation.kt
│   │   └── Ticket.kt
│   └── rules/
│       ├── SelectionRules.kt
│       └── OperationRules.kt
├── data/demo/
│   ├── DemoCatalog.kt
│   └── DemoScenarios.kt
└── ui/
    ├── theme/
    ├── components/
    │   ├── AppScaffold.kt
    │   ├── AppNavigationBar.kt
    │   ├── StatusMessage.kt
    │   ├── PriceText.kt
    │   └── DemoNotice.kt
    ├── home/
    ├── selection/
    ├── checkout/
    ├── tickets/
    ├── payments/
    ├── wallets/
    └── account/
```

Each screen folder owns its screen composable and screen-specific subcomponents. Only stable, truly shared presentation pieces belong in `ui/components`. Put eligibility and state-transition rules in `domain`, not in composables. Keep the seeded demo catalog and repeatable outcomes in `data/demo`; these are not production repositories or real services.

## Component responsibilities

### App shell and navigation

| Compose component | Responsibility | Must not absorb |
|---|---|---|
| `SisComeApp` | Apply theme and host app navigation and overlay state. | Business rules or screen-specific UI. |
| `AppScaffold` | Provide shared top-level layout, content insets and app-level demo/connectivity status. | The Home menu, cart calculations, or payment states. |
| `AppNavigationRail` / `AppNavigationBar` | Show the same five destinations in wide and compact layouts, with selected state and ticket count where applicable. | Ticket picker or modal navigation. The rail and bottom bar share destination semantics but have distinct layouts and touch behavior; keep separate composables if their structures differ. |
| `ConnectivityStatus` | Display online/loading/offline/server status and offer a retry or demo control where the prototype exposes it. | Claiming cached menu availability is current or disabling offline ticket viewing. |

### Home and menu selection

| Compose component | Responsibility | Distinction to preserve |
|---|---|---|
| `HomeScreen` | Compose the Home-only content and connect it to screen state/actions. | Does not own payment/ticket domain rules. |
| `WelcomeHeader` | Show student-facing greeting and demo date. | Not account management. |
| `ActiveTicketCard` | Shortcut to one known active ticket and its QR. | Not the My Tickets history and not a ticket detail itself. |
| `MenuSection` | Present menu content plus loading, unavailable, and error states. | Loading is not an empty menu; no publication is not a connectivity error. |
| `DateSelector` / `DateOptionItem` | Select only dates with a confirmed published menu; show unavailable dates and selected count. | Availability must not be inferred for future dates. |
| `ServiceSelector` / `ServiceOptionCard` | Choose breakfast, lunch or dinner and show hours, sale state, selected/owned badges. | This is intentionally prominent, not a generic compact segmented control. A closed service may remain selectable for information but cannot be acquired. |
| `MenuVariantCard` | Show one published variant's name, description, price, image and selection state. | Similar-looking variants remain distinct domain items. Selecting another variant replaces the same date-service selection; it never adds a quantity. |
| `OwnedTicketNotice` | Explain that the selected date-service already has a ticket and link to its QR. | Different from a closed-sale message. It must not offer repurchase. |
| `SaleClosedNotice` | Explain that the sale window has closed and provide only a safe alternative if one exists. | Different from exhausted availability, missing publication, or an owned ticket. |
| `AvailabilityNotice` | Explain that the item is currently eligible to select and will be revalidated before payment. | Does not promise inventory or guarantee purchase. |
| `RecentActivitySection` | Show a small recent summary with links to the relevant ticket/payment history when wired. | Not the full My Tickets or Payments modules. |

### Selection and checkout

| Compose component | Responsibility | Distinction to preserve |
|---|---|---|
| `SelectionSummaryPanel` | Desktop/wide layout for current selected lines, total, removal and checkout. | Persistent panel presentation; the content is not the mobile floating bar. |
| `SelectionBar` | Compact mobile action showing count/services/total and a direct checkout action. | Its summary tap opens the sheet; the payment action opens checkout. Do not make the whole bar perform one action. |
| `SelectionSheet` | Expand the selected lines and rule explanation as a modal bottom sheet. | Overlay on Home, not a destination. It can reuse line rows but has its own layout, insets, scroll behavior and close action. |
| `SelectionLine` | Show one unique date + service + chosen variant, price and remove action. | One line is one ticket candidate; no quantity control. |
| `WalletSummary` | Identify the selected fictitious wallet in the prototype. | Not wallet linking/authorization and not transaction confirmation. Never request wallet secrets. |
| `CheckoutDialog` | Review every line and the total, then confirm one operation for all lines. | Not a payment history detail. Confirmation does not itself mean ticket issuance. |
| `DemoScenarioPicker` | Select a deterministic fixture: success, unknown payment, payment confirmed/issuance pending, rejected, or unavailable before payment. | Demo control only; never present as a real service choice. |
| `OperationProgress` | Communicate verification, payment and issuance steps while work is pending. | A spinner/progress state is not a success result. Prevent duplicate confirmation while processing. |
| `OperationResult` | Present known charge, issuance, selection-preservation and safe-next-action facts for one outcome. | Not interchangeable with the operation detail screen; it is a contextual result with a primary recovery action. |

### Tickets and payments

| Compose component | Responsibility | Distinction to preserve |
|---|---|---|
| `MyTicketsScreen` | Full ticket history, status summary, filters and ticket cards. | Separate destination; clicking the navigation item must not automatically open a modal. |
| `TicketFilterBar` | Filter all/available/consumed demo ticket records. | Ticket lifecycle filters, not payment filters. |
| `TicketCard` | Summarize one ticket's consumption date, service, variant, status and ID. | An available ticket may offer QR; consumed tickets must not look usable. |
| `TicketPickerDialog` | Select a ticket within the caller's context: generic available tickets, or only the exact operation's records. | Not `MyTicketsScreen`; never substitute unrelated available tickets for an expired operation result. Skip when one target is known. |
| `TicketDetailDialog` | Show one selected ticket's identity/status; provide QR/download only for available tickets. | Expired/consumed records are read-only with no usable QR. Opening/closing preserves the destination and operation scope. |
| `QrDisplay` | Render the same demo QR pattern for a ticket in its detail context. | Refresh/query may not change the ticket identity or QR contents. Offline/failure must not be portrayed as a recent validation. |
| `QrRefreshControls` | Demonstrate loading and deterministic success/failure/offline queries of an available ticket's saved QR. | Keep ID/content and last successful demo-consultation timestamp; failed/offline results stay stale and never imply institutional validity. |
| `PaymentsScreen` | Show operation summaries, filters, history and help content. | Financial operations, not ticket history. |
| `PaymentFilterBar` | Filter all/confirmed/pending/rejected operations. | Separate from ticket filters, even if both visually use segmented buttons. |
| `PaymentOperationRow` | Summarize one operation and open its detail. | Must show payment and issuance statuses as separate facts. |
| `PaymentDetailDialog` | Explain one operation's reference, timestamp, amount, payment state, issuance state and services. | Not a checkout result; no action may create a second payment merely by viewing details. |

### Shared visual components

Reuse only components with the same semantics and interaction contract. Likely shared pieces include `PrimaryButton`, `SecondaryButton`, `StatusMessage`, `EmptyState`, `LoadingSkeleton`, `PriceText`, `DemoNotice`, and `DialogHeader`. A shared visual shell is acceptable; do not create a universal card/status component that hides whether it describes a menu, payment, or ticket. Status text must remain understandable without color or icon alone. Use accessible labels, minimum touch targets, visible focus/pressed/disabled states, and reduced-motion-aware animations.

## State and domain boundaries

Recommended state ownership for this prototype:

- **App-level state:** current destination and whichever overlay is open. Closing an overlay returns to the same destination.
- **Home state:** date, service, menu loading, selection, connectivity demo state.
- **Selection state:** map keyed by `date + service`; each value contains exactly one variant. Derive sorted lines and total instead of storing duplicate totals.
- **Operation state:** selected scenario, operation reference, processing stage and result. One confirmation represents one operation across all selected lines.
- **Ticket state:** issued ticket records and selected ticket key. Ticket history is separate from payment history.
- **Payment state:** operation reference, timestamp, amount, payment status and issuance status. Querying an operation updates that record; it does not append another operation.

Minimum conceptual models:

```text
MenuPublication(date, service, variants, price, currency, hours, saleWindow, availability, updatedAt)
SelectionLine(date, service, variant)
PaymentOperation(reference, createdAt, amount, paymentStatus, issuanceStatus, selectedLines)
Ticket(id, date, service, variant, lifecycleStatus, qrContent)
```

Keep `paymentStatus` and `issuanceStatus` independent. At minimum the prototype must represent confirmed, pending/unknown and rejected payment outcomes, plus not-started, pending and completed issuance outcomes. Ticket lifecycle has its own available-for-use, consumed and expired meanings; do not label an unissued ticket “available”.

## Critical behavior invariants

1. A ticket's unique key is **student + service + consumption date**. The single fictitious student may be implicit in demo data, but the model must not use the menu variant as a way to bypass uniqueness.
2. There is no quantity. A second variant for the same key replaces the prior selection; selecting the active variant again removes it.
3. Existing ticket or active selection for the same key prevents a duplicate purchase. An issued ticket action recovers the existing ticket.
4. A future date is eligible only when it has an explicit valid menu publication. Published menu does not imply stock or purchase guarantee.
5. Revalidate publication, price, availability and eligibility before a confirmable payment. A price change requires explicit review.
6. Payment and issuance are separate. A confirmed payment may still have pending issuance; unknown payment does not mean no charge.
7. A pending/unknown operation retains its reference and must be queried using that same reference. Never recommend paying again before resolution.
8. Repeated operation submission/query must not duplicate a payment or ticket. A new reference is only a new operation when an outcome is resolved and policy permits another attempt.
9. A failure preserves the selection and other context unless the confirmed scenario explicitly requires an affected line to be removed after the user chooses that recovery.
10. Only digital wallets are supported by the product definition; do not offer cash. The demo wallet is fictitious and must not solicit a PIN, password or one-time code.
11. The QR is fictitious. Reopening, downloading, or simulating a refresh must preserve its ticket identity and content; never claim institutional validation.
12. Scenario fixtures are deterministic and repeatable. The demo clearly states what was charged, whether tickets were issued, what selection remains, and the safe next action.

Some policy questions remain unresolved in `FINALIDAD.md` (for example, whether a rejected/cancelled payment releases eligibility, how a purchased ticket is changed, and refund rules). Do not make those decisions in UI code or this component plan.

## Current prototype states to represent

| Scenario | Payment | Issuance | Selection | Safe primary action |
|---|---|---|---|---|
| Success | Confirmed | Completed | Emitted lines removed | View/select issued tickets. |
| Unknown result | Pending/unknown | Not confirmed | Preserved | Query the same operation reference. |
| Issuance delay | Confirmed | Pending | Preserved until resolved | Query issuance; do not pay again. |
| Rejected | Rejected | Not started | Preserved | Only retry as a new operation when the simulated result is resolved and permitted. |
| Unavailable before payment | Not started | Not started | Preserve unaffected lines; affected-line recovery is explicit | Remove/review the affected line; no charge occurred. |
| Offline/menu query failure | No new payment | No new issuance | Preserve intent and mark cached data stale | Retry menu query or view already-issued tickets. |

The current web demo has a fixed clock, seeded dates, a seeded wallet/ticket, in-memory state and no real backend. These are demo fixtures, not institutional facts or production integrations.

## Compose setup note for this Android project

The current `app/build.gradle.kts` declares AppCompat, Core KTX and Google Material View dependencies; it does not enable Compose. `gradle/libs.versions.toml` also does not declare Kotlin or Compose plugins/libraries yet. Before implementing the composables:

- use AGP 9 built-in Kotlin and apply the Compose compiler plugin with a compatible Kotlin toolchain version; do not add `org.jetbrains.kotlin.android` while built-in Kotlin is enabled;
- enable `buildFeatures { compose = true }` in the app module;
- add the Compose BOM and only the needed Compose UI, Material 3, Activity Compose, Navigation Compose (if a navigation host is chosen), and test dependencies;
- resolve versions centrally through the existing version catalog rather than copying version numbers from this document;
- choose either a Compose-first Activity or a deliberate Views/Compose interop plan; avoid maintaining duplicate implementations of the same screen.

This is a preparation checklist only. It does not edit Gradle files, update dependencies, or migrate the project.

## Implementation and verification checklist

- [ ] Every destination, dialog and bottom sheet has one clear responsibility and an explicit entry/exit action.
- [ ] Home, My Tickets and Payments are distinct destinations; ticket picker/detail and payment detail remain contextual overlays.
- [ ] Similar controls are shared only when semantics match; ticket filters and payment filters remain separate.
- [ ] Cart/selection, payment operation, ticket issuance and ticket lifecycle are independently modeled.
- [ ] Duplicate date-service tickets, quantities and variant-based uniqueness bypasses are impossible in the UI state transitions.
- [ ] Unknown payment, confirmed-payment/pending-issuance and rejection have different text, preserved state and safe recovery actions.
- [ ] Repeatable demo cases never create duplicate references, charges or tickets.
- [ ] Empty, loading, stale/offline, unavailable, closed, owned and error states are visibly distinct.
- [ ] Every status is communicated with text and not color alone; controls have accessible names and real enabled/disabled behavior.
- [ ] Navigation under overlays is preserved; direct QR actions open the intended ticket without changing destination.
- [ ] Narrow Android screens, bottom navigation, system insets, large text and reduced motion are checked.
- [ ] Demo limitations are visible before consequential interactions; no real payment, wallet credentials, valid ticket, or integration is implied.

## Deliberately out of scope

This document does not implement the Android UI, prescribe a final visual redesign, decide unresolved institutional policies, add APIs or persistence, integrate a wallet, authenticate students, validate QR tickets, or claim production readiness. The Android project is a prototype target; implementation should proceed screen-by-screen after confirming the intended visual design and product decisions.
