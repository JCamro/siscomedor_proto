---
name: SisCome Native
description: Source-derived Material 3 student-service prototype
colors:
  primary: "#1B5D4B"
  primary-container: "#E5F2ED"
  yellow-accent: "#EFBD55"
  background: "#F4F7F5"
  surface: "#FFFFFF"
  foreground: "#172420"
  pending-container: "#EEE9F8"
  dark-background: "#101D18"
  dark-primary: "#A3D8C4"
rounded:
  notice: "12dp"
  card: "16dp"
  hero: "20dp"
  dialog: "24dp"
spacing:
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
`ui/theme/Theme.kt` is the executable authority. Green carries active actions and ticket shortcuts; yellow marks selection count and connection warnings. Violet distinguishes unresolved operations. Separate light/dark schemes follow the system theme; no wallpaper-based recoloring is used.

## Typography
Material 3's default native typography roles are used, without a remote font dependency. Page headings use headline roles; service/card headings use title roles; descriptions use body roles; controls and metadata use label roles. Text scales through `sp` defaults; actual large-text behavior still needs device verification.

## Layout
Compact navigation is a five-destination bottom bar. At 840dp and above, a distinct navigation rail replaces it and Home has a 320dp selection panel. Content scrolls; date/service/filter rows can scroll horizontally. Scaffold insets reserve the cart bar above navigation. Dialogs are width-constrained to 560dp with scrollable content; sheets use native Material modal behavior.

## Elevation & Depth
Most content uses flat or outlined native surfaces. The floating selection surface uses Material shadow elevation at 6dp. Native button/chip/dialog/sheet elevation and state treatment remain framework-owned.

## Shapes
Notices, menu/ticket/payment cards, active-ticket/selection surfaces and dialogs use the recorded rounded scale. Native buttons, navigation and filter chips retain Material shapes.

## Components
Distinct meanings retain distinct components: service choice, menu variant, selection bar, selection sheet, checkout review/progress/result, ticket history/picker/detail and payment history/detail. Shared primitives cover icons, currency, notices and dialog framing, not domain decisions. Vector icons correspond to tasks; the launcher uses utensils rather than the template Android robot.

Menu cards retain the reference's cropped photo-above-copy composition using external illustrative images, with loading/error text instead of fabricated replacements. Operation ticket pickers retain reference scope; expired/consumed detail has no QR controls. Available detail shows the saved QR throughout a refresh wait/failure and communicates the demo consultation separately from institutional validity. No device-level rendering judgment is implied.

## Do's and Don'ts
- Do preserve operational Spanish UI and English source identifiers/documentation.
- Do describe payment, issuance and ticket lifecycle independently, with text beyond color.
- Do keep ticket/payment overlays on the originating destination.
- Do use the same stable fictitious QR cell function in display and PNG export.
- Don't imply real funds, wallet authorization, institutional QR validation or inventory guarantee.
- Don't canonize untested screenshot fidelity, contrast, gesture behavior or accessibility outcomes. These remain device-review checks, not certified properties.
