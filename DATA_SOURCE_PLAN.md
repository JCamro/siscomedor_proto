# SisCome Data Source Plan

**Goal:** Stop hardcoding demo data in Kotlin. Load it from one file (`app/src/main/assets/catalog.json`) through an interface, so a database or API can later replace the file without touching the UI or the domain rules.

**Status:** Proposal only. Nothing is implemented yet.

## Current state

| Hardcoded data | Where |
|---|---|
| Clock (`today`, `time`), student, wallets, published dates, menu variants, initial tickets, payment history | `data/DemoCatalog.kt` (`object`, read globally) |
| Prices, hours, sale cutoff per service | `domain/Models.kt` (`enum class Service`) |
| Default wallet duplicated | `domain/Models.kt:46` |
| Greeting name "Antonia" and date "Miércoles, 7 de octubre" | `ui/home/HomeScreen.kt:54`, `ui/SisComeApp.kt:77` |
| Timestamp `"7 oct 2026 · …"` | `domain/DemoSession.kt:65`, `ui/SisComeViewModel.kt:301` |
| Next ticket number `1049` | `domain/DemoSession.kt:13` |
| Business rules (`published`, `closed`, `eligible`) | `data/DemoCatalog.kt:44-48` (should be domain) |

Problems: `domain` imports `data` and vice versa (import cycle), and 30+ call sites read `DemoCatalog` directly, so the data source cannot be swapped.

## Target architecture

```
ui (ViewModel, composables)
   │ uses
   ▼
domain  ── Catalog (data class), CatalogRules, DemoSession
   │       CatalogRepository (interface = the "port")
   ▲ implements
data   ── JsonCatalogRepository (now)   → reads assets/catalog.json
          RoomCatalogRepository / ApiCatalogRepository (later)
```

The dependency always points **toward `domain`**. The UI only knows `CatalogRepository`; it never knows whether the data came from a file, Room, or HTTP.

```kotlin
// domain/CatalogRepository.kt
interface CatalogRepository {
    suspend fun load(): Catalog   // suspend now so a DB/network source needs no signature change
}
```

## File format (`app/src/main/assets/catalog.json`)

```json
{
  "clock": { "today": "2026-10-07", "time": "16:40" },
  "student": { "name": "Brayan Motta Medina", "firstName": "Brayan" },
  "wallets": ["Billetera Demo ···· 2841", "Billetera Demo ···· 7390"],
  "services": [
    { "id": "BREAKFAST", "label": "Desayuno", "hours": "07:00–09:30", "saleEnd": "09:00", "cents": 450 }
  ],
  "dates": [ { "date": "2026-10-07", "day": "HOY", "published": true } ],
  "variants": [
    { "service": "BREAKFAST", "id": "traditional", "name": "Desayuno clásico",
      "description": "Pan con pollo, fruta de estación y bebida caliente",
      "imageUrl": "https://images.unsplash.com/photo-1619957666910-bf7ba37cda1b?auto=format&fit=crop&w=320&q=80" }
  ],
  "tickets": [
    { "id": "SC-2026-1048", "date": "2026-10-07", "service": "DINNER", "variant": "traditional",
      "status": "AVAILABLE", "issuedAt": "2026-10-07T11:42" }
  ],
  "payments": [
    { "reference": "DEMO-78392", "createdAt": "2026-10-02T11:48", "status": "CONFIRMED",
      "issuance": "COMPLETED", "scenario": "SUCCESS",
      "lines": [ { "date": "2026-10-02", "service": "LUNCH", "variant": "traditional" } ] }
  ]
}
```

Design choices:
- **Flat lists with IDs** (`variants`, `tickets`, `payments` reference services/variants by `id`) mirror database tables, so moving to SQL later is a 1:1 mapping.
- **Timestamps in ISO format** (`2026-10-07T11:42`). The UI formats them for display ("7 oct 2026 · 11:42"); the data never stores display text.
- **Prices live with the service**, not in the enum. `Service` stays an enum for identity only (`BREAKFAST`, `LUNCH`, `DINNER`); label, hours, cutoff and price come from the file.

## Parsing library

Recommended: **kotlinx.serialization** (official JetBrains plugin + `kotlinx-serialization-json`).

