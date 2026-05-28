package com.citydamage.app;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ReportPageView {

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Runnable onBack;
    private final Runnable onNext;
    private final Runnable onLogin;
    private final Runnable onUseful;

    private BorderPane rootRef;
    private ImageView logoView;

    private TextField streetField;
    private TextField numberField;
    private TextField zipField;
    private ComboBox<String> areaCombo;
    private TileMapPane mapPane;

    private VBox cardPanel;
    private Button confirmLocationBtn;
    private StackPane mapStack;

    private double selectedLat = 38.2466;
    private double selectedLng = 21.7346;

    // References for in-place language updates (no map rebuild)
    private Label  cardTitle;
    private Label  streetLabel, numberLabel, zipLabel, areaLabel;
    private Button locationBtn, mapBtn, nextBtn;
    private Label  footerLabel;

    public ReportPageView(Runnable onBack, Runnable onNext, Runnable onLogin, Runnable onUseful) {
        this.onBack   = onBack;
        this.onNext   = onNext;
        this.onLogin = onLogin;
        this.onUseful = onUseful;
    }

    // ─── ROOT ─────────────────────────────────────────────────────────────────

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        rootRef = root;
        root.setTop(buildNavBar());
        root.setCenter(buildSplitLayout());
        root.setBottom(buildFooter());
        return root;
    }

    // ─── SPLIT LAYOUT ─────────────────────────────────────────────────────────

    private HBox buildSplitLayout() {
        // LEFT solid panel
        cardPanel = buildCardPanel();
        cardPanel.getStyleClass().add("report-left-panel");
        cardPanel.setPrefWidth(480);
        cardPanel.setMinWidth(400);
        cardPanel.setMaxWidth(520);

        // Native JavaFX tile map — no WebView, no grey tiles
        mapPane = new TileMapPane();
        mapPane.setLanguage(!lang.isGreek());
        HBox.setHgrow(mapPane, Priority.ALWAYS);

        // Confirm button — shown bottom-right in select mode
        confirmLocationBtn = new Button(lang.isGreek() ? "Χρήση Τοποθεσίας" : "Use Location");
        confirmLocationBtn.getStyleClass().add("confirm-location-btn");
        confirmLocationBtn.setVisible(false);
        confirmLocationBtn.setOpacity(0);
        confirmLocationBtn.setOnAction(e -> exitSelectMode());
        StackPane.setAlignment(confirmLocationBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(confirmLocationBtn, new Insets(0, 24, 24, 0));

        mapStack = new StackPane(mapPane, confirmLocationBtn);
        HBox.setHgrow(mapStack, Priority.ALWAYS);

        mapPane.prefWidthProperty().bind(mapStack.widthProperty());
        mapPane.prefHeightProperty().bind(mapStack.heightProperty());

        HBox split = new HBox(cardPanel, mapStack);
        split.getStyleClass().add("report-split");
        return split;
    }

    // ─── SELECT MODE ──────────────────────────────────────────────────────────

    private void enterSelectMode() {
        // Fade out card
        FadeTransition ft = new FadeTransition(Duration.millis(220), cardPanel);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            cardPanel.setManaged(false);
            cardPanel.setVisible(false);
        });
        ft.play();

        // Show confirm button
        confirmLocationBtn.setVisible(true);
        FadeTransition ftBtn = new FadeTransition(Duration.millis(280), confirmLocationBtn);
        ftBtn.setDelay(Duration.millis(180));
        ftBtn.setToValue(1);
        ftBtn.play();

        mapPane.enableDrag();
    }

    private void exitSelectMode() {
        selectedLat = mapPane.getPickedLat();
        selectedLng = mapPane.getPickedLon();

        // Reverse-geocode the picked point and fill form fields
        reverseGeocode(selectedLat, selectedLng);

        // Hide confirm button
        FadeTransition ftBtn = new FadeTransition(Duration.millis(180), confirmLocationBtn);
        ftBtn.setToValue(0);
        ftBtn.setOnFinished(e -> confirmLocationBtn.setVisible(false));
        ftBtn.play();

        // Show card again
        cardPanel.setVisible(true);
        cardPanel.setManaged(true);
        cardPanel.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(280), cardPanel);
        ft.setToValue(1);
        ft.play();

        mapPane.disableDrag();
    }

    private void reverseGeocode(double lat, double lon) {
        String langCode = lang.isGreek() ? "el" : "en";
        String nominatimUrl = "https://nominatim.openstreetmap.org/reverse?format=jsonv2"
                + "&lat=" + lat + "&lon=" + lon
                + "&zoom=18&addressdetails=1"
                + "&accept-language=" + langCode;
        Thread t = new Thread(() -> {
            try {
                // Step 1: Nominatim reverse geocode → road name + postcode
                HttpURLConnection conn = (HttpURLConnection) new URL(nominatimUrl).openConnection();
                conn.setRequestProperty("User-Agent", "CityDamageReporter/1.0 (educational project)");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.connect();
                if (conn.getResponseCode() != 200) return;
                String json;
                try (InputStream is = conn.getInputStream()) {
                    json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
                String road        = extractJsonField(json, "road");
                String houseNumber = extractJsonField(json, "house_number");
                String postcode    = extractJsonField(json, "postcode");

                // Step 2: if Nominatim gave no house_number (click was on a road, not a building),
                // ask Overpass API for the nearest address node within 100 m.
                if (houseNumber == null) {
                    houseNumber = fetchNearbyHouseNumber(lat, lon);
                }

                final String finalRoad   = road;
                final String finalNumber = houseNumber;
                final String finalZip    = postcode;
                Platform.runLater(() -> {
                    if (finalRoad   != null && streetField != null) streetField.setText(finalRoad);
                    if (finalNumber != null && numberField != null) numberField.setText(finalNumber);
                    if (finalZip    != null && zipField    != null) zipField.setText(finalZip);
                });
            } catch (Exception ignored) { /* user can fill in manually */ }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Queries the Overpass API for the nearest OSM address node within 100 m of the
     * given point that has an addr:housenumber tag. Returns the house number or null.
     */
    private String fetchNearbyHouseNumber(double lat, double lon) {
        try {
            String query = "[out:json][timeout:6];"
                    + "node(around:100," + lat + "," + lon + ")[\"addr:housenumber\"];"
                    + "out 1;";
            byte[] body = ("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8))
                    .getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection)
                    new URL("https://overpass-api.de/api/interpreter").openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", "CityDamageReporter/1.0 (educational project)");
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(7000);
            try (OutputStream os = conn.getOutputStream()) { os.write(body); }
            if (conn.getResponseCode() != 200) return null;
            String json;
            try (InputStream is = conn.getInputStream()) {
                json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            return extractJsonField(json, "addr:housenumber");
        } catch (Exception e) {
            return null;
        }
    }

    /** Extracts a string value from JSON — handles both "key":"val" and "key": "val". */
    private String extractJsonField(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int cursor = idx + search.length();
        // Skip any whitespace between : and the opening quote
        while (cursor < json.length() && json.charAt(cursor) == ' ') cursor++;
        if (cursor >= json.length() || json.charAt(cursor) != '"') return null;
        cursor++; // skip opening quote
        int end = json.indexOf('"', cursor);
        if (end < 0) return null;
        String value = json.substring(cursor, end);
        return value.isEmpty() ? null : value;
    }

    // ─── CARD PANEL ───────────────────────────────────────────────────────────

    private VBox buildCardPanel() {
        boolean gr = lang.isGreek();

        cardTitle = new Label(gr ? "Δήλωση Βλάβης" : "Report Damage");
        cardTitle.getStyleClass().add("report-card-title");
        cardTitle.setMaxWidth(Double.MAX_VALUE);
        cardTitle.setTextAlignment(TextAlignment.CENTER);

        streetField = styledField(gr ? "π.χ. Κορίνθου"       : "e.g. Main St");
        numberField = styledField(gr ? "π.χ. 12, 12Α, -"     : "e.g. 12, 12A");
        zipField    = styledField(gr ? "π.χ. 264 41 ή 26441" : "e.g. 264 41");

        streetLabel = fieldLabel(gr ? "Οδός"    : "Street");
        numberLabel = fieldLabel(gr ? "Αριθμός" : "Number");
        zipLabel    = fieldLabel(gr ? "Τ.Κ."    : "ZIP");

        VBox streetBox = labeledField(streetLabel, streetField);
        VBox numberBox = labeledField(numberLabel, numberField);
        VBox zipBox    = labeledField(zipLabel,    zipField);
        HBox.setHgrow(streetBox, Priority.ALWAYS);
        numberField.setPrefWidth(100);
        zipField.setPrefWidth(120);

        HBox row1 = new HBox(12, streetBox, numberBox, zipBox);

        areaCombo = new ComboBox<>();
        areaCombo.getItems().addAll(areaList(gr));
        areaCombo.getSelectionModel().selectFirst();
        areaCombo.getStyleClass().add("report-combo");
        areaCombo.setMaxWidth(Double.MAX_VALUE);
        areaLabel = fieldLabel(gr ? "Περιοχή" : "Area");
        VBox areaBox = labeledField(areaLabel, areaCombo);

        locationBtn = new Button(gr ? "Τρέχουσα Τοποθεσία" : "Use Current Location");
        locationBtn.getStyleClass().add("location-btn");
        locationBtn.setOnAction(e -> useCurrentLocation());
        HBox locationRow = new HBox(locationBtn);
        locationRow.setAlignment(Pos.CENTER);

        mapBtn = new Button(gr ? "Επιλέξτε στον χάρτη" : "Select on Map");
        mapBtn.getStyleClass().add("map-select-btn");
        mapBtn.setOnAction(e -> enterSelectMode());

        nextBtn = new Button(gr ? "Επόμενο" : "Next");
        nextBtn.getStyleClass().add("cta-btn");
        nextBtn.setOnAction(e -> { if (onNext != null) onNext.run(); });

        HBox.setHgrow(mapBtn, Priority.ALWAYS);
        HBox.setHgrow(nextBtn, Priority.ALWAYS);
        mapBtn.setMaxWidth(Double.MAX_VALUE);
        nextBtn.setMaxWidth(Double.MAX_VALUE);
        HBox bottomRow = new HBox(16, mapBtn, nextBtn);

        VBox card = new VBox(24, cardTitle, row1, areaBox, locationRow, bottomRow);
        card.setPadding(new Insets(40, 36, 40, 36));
        card.setAlignment(Pos.TOP_CENTER);
        return card;
    }

    // ─── NAVBAR ───────────────────────────────────────────────────────────────

    private StackPane buildNavBar() {
        StackPane navContainer = new StackPane();
        navContainer.getStyleClass().add("navbar-container");
        navContainer.setPrefHeight(60);

        Label navHome    = navLink(lang.nav_home());
        Label navReports = navLink(lang.nav_reports());
        Label navUseful  = navLink(lang.nav_useful());
        navHome.setOnMouseClicked(e -> { if (onBack   != null) onBack.run(); });
        navUseful.setOnMouseClicked(e -> { if (onUseful != null) onUseful.run(); });

        HBox links = new HBox(28, navHome, navReports, navUseful);
        links.setAlignment(Pos.CENTER_LEFT);

        HBox controls = buildControls();
        Button loginBtn = new Button(lang.nav_login());
        loginBtn.getStyleClass().add("login-btn");
        loginBtn.setOnAction(e -> { if (onLogin != null) onLogin.run(); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navItems = new HBox(16, links, spacer, controls, loginBtn);
        navItems.setAlignment(Pos.CENTER);
        navItems.setPadding(new Insets(0, 32, 0, 32));
        navItems.getStyleClass().add("navbar");

        logoView = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
            logoView.setImage(logo);
            logoView.setFitHeight(38);
            logoView.setPreserveRatio(true);
            logoView.getStyleClass().add("navbar-logo");
        } catch (Exception ignored) {}

        navContainer.getChildren().addAll(navItems, logoView);
        StackPane.setAlignment(logoView, Pos.CENTER);
        return navContainer;
    }

    private Label navLink(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("nav-link");
        return lbl;
    }

    private HBox buildControls() {
        Label grFlag = new Label("🇬🇷");
        Label enFlag = new Label("🇬🇧");
        grFlag.getStyleClass().add("flag-icon");
        enFlag.getStyleClass().add("flag-icon");

        updateFlagOpacity(grFlag, enFlag, lang.isGreek());
        grFlag.setOnMouseClicked(e -> { lang.setGreek(true);  updateFlagOpacity(grFlag, enFlag, true);  rebuildPage(); });
        enFlag.setOnMouseClicked(e -> { lang.setGreek(false); updateFlagOpacity(grFlag, enFlag, false); rebuildPage(); });

        CheckBox themeToggle = new CheckBox();
        themeToggle.getStyleClass().add("theme-toggle");
        themeToggle.selectedProperty().addListener((obs, was, isLight) -> {
            ColorAdjust ca = new ColorAdjust();
            if (isLight) { rootRef.getStyleClass().add("light-theme");    ca.setBrightness(-0.8); }
            else         { rootRef.getStyleClass().remove("light-theme"); ca.setBrightness(0); }
            if (logoView != null) logoView.setEffect(ca);
        });

        HBox box = new HBox(10, grFlag, enFlag, themeToggle);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void updateFlagOpacity(Label gr, Label en, boolean isGr) {
        gr.setOpacity(isGr ? 1.0 : 0.35);
        en.setOpacity(isGr ? 0.35 : 1.0);
    }

    private void rebuildPage() {
        javafx.application.Platform.runLater(() -> {
            // Rebuild only the navbar (language flags, link text)
            rootRef.setTop(buildNavBar());
            // Update all text in the existing layout — map is untouched
            updateTexts();
        });
    }

    private void updateTexts() {
        boolean gr = lang.isGreek();
        if (cardTitle    != null) cardTitle.setText(gr ? "Δήλωση Βλάβης" : "Report Damage");
        if (streetLabel  != null) streetLabel.setText(gr ? "Οδός"    : "Street");
        if (numberLabel  != null) numberLabel.setText(gr ? "Αριθμός" : "Number");
        if (zipLabel     != null) zipLabel.setText(gr ? "Τ.Κ."    : "ZIP");
        if (areaLabel    != null) areaLabel.setText(gr ? "Περιοχή" : "Area");
        if (streetField  != null) streetField.setPromptText(gr ? "π.χ. Κορίνθου"       : "e.g. Main St");
        if (numberField  != null) numberField.setPromptText(gr ? "π.χ. 12, 12Α, -"     : "e.g. 12, 12A");
        if (zipField     != null) zipField.setPromptText(gr ? "π.χ. 264 41 ή 26441" : "e.g. 264 41");
        if (locationBtn  != null) locationBtn.setText(gr ? "Τρέχουσα Τοποθεσία" : "Use Current Location");
        if (mapBtn       != null) mapBtn.setText(gr ? "Επιλέξτε στον χάρτη" : "Select on Map");
        if (nextBtn      != null) nextBtn.setText(gr ? "Επόμενο" : "Next");
        if (confirmLocationBtn != null) confirmLocationBtn.setText(gr ? "Χρήση Τοποθεσίας" : "Use Location");
        if (footerLabel  != null) footerLabel.setText(lang.footer());
        if (mapPane      != null) mapPane.setLanguage(!lang.isGreek());
        if (areaCombo    != null) {
            int sel = areaCombo.getSelectionModel().getSelectedIndex();
            areaCombo.getItems().setAll(areaList(gr));
            areaCombo.getSelectionModel().select(sel);
        }
    }

    // ─── FOOTER ───────────────────────────────────────────────────────────────

    private HBox buildFooter() {
        footerLabel = new Label(lang.footer());
        footerLabel.getStyleClass().add("footer-text");
        HBox footer = new HBox(footerLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(14, 0, 14, 0));
        footer.getStyleClass().add("footer");
        return footer;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private static java.util.List<String> areaList(boolean gr) {
        if (gr) return java.util.List.of(
            "Επιλέξτε περιοχή...",
            "Κέντρο Πάτρας", "Ψηλαλώνια", "Άνω Πόλη", "Κάτω Πόλη",
            "Αγία Σοφία", "Αγυιά", "Ρίο", "Ζαρουχλέικα", "Ταραμπούρα",
            "Εγλυκάδα", "Περιβόλα", "Μποζαΐτικα", "Οβρυά", "Σούλι", "Κουκούλι"
        );
        return java.util.List.of(
            "Select area...",
            "Patras City Center", "Psilalonia", "Upper Town (Ano Poli)", "Lower Town (Kato Poli)",
            "Agia Sofia", "Agyia", "Rio", "Zarouchleika", "Taraboura",
            "Eglykada", "Perivola", "Bozaitika", "Ovrya", "Souli", "Koukoulí"
        );
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("report-field");
        return tf;
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("report-field-label");
        return lbl;
    }

    private VBox labeledField(Label lbl, Control field) {
        VBox box = new VBox(6, lbl, field);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void useCurrentLocation() {
        if (locationBtn != null) {
            locationBtn.setDisable(true);
            locationBtn.setText(lang.isGreek() ? "Αναζήτηση..." : "Locating...");
        }

        Thread t = new Thread(() -> {
            try {
                // Use Windows Location Platform via PowerShell (.NET System.Device)
                String script =
                    "Add-Type -AssemblyName System.Device;" +
                    "$w = New-Object System.Device.Location.GeoCoordinateWatcher(" +
                    "  [System.Device.Location.GeoPositionAccuracy]::High);" +
                    "$w.Start();" +
                    "$deadline = [DateTime]::Now.AddSeconds(10);" +
                    "while ($w.Position.Location.IsUnknown -and [DateTime]::Now -lt $deadline)" +
                    "  { Start-Sleep -Milliseconds 400; }" +
                    "$c = $w.Position.Location;" +
                    "$w.Stop();" +
                    "$inv = [System.Globalization.CultureInfo]::InvariantCulture;" +
                    "if (-not $c.IsUnknown) {" +
                    "  Write-Output $c.Latitude.ToString($inv);" +
                    "  Write-Output $c.Longitude.ToString($inv)" +
                    "} else { Write-Output 'unknown' }";

                ProcessBuilder pb = new ProcessBuilder(
                        "powershell", "-NoProfile", "-NonInteractive", "-Command", script);
                pb.redirectErrorStream(true);
                Process proc = pb.start();

                String output;
                try (InputStream is = proc.getInputStream()) {
                    output = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
                }
                proc.waitFor();

                String[] lines = output.split("\\r?\\n");
                if (lines.length >= 2 && !lines[0].trim().equals("unknown")) {
                    double lat = Double.parseDouble(lines[0].trim());
                    double lon = Double.parseDouble(lines[1].trim());

                    if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                        final double fLat = lat, fLon = lon;
                        Platform.runLater(() -> {
                            selectedLat = fLat;
                            selectedLng = fLon;
                            if (mapPane != null) mapPane.panTo(fLat, fLon);
                            resetLocationBtn();
                        });
                        reverseGeocode(lat, lon);
                        return;
                    }
                }
            } catch (Exception ignored) {}

            // Fallback: re-enable button
            Platform.runLater(this::resetLocationBtn);
        });
        t.setDaemon(true);
        t.start();
    }

    private void resetLocationBtn() {
        if (locationBtn == null) return;
        locationBtn.setDisable(false);
        locationBtn.setText(lang.isGreek() ? "Τρέχουσα Τοποθεσία" : "Use Current Location");
    }

    // ─── GETTERS ──────────────────────────────────────────────────────────────

    public String getStreet() { return streetField != null ? streetField.getText() : ""; }
    public String getNumber() { return numberField != null ? numberField.getText() : ""; }
    public String getZip()    { return zipField    != null ? zipField.getText()    : ""; }
    public String getArea()   { return areaCombo   != null ? areaCombo.getValue()  : ""; }
    public double getLat()    { return selectedLat; }
    public double getLng()    { return selectedLng; }
}
