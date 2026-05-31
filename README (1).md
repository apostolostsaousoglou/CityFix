# CityFix

A community-driven civic engagement desktop application that lets citizens report and track city infrastructure damage and public safety hazards. Built with Java and JavaFX, with a fully custom tile-based map renderer, bilingual UI (Greek/English), dark/light theme support, and a PostgreSQL backend.

> Geographically focused on **Patras, Greece**, but the codebase is easily adaptable to any city.

---

## Table of Contents

- [Features](#features)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Building & Running](#building--running)
- [Navigation Flow](#navigation-flow)
- [Pages & Components](#pages--components)
- [Map Engine](#map-engine)
- [Database](#database)
- [Localization](#localization)
- [Theming](#theming)
- [Authentication](#authentication)
- [Known Limitations](#known-limitations)
- [Contributing](#contributing)

---

## Features

| Feature | Details |
|---|---|
| **Damage Reporting** | Report 15+ infrastructure hazard types (potholes, broken pipes, unsafe wires, and more) |
| **Interactive Map** | Native JavaFX tile map — no WebView, no grey tiles. Supports pan, zoom, click-to-select, and live reverse geocoding |
| **Reverse Geocoding** | Nominatim primary + Overpass API fallback for house numbers within 100 m |
| **GPS Location** | Windows PowerShell-based geolocation (`System.Device.Location`) with 10-second timeout |
| **Photo Attachment** | File picker for attaching photographic evidence to a report; stored as `bytea` in PostgreSQL |
| **Bilingual UI** | Full Greek / English support with real-time switching — no restart needed |
| **Dark / Light Theme** | CSS-based theme toggle available on every page |
| **Useful Resources** | Emergency contacts for Police, Fire, DEUAP, DEI, and Municipality |
| **PostgreSQL Backend** | All reports and user accounts persisted in a relational database |
| **Admin Role** | Admins can view all reports, change report status, and view photo evidence |
| **Report Tracking** | Users can view their own submitted reports with status colour coding |

---

## Architecture Overview

CityFix follows a **View-Callback Navigation** pattern:

- Each page is a self-contained `*View` class with a `build()` method that returns a JavaFX node.
- `MainApp` owns the single `Scene` and swaps its root via `scene.setRoot(view.build())`.
- Pages are completely decoupled — they know nothing about `MainApp`. Navigation is wired through `Runnable` callbacks passed into each view's constructor.
- After login, `MainApp` checks `currentUser.isAdmin` to decide whether to present the standard `HomePageView` or redirect an admin to `ReportsPageView`.

```
MainApp
 ├─ showHomePage()       → HomePageView(onReport, onLogin, onUseful, onReports, onLogout, currentUser)
 ├─ showAuthPage()       → AuthDialog(onBack, onSuccess)
 ├─ showReportPage()     → ReportPageView(onBack, onNext, onLogin, onUseful)
 ├─ showReportInfoPage() → ReportInfoPageView(onBack, onSubmit, onUseful, lat, lon, ..., currentUser)
 ├─ showReportsPage()    → ReportsPageView(onHome, onReport, onUseful, currentUser)
 └─ showUsefulPage()     → UsefulPageView(onHome, onReport)
```

---

## Project Structure

```
CityDamageReporter/
├── src/
│   └── com/citydamage/app/
│       ├── MainApp.java            # Entry point, scene/navigation manager
│       ├── DatabaseManager.java    # Singleton: PostgreSQL connection, all DB queries
│       ├── LanguageManager.java    # Singleton, 40+ bilingual UI strings
│       ├── HomePageView.java       # Landing page (hero, how-it-works, footer, settings overlay)
│       ├── AuthDialog.java         # Full-page login / register view
│       ├── ReportPageView.java     # Location picker (map + address form)
│       ├── ReportInfoPageView.java # Damage type, comments, photo upload, submit
│       ├── ReportsPageView.java    # Report list + map (user history / admin panel)
│       ├── TileMapPane.java        # Custom tile map renderer (no WebView)
│       └── UsefulPageView.java     # Emergency contacts directory
├── resources/
│   ├── css/
│   │   └── styles.css              # Unified stylesheet (dark + light themes)
│   ├── postgresql-42.7.5.jar       # JDBC driver
│   ├── bg.jpg                      # Background image
│   ├── bridge.jpg                  # Hero section image
│   └── images/
│       └── logo.png                # Navbar logo
├── bin/                            # Compiled .class files (git-ignored)
├── .env                            # DB credentials (git-ignored)
├── run.bat                         # One-click compile + run (Windows)
└── .classpath                      # Eclipse IDE project config
```

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 21 or later (tested on JDK 25.0.2) |
| OpenJFX SDK | 21 or later (tested on 25.0.2) |
| PostgreSQL | 13 or later |
| OS | Windows (geolocation uses PowerShell) |

Download OpenJFX at [https://openjfx.io](https://openjfx.io) and extract it to a stable path, e.g. `C:\javafx-sdk-25.0.2\`.

---

## Building & Running

### Step 1 — Install Java & JavaFX

1. Install **Java JDK 21+** (e.g. [Temurin](https://adoptium.net/)). Make sure `java` and `javac` are on your `PATH`.
2. Download **OpenJFX SDK 21+** from [https://openjfx.io](https://openjfx.io), choose the Windows SDK zip, and extract it. Example result: `C:\javafx-sdk-25.0.2\`.

### Step 2 — Set up PostgreSQL

1. Install and start **PostgreSQL 13+**.
2. Create a database (e.g. `citydamage`) and the required tables (see [Database](#database)).
3. Create a `.env` file in the project root (`CityDamageReporter/`) with your credentials:

```
DB_URL=jdbc:postgresql://localhost:5432/citydamage
DB_USER=your_username
DB_PASS=your_password
```

> If `.env` is missing the app still launches, but login, registration, and report submission will all fail silently.

### Step 3 — Configure run.bat

Open `run.bat` and set the `JAVAFX_LIB` variable to your OpenJFX `lib` folder:

```bat
set JAVAFX_LIB=C:\javafx-sdk-25.0.2\lib
```

### Step 4 — Run the app

Double-click `run.bat` or run it from a terminal in the project folder:

```bat
run.bat
```

This compiles all source files into `bin/` and launches the application in one step. A JavaFX window titled **"City Damage Reporter"** should appear.

### Manual commands (alternative)

**Compile:**
```bat
javac ^
  --module-path "C:\javafx-sdk-25.0.2\lib" ^
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web ^
  -d bin ^
  -cp "bin;resources\postgresql-42.7.5.jar" ^
  src/com/citydamage/app/*.java
```

**Run:**
```bat
java ^
  --module-path "C:\javafx-sdk-25.0.2\lib" ^
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web ^
  -cp "bin;resources;resources\postgresql-42.7.5.jar" ^
  com.citydamage.app.MainApp
```

> **Important:** The classpath must include `bin`, `resources` (CSS, images), **and** the PostgreSQL JDBC JAR. Omitting `resources` causes a `NullPointerException` at startup when loading the stylesheet.

---

## Navigation Flow

```
HomePageView
 │
 ├─[Login button]──────────────► AuthDialog
 │                                  └─[Back / success]──► HomePageView
 │
 ├─[Report CTA button]
 │   ├─(not logged in)─────────► AuthDialog → then back to ReportPageView
 │   └─(logged in)────────────► ReportPageView
 │                                  ├─[Back]─────────────► HomePageView
 │                                  ├─[Login button]─────► AuthDialog
 │                                  └─[Next]─────────────► ReportInfoPageView
 │                                                             ├─[Back]────► ReportPageView
 │                                                             └─[Submit]──► HomePageView
 │
 ├─[Reports nav link]──────────► ReportsPageView
 │                                  ├─[Home]─────────────► HomePageView
 │                                  └─(admin) status/photo management
 │
 └─[Useful links nav]──────────► UsefulPageView
                                     ├─[Home]─────────────► HomePageView
                                     └─[Report]───────────► ReportPageView
```

---

## Pages & Components

### HomePageView
The landing page. Contains:
- **Navbar** — navigation links, language flags (🇬🇷 / 🇬🇧), dark/light theme toggle, login/logout button
- **Hero section** — title, subtitle, CTA button ("Report a Problem" for users / "See All Reports" for admins)
- **How It Works** — three step cards (Locate → Submit → Track)
- **Settings overlay** — logged-in users can edit their profile (name, email, phone, password) in a modal overlay
- **Logout confirmation** — modal dialog before ending the session
- **Footer**

### AuthDialog
Full-page authentication view (replaces the scene root — not a popup). Contains:
- **Back button** at the top
- **Login card** — email or mobile + password, inline error feedback, async DB lookup
- **Register card** — first name, last name, email (with domain autocomplete), mobile (10-digit validation), password, confirm password, role selector (User / Admin)
- Toggle link to switch between login and register in-place

> **Admin registration:** To create an Administrator account, select **"Διαχειριστής / Admin"** in the role field during registration. An extra field appears asking for the admin access code. Enter:
> ```
> CEIDPATRAS
> ```
> Without this code the registration is rejected. Admins can view all reports, change their status, and access photo evidence.

### ReportPageView
Split-layout location picker:
- **Left panel** — street, number, ZIP code, area dropdown (15 Patras neighbourhoods), "Use My Location" (GPS) and "Select on Map" buttons
- **Right panel** — interactive `TileMapPane` (see [Map Engine](#map-engine))
- Clicking on the map places a crosshair marker; confirming triggers reverse geocoding and auto-fills the address fields

### ReportInfoPageView
Report submission form:
- **Damage type** dropdown — 15 categories (pothole, broken pipe, gas leak, fallen tree, broken sidewalk, etc.)
- **Comments** textarea
- **Photo upload** — system FileChooser with image preview; photo stored as `bytea` in the DB
- **Read-only map preview** showing the confirmed coordinates
- **Submit** — sends to `DatabaseManager.addReport()`, shows success/error Alert

### ReportsPageView
Split-layout report browser:
- **Left panel** — scrollable list of report cards with colour-coded status badge
  - **Regular user:** sees own reports; can delete reports still in `received` status
  - **Admin:** sees all reports; can change status via dropdown (`received → approved → in progress → completed → rejected`) and view photo attachments in a modal dialog
- **Right panel** — `TileMapPane` with a coloured pin for each report; clicking a pin highlights the matching card

### UsefulPageView
Emergency contacts directory with five service cards (Police, Fire Service, DEUAP, DEI, Municipality of Patras). Each card shows phone numbers, address, and a clickable website link opened via `Desktop.getDesktop().browse()`.

---

## Map Engine

`TileMapPane extends Pane` — a fully custom JavaFX tile renderer with **no WebView, no Leaflet, no external map library**.

| Aspect | Implementation |
|---|---|
| **Tile providers** | CartoCDN Voyager (Greek UI) / ArcGIS World Street Map (English UI) |
| **Projection** | Web Mercator (EPSG:3857) |
| **Tile fetching** | 24-thread executor with 20 ms debounce to avoid request storms |
| **Caching** | LRU `LinkedHashMap` (1 200 tiles max) + retained `ImageView` pool (`liveViews`) |
| **Panning** | Drag events translated into world-group `translateX/Y` — zero scene-graph rebuild |
| **Zooming** | Scroll wheel; zoom level clamped to **[3, 19]** |
| **Select mode** | `enableDrag()` → click places crosshair marker; "Use Location" button confirms |
| **Pins** | `addPin(lat, lon, color, label, onClick)` — coloured circles with tooltip and click handler |
| **Reverse geocoding** | Nominatim API → road + postcode + house number |
| **Fallback geocoding** | Overpass API query for nearest `addr:housenumber` node within 100 m |
| **GPS** | PowerShell `System.Device.Location.GeoCoordinateWatcher`, 10-second timeout |
| **Language** | Passes `accept-language` header to Nominatim (`el` or `en`); switches tile provider on language toggle |
| **Threading** | All network I/O on daemon threads; UI updates via `Platform.runLater()` |

---

## Database

`DatabaseManager` is a singleton that manages the PostgreSQL connection and exposes all data access methods.

### Connection

Credentials are read at class-load time from `.env` in the working directory:

```
DB_URL=jdbc:postgresql://localhost:5432/citydamage
DB_USER=postgres
DB_PASS=secret
```

The JDBC driver (`postgresql-42.7.5.jar`) must be on the classpath. The singleton auto-reconnects if the connection is dropped.



### Report status lifecycle

```
received → approved → in progress → completed
                                  ↘ rejected
```

Status colours in the UI: blue (`received`), purple (`approved`), orange (`in progress`), green (`completed`), red (`rejected`).

---

## Localization

All UI text is centralised in the `LanguageManager` singleton:

```java
LanguageManager lang = LanguageManager.getInstance();
lang.isGreek();        // true = Greek, false = English
lang.setGreek(false);  // switch language at runtime
lang.hero_title();     // returns the correct string for the active language
```

Clicking a language flag in the navbar switches the language immediately. Each view stores direct `Label`/`Button` references and calls `refreshTexts()` to update them in-place — no page reload required. The map tile provider also switches on language toggle.

---

## Theming

Dark theme is the default. A pill-shaped toggle in the navbar switches to light theme by:

1. Adding/removing the `.light-theme` CSS class on the root `BorderPane`
2. Applying a `ColorAdjust` brightness effect to the logo `ImageView`

All theme-sensitive colours are defined as paired rules in `styles.css`:

```css
/* dark default */
.root-pane { -fx-background-color: #1a1a2e; }

/* light override */
.root-pane.light-theme { -fx-background-color: #f5f5f5; }
```

---

## Authentication

Authentication is backed by **PostgreSQL** via `DatabaseManager`.

- **Register:** Validates all fields (unique email, unique phone, 10-digit mobile, matching passwords). Stores the password as a **SHA-256 hex digest**. To register as Admin, select the Admin role and enter the secret access code **`CEIDPATRAS`** in the extra field that appears.
- **Login:** Looks up the user by email **or** phone number and compares the SHA-256 hash of the entered password.
- **Session:** The logged-in `UserRecord` (id, firstName, lastName, email, phone, isAdmin) is held in `MainApp.currentUser` for the lifetime of the session.
- **Settings:** Logged-in users can update their profile (name, email, phone, password) from the settings overlay on the home page; changes are persisted to the DB immediately.
- **Logout:** Clears `currentUser` and returns to `HomePageView`.

> **Note:** Passwords registered via the companion web app use Argon2 hashing and are **not** compatible with this Java app's SHA-256 check. Users must register through the Java app to log in here.

---

## Known Limitations

- **Windows-only geolocation** — "Use My Location" invokes PowerShell (`System.Device.Location.GeoCoordinateWatcher`) and will not work on macOS or Linux.
- **Hardcoded OpenJFX path** — `run.bat` references an absolute local path. Each developer must update `JAVAFX_LIB` to match their installation.
- **No location validation on Next** — `ReportPageView` proceeds to the next step even if no point has been selected on the map; it falls back to the default Patras city-centre coordinates (lat 38.2466, lon 21.7346).
- **No GPS error dialog** — when geolocation times out, the button silently resets with no user-facing error message.
- **SHA-256 password storage** — suitable for a university project; a production system should use a proper key-derivation function (bcrypt / Argon2).
- **Manual JSON parsing** — Nominatim and Overpass responses are parsed with string extraction rather than a JSON library, which can be brittle for unexpected response formats.
- **No automated tests** — there is no unit or integration test suite.
- **No logo in repo** — `logo.png` is expected at `resources/images/logo.png`. The app handles a missing file gracefully (prints a warning and continues).

---

## Contributing

1. Fork the repository and create a feature branch off `main`.
2. Keep the view-callback navigation pattern — views must not import or reference `MainApp` directly.
3. Add all new UI strings to `LanguageManager` in both Greek and English.
4. Add all new DB operations to `DatabaseManager`; never open a raw `Connection` in a view class.
5. Test with both language modes, both themes, and both user roles (regular + admin) before opening a pull request.
