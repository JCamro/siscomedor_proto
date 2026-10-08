---
name: SisCome Native
description: Source-derived web-faithful native Material 3 refinement
colors:
  primary: "#1B5D4B"
  primary-container: "#E5F2ED"
  yellow-accent: "#EFBD55"
  background: "#F4F7F5"
  surface: "#FFFFFF"
  foreground: "#172420"
  hero: "#123D32"
  pending-container: "#FFF4DB"
  dark-background: "#101D18"
  dark-primary: "#A3D8C4"
rounded:
  notice: "12dp"
  card: "16dp"
  hero: "20dp"
  dialog: "24dp"
spacing:
  context: "4–6dp"
  compact: "8dp"
  related: "12dp"
  notice: "16dp"
  screen: "20dp"
  section: "24dp"
---

# Design System: SisCome Native

## Overview
Source-derived documentation of the approved reference identity, adapted to native Material 3 tasks. This is **not visual approval**: no phone/tablet screenshots or device inspection were performed. The finishing visual-review disposition is `recapture`, pending authorized device captures.

## Colors
`ui/theme/Theme.kt` is the executable authority. Dark green (#123D32) anchors the ticket hero and cart; green (#1B5D4B) carries primary light-theme actions; yellow (#EFBD55) marks the brand/count. Unresolved operations use amber, not violet. All Material surface-container, inverse and tint roles are explicit: light navigation/sheets are white or green-neutral, dark surfaces use green-neutral counterparts. Separate schemes follow the system theme; no wallpaper-based recoloring is used.

## Typography
Bundled Inter defines every Material typography role: 26sp page headings, semibold 20sp section/hero headings, semibold 15sp card titles, 14sp body/actions and bold menu prices, 12sp menu descriptions/supporting copy and 11sp navigation labels, with explicit line heights, weights and zero letter spacing. `res/font/inter.ttf` is the unchanged upstream Inter variable TTF under SIL OFL 1.1; source, hash and license are bundled in `assets/fonts/`. Binary inspection confirms `wght` 100–900/default 400 and `opsz` 14–32/default 14. Android 26+ requests actual weight-axis instances and optical size 14; API 24–25 uses the regular default instance with native synthesis. No runtime remote font dependency exists. Type uses `sp`, not web px-to-dp conversion; actual loading and large-text rendering still need device verification.

## Layout
Compact navigation is a five-destination bottom bar. At 840dp and above, a distinct navigation rail replaces it and Home has a 320dp selection panel. Content scrolls; dates/history filters may scroll horizontally. The three service cards share available width equally inside a pale rounded group with 6dp inset; when effective width per card falls below 96dp after font scaling they stack. Owned/selected vectors accompany state text. Dates integrate counts in the day row, without a height-changing extra line or overlay. The filter uses an utensils vector; consumption/date/hours and sale-end metadata use calendar and clock vectors. Only today's menu carries the fixed simulated time. A divider separates criteria from menu states/variants. Scaffold insets reserve the cart above navigation.

Dialogs are width-constrained to 560dp with a fixed header/48dp top-end close row **outside** the separately scrolling body; the body cannot scroll underneath the close control. Its remaining-height weight keeps short dialogs natural and long content scrollable. Result-specific headings appear once in the body while the fixed strip still exposes close and the pane name. The selection sheet uses native Material behavior and viewport-relative sizing, with a fixed grouped header and **one** scrolling body containing lines, total, wallet and payment. No nested list scroll or height-consuming fixed footer is introduced.

Semantic rhythm is 4–6dp for titles/descriptions, 8–12dp within groups and 16–24dp between meaningful sections. Activity uses flat divided rows; ticket metadata uses dividers/proximity rather than nested cards. Payment detail keeps two explicit independent state rows. Actions stay inside their owned/closed/pending/connectivity/unavailable explanation. Routine availability and uniqueness guidance is subdued; unresolved and failure states keep appropriate amber/error roles and explicit safe consequences. These are source-derived structures, not device-layout certification.

## Elevation & Depth
Most content uses flat or outlined native surfaces. The floating selection surface uses Material shadow elevation at 6dp. Native button/chip/dialog/sheet elevation and state treatment remain framework-owned.

## Shapes
Notices, menu/ticket/payment cards, active-ticket/selection surfaces and dialogs use the recorded rounded scale. Native buttons, navigation and filter chips retain Material shapes.

## Components
Distinct meanings retain distinct components: service choice, menu variant, selection bar, selection sheet, checkout review/progress/result, ticket history/picker/detail and payment history/detail. Shared primitives cover icons, currency, notices and dialog framing, not domain decisions. Vector icons correspond to tasks; the launcher uses utensils rather than the template Android robot.

`MenuVariantCard` calls `MenuPhoto(variant.imageUrl)`: Coil's `SubcomposeAsyncImage` crops a 170dp image above name and description, with loading/error slots. Its 12dp rounded card footer uses a full-width space-between row: bold price left and outlined add pill far right, with a 48dp minimum touch height. Selected cards preserve green border, animated pale-green background, filled selected pill and photo check marker. Selection lines reuse the same image helper as 40dp thumbnails. Images are illustrative and remote; offline availability is not promised. Operation ticket pickers retain reference scope; expired/consumed detail has no QR controls. Available detail preserves the saved QR during refresh/failure and distinguishes a demo consultation from institutional validity.

## Motion and disclosures
Menu skeleton shimmer runs only during existing catalog/image waits. Date/service content uses a 220ms fade/6dp arrival keyed to the menu identity; selection uses a 150ms color/check transition; cart count and total crossfade on their actual value changes. The sheet retains native animation. Checkout shows a spinner, actual completed-stage track and current/done/upcoming labels, then a compact animated result. A query uses one truthful consultation step, not fabricated payment stages. Compose's duration scale controls finite/native animation; custom shimmer and spinner have an explicit `MotionDurationScale` zero-scale static branch. No new fixture delays were added.

The app shell exposes `Prototipo · Sin dinero real` before task interactions, with an expandable identity/wallet/payment/QR/session explanation. Wallet/account placeholders state their specific limitations without repeating the full global disclosure. Checkout uses `Confirmación de pago` and `Confirmar pago`, with one `No se realizará ningún cargo real.` notice beside confirmation and no PIN note. QR detail retains `Código de demostración sin validez institucional.` Demo scenario controls expand into a wrapping row; their consequence hint is hidden when collapsed. Results use a centered task vector, one outcome heading, precise consequences and a grouped `Importe de la operación`/reference/issued-ID context before recovery. The amount always uses the frozen operation snapshot, not the current selection or an assertion that money was charged. `Ver mis tickets` remains scoped to that reference. Unknown-payment/no-repeat warnings, payment versus issuance, saved/unchecked QR states and same/new reference distinctions remain visible. No static-clock stock-update claim is added.

## Do's and Don'ts
- Do preserve operational Spanish UI and English source identifiers/documentation; educational comments in owned Kotlin sources use neutral Spanish as explicitly requested.
- Do describe payment, issuance and ticket lifecycle independently, with text beyond color.
- Do keep ticket/payment overlays on the originating destination.
- Do use the same stable fictitious QR cell function in display and PNG export.
- Don't imply real funds, wallet authorization, institutional QR validation or inventory guarantee.
- Don't canonize untested screenshot fidelity, contrast, gesture behavior or accessibility outcomes. These remain device-review checks, not certified properties.