| Option | Pros | Cons |
|---|---|---|
| kotlinx.serialization | Type-safe `@Serializable` DTOs, works in plain JVM unit tests, same DTOs reusable for a future REST API | Adds a Gradle plugin and one library |
| `org.json` (built into Android) | No dependency | Manual, verbose parsing; Android stubs throw in local unit tests, so tests need Robolectric or instrumentation |

Use a library version compatible with the project's Kotlin 2.4 toolchain (check the kotlinx.serialization release notes when adding it).

## Phases

### Phase 1 — Move rules to the domain and break the cycle (refactor, no behavior change)
- [ ] Create `domain/Catalog.kt`: `data class Catalog(clock, student, wallets, services, dates, variants, tickets, payments)` plus `ServiceInfo`, `Student`, `DemoClock`.
- [ ] Move `published`, `closed`, `eligible`, `variants(service)`, `line(...)` from `DemoCatalog` into `domain/CatalogRules.kt` (methods on `Catalog` or a small class taking it).
- [ ] Change `DemoSession` to `class DemoSession(private val catalog: Catalog, includePaymentHistory: Boolean = false)`. Remove `import ...data.DemoCatalog`. Derive `nextTicket` from the highest existing ticket number.
- [ ] Keep `DemoCatalog` temporarily as a builder returning a `Catalog` with today's exact values, so all tests keep passing.
- [ ] Run `:app:testDebugUnitTest`. All existing tests must stay green.

### Phase 2 — Load the catalog from `catalog.json`
- [ ] Add the kotlinx.serialization plugin and library to `gradle/libs.versions.toml` and `app/build.gradle.kts`.
- [ ] Create `assets/catalog.json` with exactly the data now in `DemoCatalog.kt` (same values, so the demo looks identical).
- [ ] Create `data/CatalogDto.kt` (`@Serializable` classes mirroring the JSON) and `data/CatalogMapper.kt` (`fun CatalogDto.toDomain(): Catalog`). DTOs never leave `data`.
- [ ] Create `domain/CatalogRepository.kt` (interface) and `data/JsonCatalogRepository.kt` (reads the asset with `context.assets.open(...)` on `Dispatchers.IO`).
- [ ] In `SisComeViewModel`, load the catalog in `init` via `viewModelScope.launch`, then create `DemoSession(catalog)`. Add a `CatalogState` (`Loading`, `Ready`, `Error`) and show a loading/error screen.
- [ ] Validate on load: unknown service IDs, variants referenced by tickets that don't exist, malformed dates → `Error` with a clear message, never a crash.
- [ ] Tests: put a copy of the JSON under `app/src/test/resources/` and test the mapper and validation without a device. Delete `DemoCatalog.kt`.

### Phase 3 — Remove remaining hardcoded values in the UI
- [ ] Replace "Hola, Antonia" and "Antonia Muñoz" with `catalog.student` (also fixes the name-mismatch bug).
- [ ] Build "Miércoles, 7 de octubre" and "7 oct 2026 · 16:40" from `DemoClock` with a single formatter (`ui/Formatters.kt`).
- [ ] Replace every remaining `DemoCatalog.*` read in `HomeScreen`, `TicketsUi`, `Components`, `SisComeViewModel` with values exposed by the ViewModel.

### Phase 4 — Ready for a database (later, decide then)
The file is **read-only seed data**. Tickets and payments created at runtime still live only in memory. A database needs a write side:
- [ ] Split the port: `CatalogRepository` (menu, dates, services: read) and `OrderRepository` (`saveOperation`, `updateOperation`, `saveTickets`: read/write).
- [ ] Implement one of:
  - **Room (local SQLite):** tables `service`, `published_date`, `variant`, `ticket`, `payment`, `payment_line`. Prepopulate from the same `catalog.json` on first launch.
  - **Remote API / Supabase / Firebase:** `ApiCatalogRepository` reusing the same `@Serializable` DTOs.
- [ ] Swap the implementation in one place (ViewModel factory). UI and `DemoSession` stay untouched — that is the point of the interface.

## Out of scope
Authentication, real payments, real-time stock, process-death persistence before Phase 4, dependency-injection frameworks (manual construction in the ViewModel is enough for a lab).
