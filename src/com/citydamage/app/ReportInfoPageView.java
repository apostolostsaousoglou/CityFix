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
import java.util.List;

public class ReportInfoPageView {

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Runnable onBack;
    private final Runnable onSubmit;
    private final Runnable onUseful;
    private final double lat;
    private final double lon;

    private BorderPane rootRef;
    private ImageView logoView;

    private Label  cardTitle;
    private Label  damageTypeLabel;
    private Label  commentsLabel;
    private Label  photoLabel;
    private ComboBox<String> damageCombo;
    private TextArea commentsArea;
    private Label  fileNameLabel;
    private Button chooseFileBtn;
    private Button clearFileBtn;
    private Button backBtn;
    private Button submitBtn;
    private Label  statusLabel;   // shows submit result
    private Label  footerLabel;

    private File selectedFile = null;

    public ReportInfoPageView(Runnable onBack, Runnable onSubmit, Runnable onUseful,
                               double lat, double lon) {
        this.onBack   = onBack;
        this.onSubmit = onSubmit;
        this.onUseful = onUseful;
        this.lat      = lat;
        this.lon      = lon;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        rootRef = root;
        root.setTop(buildNavBar());
        root.setCenter(buildSplitLayout());
        root.setBottom(buildFooter());
        return root;
    }

    private HBox buildSplitLayout() {
        VBox card = buildCardPanel();
        card.getStyleClass().add("report-left-panel");
        card.setPrefWidth(480);
        card.setMinWidth(400);
        card.setMaxWidth(520);

        TileMapPane mapPane = new TileMapPane();
        mapPane.setLanguage(!lang.isGreek());
        mapPane.panTo(lat, lon);
        HBox.setHgrow(mapPane, Priority.ALWAYS);

        HBox split = new HBox(card, mapPane);
        split.getStyleClass().add("report-split");
        HBox.setHgrow(mapPane, Priority.ALWAYS);
        return split;
    }

    private VBox buildCardPanel() {
        boolean gr = lang.isGreek();

        cardTitle = new Label(gr ? "Πληροφορίες Δήλωσης" : "Report Information");
        cardTitle.getStyleClass().add("report-card-title");
        cardTitle.setMaxWidth(Double.MAX_VALUE);
        cardTitle.setTextAlignment(TextAlignment.CENTER);

        damageTypeLabel = fieldLabel(gr ? "Τύπος Βλάβης" : "Damage Type");
        damageCombo = new ComboBox<>();
        damageCombo.getItems().addAll(damageTypeList(gr));
        damageCombo.getSelectionModel().selectFirst();
        damageCombo.getStyleClass().add("report-combo");
        damageCombo.setMaxWidth(Double.MAX_VALUE);
        VBox damageBox = labeledField(damageTypeLabel, damageCombo);

        commentsLabel = fieldLabel(gr ? "Σχόλια" : "Comments");
        commentsArea = new TextArea();
        commentsArea.setPromptText(gr ? "Περιγράψτε το πρόβλημα εδώ..." : "Describe the issue here...");
        commentsArea.getStyleClass().add("report-field");
        commentsArea.setPrefRowCount(4);
        commentsArea.setWrapText(true);
        VBox commentsBox = labeledField(commentsLabel, commentsArea);

        photoLabel = fieldLabel(gr ? "Φωτογραφία (προαιρετικό)" : "Photo (optional)");
        fileNameLabel = new Label(gr ? "Κανένα αρχείο" : "No file chosen");
        fileNameLabel.getStyleClass().add("report-field-label");
        fileNameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: #94a3b8;");

        chooseFileBtn = new Button(gr ? "Επιλογή Αρχείου" : "Choose File");
        chooseFileBtn.getStyleClass().add("map-select-btn");
        chooseFileBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle(lang.isGreek() ? "Επιλέξτε Φωτογραφία" : "Select Photo");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            lang.isGreek() ? "Εικόνες" : "Images",
                            "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File f = fc.showOpenDialog(rootRef.getScene().getWindow());
            if (f != null) {
                selectedFile = f;
                fileNameLabel.setText(f.getName());
                fileNameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: #f1f5f9;");
            }
        });

        clearFileBtn = new Button(gr ? "Καθαρισμός" : "Clear");
        clearFileBtn.getStyleClass().add("location-btn");
        clearFileBtn.setOnAction(e -> {
            selectedFile = null;
            fileNameLabel.setText(lang.isGreek() ? "Κανένα αρχείο" : "No file chosen");
            fileNameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: #94a3b8;");
        });

        HBox fileRow = new HBox(10, chooseFileBtn, clearFileBtn, fileNameLabel);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        VBox photoBox = new VBox(6, photoLabel, fileRow);

        backBtn = new Button(gr ? "Επιστροφή" : "Back");
        backBtn.getStyleClass().add("location-btn");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        submitBtn = new Button(gr ? "Αποστολή" : "Submit");
        submitBtn.getStyleClass().add("cta-btn");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (onSubmit != null) onSubmit.run();
            if (statusLabel != null) {
                statusLabel.setText(lang.isGreek() ? "✓ Η δήλωση στάλθηκε!" : "✓ Report submitted!");
                statusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 13px;");
            }
        });

        // Status label for submit feedback
        statusLabel = new Label("");
        statusLabel.setWrapText(true);

        HBox.setHgrow(backBtn, Priority.ALWAYS);
        HBox.setHgrow(submitBtn, Priority.ALWAYS);
        HBox bottomRow = new HBox(16, backBtn, submitBtn);

        VBox card = new VBox(24, cardTitle, damageBox, commentsBox, photoBox, bottomRow, statusLabel);
        card.setPadding(new Insets(40, 36, 40, 36));
        card.setAlignment(Pos.TOP_CENTER);
        return card;
    }

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

    private HBox buildFooter() {
        footerLabel = new Label(lang.footer());
        footerLabel.getStyleClass().add("footer-text");
        HBox footer = new HBox(footerLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(14, 0, 14, 0));
        footer.getStyleClass().add("footer");
        return footer;
    }

    private static List<String> damageTypeList(boolean gr) {
        if (gr) return List.of(
            "Επιλέξτε τύπο βλάβης",
            "Χαλασμένος σωλήνας νερού", "Διαρροές αερίου", "Χαλασμένα φανάρια",
            "Εκτεθειμένα καλώδια ή ηλεκτρολογικοί κίνδυνοι", "Λακκούβες",
            "Πεσμένα δέντρα ή κλαδιά", "Σπασμένα πεζοδρόμια", "Κατεστραμμένα παγκάκια",
            "Παράνομη απόρριψη απορριμμάτων", "Ανοιχτά ή χωρίς κάλυμμα φρεάτια",
            "Σπασμένες παιδικές χαρές", "Χαλασμένος δημοτικός φωτισμός",
            "Κατεστραμμένες στάσεις λεωφορείων", "Ρωγμές σε τοίχους δημόσιων κτιρίων", "Άλλο"
        );
        return List.of(
            "Select damage type",
            "Broken water pipe", "Gas leaks", "Broken traffic lights",
            "Exposed wires or electrical hazards", "Potholes",
            "Fallen trees or branches", "Broken sidewalks", "Damaged benches",
            "Illegal dumping of garbage", "Open or uncovered manholes",
            "Broken playground equipment", "Broken municipal lighting",
            "Damaged bus stops", "Cracks in public building walls", "Other"
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

    public String getDamageType() { return damageCombo  != null ? damageCombo.getValue()  : ""; }
    public String getComments()   { return commentsArea != null ? commentsArea.getText()   : ""; }
    public File   getPhotoFile()  { return selectedFile; }
}
