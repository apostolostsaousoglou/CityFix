# CityDamageReporter

A JavaFX desktop application for reporting city infrastructure damage.

## Prerequisites

- Java 24 (JDK)
- JavaFX 21 SDK

## Build & Run (Eclipse)

1. Import project as existing Java project
2. Add JavaFX SDK jars to build path (`javafx.controls`, `javafx.fxml`)
3. Add VM arguments: `--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml`
4. Run `MainApp.java`

## Known Limitations

- User accounts are stored in-memory only (cleared on restart)
- GPS location requires Windows Location Services to be enabled
- Tile map requires internet connection to load
