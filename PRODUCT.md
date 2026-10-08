# Product

<!-- impeccable:product-schema 1 -->

## Platform
android

## Stack
User-selected Kotlin + Jetpack Compose, on the existing Android Studio toolchain.

## Users
Students browsing explicitly published dining menus and recovering demo tickets on mobile devices.

## Product Purpose
Make one-ticket-per-date/service eligibility and the separate outcomes of payment and issuance understandable, including controlled failures.

## Capabilities and Constraints
Authorized scope: menu selection, shared cart, five payment fixtures, ticket/operation histories, offline ticket display, PNG export. Wallet/account management stays unimplemented and visibly identified. All data is fictitious and in memory. No money, authentication, institutionally valid QR, backend, or persistence. Rejection retry is a fixture, not an agreed policy.

## Brand Commitments
SisCome; neutral Spanish UI; dark green, pale surfaces, yellow accents and task-meaningful vector icons. Material 3 native interactions. Preserve the supplied reference identity rather than inventing a replacement world.

## Evidence on Hand
`ANDROID_COMPONENTS.md` and read-only web `FINALIDAD.md`, `AGENTS.md`, full `App.tsx` and `index.css`. The delegated brief explicitly approves implementation and fixes scope, so no new design interview is needed. No device screenshots are available; no emulator launch is authorized.

## Accessibility & Inclusion
System Back, insets, scrollable content, native dialog focus, named controls, selected/disabled semantics, scalable text, status text independent of color. Device/TalkBack/large-text validation remains pending.
