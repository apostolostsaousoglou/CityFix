package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class ReportInfoPageView {

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Runnable onBack;
    private final Runnable onSubmit;
    private final Runnable onUseful;
    private final double lat;
    private final double lon;
    private final String street;
    private final String streetNumber;
    private final String zip;
    private final String area;
    private final DatabaseManager.UserRecord currentUser;

    private BorderPane rootRef;
    private ImageView logoView;

    // Card references for language updates
    private Label  cardTitle;
    private Label  damageTypeLabel;
    private Label  commentsLabel;
    private Label  photoLabel;
    private ComboBox<String> damageCombo;
    private TextArea commentsArea;
    private Label     fileNameLabel;
    private Button    chooseFileBtn;
    private Button    clearFileBtn;
    private ImageView photoPreview;
    private Button    backBtn;
    private Button submitBtn;
    private Label  footerLabel;

    private File selectedFile = null;

    public ReportInfoPageView(Runnable onBack, Runnable onSubmit, Runnable onUseful,
                               double lat, double lon,
                               String street, String streetNumber, String zip, String area,
                               DatabaseManager.UserRecord currentUser) {
        this.onBack        = onBack;
        this.onSubmit      = onSubmit;
        this.onUseful      = onUseful;
        this.lat           = lat;
        this.lon           = lon;
        this.street        = street;
        this.streetNumber  = streetNumber;
        this.zip           = zip;
        this.area          = area;
        this.currentUser   = currentUser;
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
        VBox card = buildCardPanel();
        card.getStyleClass().add("report-left-panel");
        card.setPrefWidth(480);
        card.setMinWidth(400);
        card.setMaxWidth(520);

        // Read-only map showing the picked location
        TileMapPane mapPane = new TileMapPane();
        mapPane.setLanguage(!lang.isGreek());
        mapPane.panTo(lat, lon);
        HBox.setHgrow(mapPane, Priority.ALWAYS);

        HBox split = new HBox(card, mapPane);
        split.getStyleClass().add("report-split");
        HBox.setHgrow(mapPane, Priority.ALWAYS);
        return split;
    }

    // ─── CARD PANEL ───────────────────────────────────────────────────────────

    private VBox buildCardPanel() {
        boolean gr = lang.isGreek();

        cardTitle = new Label(gr ? "Πληροφορίες Δήλωσης" : "Report Information");
        cardTitle.getStyleClass().add("report-card-title");
        cardTitle.setMaxWidth(Double.MAX_VALUE);
        cardTitle.setTextAlignment(TextAlignment.CENTER);

        // ── Damage Type ──────────────────────────────────────────────────────
        damageTypeLabel = fieldLabel(gr ? "Τύπος Βλάβης" : "Damage Type");
        damageCombo = new ComboBox<>();
        damageCombo.getItems().addAll(damageTypeList(gr));
        damageCombo.getSelectionModel().selectFirst();
        damageCombo.getStyleClass().add("report-combo");
        damageCombo.setMaxWidth(Double.MAX_VALUE);
        VBox damageBox = labeledField(damageTypeLabel, damageCombo);

        // ── Comments ─────────────────────────────────────────────────────────
        commentsLabel = fieldLabel(gr ? "Σχόλια" : "Comments");
        commentsArea = new TextArea();
        commentsArea.setPromptText(gr
                ? "Περιγράψτε το πρόβλημα εδώ..."
                : "Describe the issue here...");
        commentsArea.getStyleClass().add("report-field");
        commentsArea.setPrefRowCount(4);
        commentsArea.setWrapText(true);
        VBox commentsBox = labeledField(commentsLabel, commentsArea);

        // ── Photo upload ─────────────────────────────────────────────────────
        photoLabel = fieldLabel(gr ? "Φωτογραφία (προαιρετικό)" : "Photo (optional)");
        fileNameLabel = new Label(gr ? "Κανένα αρχείο" : "No file chosen");
        fileNameLabel.getStyleClass().add("report-field-label");
        fileNameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: #94a3b8;");

        photoPreview = new ImageView();
        photoPreview.setFitWidth(160);
        photoPreview.setFitHeight(110);
        photoPreview.setPreserveRatio(true);
        photoPreview.setVisible(false);
        photoPreview.setManaged(false);
        photoPreview.setStyle("-fx-background-radius: 6;");

        chooseFileBtn = new Button(gr ? "Επιλογή Αρχείου" : "Choose File");
        chooseFileBtn.getStyleClass().add("map-select-btn");
        chooseFileBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle(lang.isGreek() ? "Επιλέξτε Φωτογραφία" : "Select Photo");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            lang.isGreek() ? "Εικόνες" : "Images",
                            "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
            javafx.stage.Window owner = null;
            try { owner = chooseFileBtn.getScene().getWindow(); } catch (Exception ignored) {}
            File f = fc.showOpenDialog(owner);
            if (f != null) {
                selectedFile = f;
                fileNameLabel.setText(f.getName());
                fileNameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: #f1f5f9;");
                try (FileInputStream fis = new FileInputStream(f)) {
                    Image img = new Image(fis);
                    photoPreview.setImage(img);
                    photoPreview.setVisible(true);
                    photoPreview.setManaged(true);
                } catch (Exception ignored) {}
            }
        });

        clearFileBtn = new Button(gr ? "Καθαρισμός" : "Clear");
        clearFileBtn.getStyleClass().add("location-btn");
        clearFileBtn.setOnAction(e -> {
            selectedFile = null;
            fileNameLabel.setText(lang.isGreek() ? "Κανένα αρχείο" : "No file chosen");
            fileNameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: #94a3b8;");
            photoPreview.setImage(null);
            photoPreview.setVisible(false);
            photoPreview.setManaged(false);
        });

        HBox fileRow = new HBox(10, chooseFileBtn, clearFileBtn, fileNameLabel);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        VBox photoBox = new VBox(8, photoLabel, fileRow, photoPreview);

        // ── Bottom buttons ────────────────────────────────────────────────────
        backBtn = new Button(gr ? "Επιστροφή" : "Back");
        backBtn.getStyleClass().add("location-btn");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        submitBtn = new Button(gr ? "Αποστολή" : "Submit");
        submitBtn.getStyleClass().add("cta-btn");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> handleSubmit());

        HBox.setHgrow(backBtn, Priority.ALWAYS);
        HBox.setHgrow(submitBtn, Priority.ALWAYS);
        HBox bottomRow = new HBox(16, backBtn, submitBtn);

        VBox card = new VBox(24, cardTitle, damageBox, commentsBox, photoBox, bottomRow);
        card.setPadding(new Insets(40, 36, 40, 36));
        card.setAlignment(Pos.TOP_CENTER);
        return card;
    }

    // ─── NAVBAR ───────────────────────────────────────────────────────────────

    private StackPane buildNavBar() {
        StackPane navContainer = new StackPane();
        navContainer.getStyleClass().add("navbar-container");
        navContainer.setPrefHeight(72);

        logoView = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
            logoView.setImage(logo);
            logoView.setViewport(new javafx.geometry.Rectangle2D(10, 70, 470, 200));
            logoView.setFitHeight(62);
            logoView.setPreserveRatio(true);
            logoView.getStyleClass().add("navbar-logo");
        } catch (Exception ignored) {}

        Label navHome    = navLink(lang.nav_home());
        Label navReports = navLink(lang.nav_reports());
        Label navUseful  = navLink(lang.nav_useful());
        navHome.setOnMouseClicked(e -> { if (onBack   != null) onBack.run(); });
        navUseful.setOnMouseClicked(e -> { if (onUseful != null) onUseful.run(); });

        HBox links = new HBox(28, navHome, navReports, navUseful);
        links.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(links, new Insets(0, 0, 0, 24));

        HBox controls = buildControls();
        Button loginBtn = new Button(lang.nav_login());
        loginBtn.getStyleClass().add("login-btn");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navItems = new HBox(0, logoView, links, spacer, controls, loginBtn);
        navItems.setAlignment(Pos.CENTER_LEFT);
        navItems.setPadding(new Insets(0, 32, 0, 12));
        navItems.getStyleClass().add("navbar");

        navContainer.getChildren().add(navItems);
        return navContainer;
    }

    private Label navLink(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("nav-link");
        return lbl;
    }

    private HBox buildControls() {
        boolean isGr = lang.isGreek();

        ImageView grFlag = new ImageView(new Image("https://flagcdn.com/w40/gr.png", true));
        grFlag.setFitHeight(22); grFlag.setPreserveRatio(true);
        grFlag.setStyle("-fx-cursor: hand;"); grFlag.setOpacity(isGr ? 1.0 : 0.35);

        ImageView enFlag = new ImageView(new Image("https://flagcdn.com/w40/gb.png", true));
        enFlag.setFitHeight(22); enFlag.setPreserveRatio(true);
        enFlag.setStyle("-fx-cursor: hand;"); enFlag.setOpacity(isGr ? 0.35 : 1.0);

        grFlag.setOnMouseClicked(e -> { lang.setGreek(true);  grFlag.setOpacity(1.0); enFlag.setOpacity(0.35); rebuildPage(); });
        enFlag.setOnMouseClicked(e -> { lang.setGreek(false); grFlag.setOpacity(0.35); enFlag.setOpacity(1.0); rebuildPage(); });

        javafx.scene.shape.Rectangle track = new javafx.scene.shape.Rectangle(40, 20);
        track.setArcWidth(20); track.setArcHeight(20);
        javafx.scene.shape.Circle thumb = new javafx.scene.shape.Circle(8);
        boolean[] isLight = {lang.isLightTheme()};
        if (isLight[0]) {
            track.setFill(javafx.scene.paint.Color.web("#e2e8f0")); thumb.setFill(javafx.scene.paint.Color.web("#1e293b"));
            thumb.setTranslateX(10);
            rootRef.getStyleClass().add("light-theme");
        } else {
            track.setFill(javafx.scene.paint.Color.web("#334155")); thumb.setFill(javafx.scene.paint.Color.WHITE);
            thumb.setTranslateX(-10);
        }
        StackPane togglePane = new StackPane(track, thumb);
        togglePane.setStyle("-fx-cursor: hand;");
        togglePane.setPrefSize(40, 20); togglePane.setMaxSize(40, 20);

        togglePane.setOnMouseClicked(e -> {
            isLight[0] = !isLight[0];
            lang.setLightTheme(isLight[0]);
            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(150), thumb);
            tt.setToX(isLight[0] ? 10 : -10); tt.play();
            ColorAdjust ca = new ColorAdjust();
            if (isLight[0]) { track.setFill(javafx.scene.paint.Color.web("#e2e8f0")); thumb.setFill(javafx.scene.paint.Color.web("#1e293b")); rootRef.getStyleClass().add("light-theme"); ca.setBrightness(-0.8); }
            else             { track.setFill(javafx.scene.paint.Color.web("#334155")); thumb.setFill(javafx.scene.paint.Color.WHITE); rootRef.getStyleClass().remove("light-theme"); }
            if (logoView != null) logoView.setEffect(ca);
        });

        HBox box = new HBox(10, grFlag, enFlag, togglePane);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void rebuildPage() {
        javafx.application.Platform.runLater(() -> {
            rootRef.setTop(buildNavBar());
            updateTexts();
        });
    }

    private void updateTexts() {
        boolean gr = lang.isGreek();
        if (cardTitle      != null) cardTitle.setText(gr ? "Πληροφορίες Δήλωσης" : "Report Information");
        if (damageTypeLabel != null) damageTypeLabel.setText(gr ? "Τύπος Βλάβης" : "Damage Type");
        if (commentsLabel  != null) commentsLabel.setText(gr ? "Σχόλια" : "Comments");
        if (photoLabel     != null) photoLabel.setText(gr ? "Φωτογραφία (προαιρετικό)" : "Photo (optional)");
        if (commentsArea   != null) commentsArea.setPromptText(gr
                ? "Περιγράψτε το πρόβλημα εδώ..." : "Describe the issue here...");
        if (chooseFileBtn  != null) chooseFileBtn.setText(gr ? "Επιλογή Αρχείου" : "Choose File");
        if (clearFileBtn   != null) clearFileBtn.setText(gr ? "Καθαρισμός" : "Clear");
        if (fileNameLabel  != null && selectedFile == null)
            fileNameLabel.setText(gr ? "Κανένα αρχείο" : "No file chosen");
        if (backBtn        != null) backBtn.setText(gr ? "Επιστροφή" : "Back");
        if (submitBtn      != null) submitBtn.setText(gr ? "Αποστολή" : "Submit");
        if (footerLabel    != null) footerLabel.setText(lang.footer());
        if (damageCombo    != null) {
            int sel = damageCombo.getSelectionModel().getSelectedIndex();
            damageCombo.getItems().setAll(damageTypeList(gr));
            damageCombo.getSelectionModel().select(sel);
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

    private static List<String> damageTypeList(boolean gr) {
        if (gr) return List.of(
            "Επιλέξτε τύπο βλάβης",
            "Χαλασμένος σωλήνας νερού",
            "Διαρροές αερίου",
            "Χαλασμένα φανάρια",
            "Εκτεθειμένα καλώδια ή ηλεκτρολογικοί κίνδυνοι",
            "Λακκούβες",
            "Πεσμένα δέντρα ή κλαδιά",
            "Σπασμένα πεζοδρόμια",
            "Κατεστραμμένα παγκάκια",
            "Παράνομη απόρριψη απορριμμάτων",
            "Ανοιχτά ή χωρίς κάλυμμα φρεάτια",
            "Σπασμένες παιδικές χαρές",
            "Χαλασμένος δημοτικός φωτισμός",
            "Κατεστραμμένες στάσεις λεωφορείων",
            "Ρωγμές σε τοίχους δημόσιων κτιρίων",
            "Άλλο"
        );
        return List.of(
            "Select damage type",
            "Broken water pipe",
            "Gas leaks",
            "Broken traffic lights",
            "Exposed wires or electrical hazards",
            "Potholes",
            "Fallen trees or branches",
            "Broken sidewalks",
            "Damaged benches",
            "Illegal dumping of garbage",
            "Open or uncovered manholes",
            "Broken playground equipment",
            "Broken municipal lighting",
            "Damaged bus stops",
            "Cracks in public building walls",
            "Other"
        );
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

    // ─── SUBMIT ───────────────────────────────────────────────────────────────

    private void handleSubmit() {
        boolean gr = lang.isGreek();
        String type        = damageCombo  != null ? damageCombo.getValue()  : "";
        String description = commentsArea != null ? commentsArea.getText()  : "";

        if (type == null || type.isEmpty() || type.equals("Επιλέξτε τύπο βλάβης") || type.equals("Select damage type")) {
            showAlert(gr ? "Επιλέξτε τύπο βλάβης." : "Please select a damage type.");
            return;
        }
        if (currentUser == null) {
            showAlert(gr ? "Συνδεθείτε πρώτα για να υποβάλετε αναφορά." : "Please log in before submitting a report.");
            return;
        }

        String phone = currentUser.phone;
        byte[] photoBytes = null;
        if (selectedFile != null) {
            try { photoBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath()); }
            catch (Exception ignored) {}
        }

        final byte[] finalPhoto = photoBytes;
        submitBtn.setDisable(true);
        Thread t = new Thread(() -> {
            boolean ok = DatabaseManager.getInstance().addReport(
                type, description, street, streetNumber, area, zip, lat, lon, phone, finalPhoto
            );
            javafx.application.Platform.runLater(() -> {
                submitBtn.setDisable(false);
                if (ok) {
                    showAlert(gr ? "Η αναφορά σας υποβλήθηκε επιτυχώς!" : "Report submitted successfully!");
                    if (onSubmit != null) onSubmit.run();
                } else {
                    showAlert(gr ? "Σφάλμα κατά την υποβολή. Δοκιμάστε ξανά." : "Submission failed. Please try again.");
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION, message,
            javafx.scene.control.ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // ─── GETTERS ──────────────────────────────────────────────────────────────

    public String getDamageType() { return damageCombo != null ? damageCombo.getValue() : ""; }
    public String getComments()   { return commentsArea != null ? commentsArea.getText() : ""; }
    public File   getPhotoFile()  { return selectedFile; }
}
