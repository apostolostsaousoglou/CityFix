package com.citydamage.app;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

public class HomePageView {

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Runnable onReport;
    private final Runnable onLogin;
    private final Runnable onLogout;
    private final Runnable onUseful;
    private final Runnable onReports;
    private final DatabaseManager.UserRecord currentUser;
    private final java.util.function.Consumer<DatabaseManager.UserRecord> onUserUpdated;
    private BorderPane root;
    private ImageView logoView;

    public HomePageView(Runnable onReport, Runnable onLogin, Runnable onUseful, Runnable onReports,
                        Runnable onLogout, DatabaseManager.UserRecord currentUser,
                        java.util.function.Consumer<DatabaseManager.UserRecord> onUserUpdated) {
        this.onReport      = onReport;
        this.onLogin       = onLogin;
        this.onLogout      = onLogout;
        this.onUseful      = onUseful;
        this.onReports     = onReports;
        this.currentUser   = currentUser;
        this.onUserUpdated = onUserUpdated;
    }

    private Label navHome, navReports, navUseful;
    private Button loginBtn, ctaBtn;
    private Label heroTitle, heroSubtitle, howTitle, footerLabel;
    private VBox step1Card, step2Card, step3Card;
    private StackPane rootStack;

    public StackPane build() {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setStyle("-fx-background-color: transparent;");
        root.setTop(buildNavBar());
        root.setCenter(buildScrollArea());

        ImageView bgView = new ImageView();
        try { bgView.setImage(new javafx.scene.image.Image(
                new java.io.File("resources/bg.jpg").toURI().toString())); }
        catch (Exception ignored) {}
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);

        rootStack = new StackPane(bgView, root);
        bgView.fitWidthProperty().bind(rootStack.widthProperty());
        bgView.fitHeightProperty().bind(rootStack.heightProperty());
        return rootStack;
    }

    // ─── NAVBAR ────────────────────────────────────────────────────────────────

    private StackPane buildNavBar() {
        StackPane navContainer = new StackPane();
        navContainer.getStyleClass().add("navbar-container");
        navContainer.setPrefHeight(72);

        // Far-left logo
        logoView = new ImageView();
        try {
            Image logoImg = new Image(getClass().getResourceAsStream("/images/logo.png"));
            logoView.setImage(logoImg);
            logoView.setViewport(new javafx.geometry.Rectangle2D(10, 70, 470, 200));
            logoView.setFitHeight(62);
            logoView.setPreserveRatio(true);
            logoView.getStyleClass().add("navbar-logo");
        } catch (Exception e) {
            System.err.println("Logo not found at /images/logo.png");
        }

        // Nav links
        navHome    = navLink(lang.nav_home());
        navReports = navLink(lang.nav_reports());
        navUseful  = navLink(lang.nav_useful());
        navReports.setOnMouseClicked(e -> { if (onReports != null) onReports.run(); });
        navUseful.setOnMouseClicked(e  -> { if (onUseful  != null) onUseful.run();  });
        HBox links = new HBox(28, navHome, navReports, navUseful);
        links.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(links, new Insets(0, 0, 0, 24));

        // Right: flags + toggle + login button
        HBox controls = buildControls();
        if (currentUser != null) {
            loginBtn = new Button(lang.isGreek()
                    ? "Έξοδος (" + currentUser.firstName + ")"
                    : "Logout (" + currentUser.firstName + ")");
            loginBtn.getStyleClass().add("login-btn");
            loginBtn.setOnAction(e -> showLogoutConfirm());
        } else {
            loginBtn = new Button(lang.nav_login());
            loginBtn.getStyleClass().add("login-btn");
            loginBtn.setOnAction(e -> { if (onLogin != null) onLogin.run(); });
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox rightItems = new HBox(8, controls, loginBtn);
        if (currentUser != null) {
            Button settingsBtn = new Button("⚙");
            settingsBtn.getStyleClass().add("login-btn");
            settingsBtn.setOnAction(e -> showSettingsOverlay());
            rightItems.getChildren().add(settingsBtn);
        }
        rightItems.setAlignment(Pos.CENTER);

        HBox navItems = new HBox(0, logoView, links, spacer, rightItems);
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

        grFlag.setOnMouseClicked(e -> { lang.setGreek(true);  grFlag.setOpacity(1.0); enFlag.setOpacity(0.35); refreshTexts(); });
        enFlag.setOnMouseClicked(e -> { lang.setGreek(false); grFlag.setOpacity(0.35); enFlag.setOpacity(1.0); refreshTexts(); });

        // Sliding toggle switch
        Rectangle track = new Rectangle(40, 20);
        track.setArcWidth(20); track.setArcHeight(20);
        Circle thumb = new Circle(8);
        boolean[] isLight = {lang.isLightTheme()};
        // Apply persisted theme state on build
        if (isLight[0]) {
            track.setFill(Color.web("#e2e8f0")); thumb.setFill(Color.web("#1e293b"));
            thumb.setTranslateX(10);
            root.getStyleClass().add("light-theme");
        } else {
            track.setFill(Color.web("#334155")); thumb.setFill(Color.WHITE);
            thumb.setTranslateX(-10);
        }
        StackPane togglePane = new StackPane(track, thumb);
        togglePane.setStyle("-fx-cursor: hand;");
        togglePane.setPrefSize(40, 20); togglePane.setMaxSize(40, 20);

        togglePane.setOnMouseClicked(e -> {
            isLight[0] = !isLight[0];
            lang.setLightTheme(isLight[0]);
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), thumb);
            tt.setToX(isLight[0] ? 10 : -10); tt.play();
            ColorAdjust ca = new ColorAdjust();
            if (isLight[0]) { track.setFill(Color.web("#e2e8f0")); thumb.setFill(Color.web("#1e293b")); root.getStyleClass().add("light-theme"); ca.setBrightness(-0.8); }
            else             { track.setFill(Color.web("#334155")); thumb.setFill(Color.WHITE); root.getStyleClass().remove("light-theme"); }
            if (logoView != null) logoView.setEffect(ca);
        });

        HBox box = new HBox(10, grFlag, enFlag, togglePane);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    // ─── SETTINGS OVERLAY ─────────────────────────────────────────────────────

    private void showSettingsOverlay() {
        boolean gr = lang.isGreek();

        // ── Fields ──────────────────────────────────────────────────────────
        TextField firstField   = settingsField(currentUser.firstName);
        TextField lastField    = settingsField(currentUser.lastName);
        TextField emailField   = settingsField(currentUser.email);
        TextField phoneField   = settingsField(currentUser.phone);
        PasswordField passField    = new PasswordField(); passField.getStyleClass().add("auth-field");
        PasswordField confirmField = new PasswordField(); confirmField.getStyleClass().add("auth-field");

        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        errorLbl.setWrapText(true);

        // ── Buttons ─────────────────────────────────────────────────────────
        Button cancelBtn = new Button(gr ? "Ακύρωση" : "Cancel");
        cancelBtn.getStyleClass().add("location-btn");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);

        Button saveBtn = new Button(gr ? "Αποθήκευση" : "Save");
        saveBtn.getStyleClass().add("cta-btn");
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(cancelBtn, Priority.ALWAYS);
        HBox.setHgrow(saveBtn, Priority.ALWAYS);
        HBox btnRow = new HBox(12, cancelBtn, saveBtn);

        // ── Card ────────────────────────────────────────────────────────────
        Label title = new Label(gr ? "Ρυθμίσεις" : "Settings");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        VBox card = new VBox(12,
            title,
            settingsRow(gr ? "Όνομα:"                    : "First Name:",        firstField),
            settingsRow(gr ? "Επώνυμο:"                  : "Last Name:",         lastField),
            settingsRow(gr ? "Email:"                    : "Email:",             emailField),
            settingsRow(gr ? "Τηλέφωνο:"                 : "Phone:",             phoneField),
            settingsRow(gr ? "Νέος Κωδικός:"             : "New Password:",      passField),
            settingsRow(gr ? "Επιβεβαίωση Νέου Κωδικού:" : "Confirm Password:",  confirmField),
            errorLbl,
            btnRow
        );
        card.setPadding(new Insets(28, 36, 28, 36));
        card.setMaxSize(400, javafx.scene.layout.Region.USE_PREF_SIZE);
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12; " +
                      "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.6),20,0,0,4);");

        // ── Dim overlay ─────────────────────────────────────────────────────
        StackPane overlay = new StackPane(card);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        StackPane.setAlignment(card, Pos.CENTER);
        rootStack.getChildren().add(overlay);

        // Close on background click
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) rootStack.getChildren().remove(overlay);
        });

        cancelBtn.setOnAction(e -> rootStack.getChildren().remove(overlay));

        saveBtn.setOnAction(e -> {
            String first   = firstField.getText().trim();
            String last    = lastField.getText().trim();
            String email   = emailField.getText().trim();
            String phone   = phoneField.getText().trim();
            String pass    = passField.getText();
            String confirm = confirmField.getText();

            if (first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                errorLbl.setText(gr ? "Συμπλήρωσε όλα τα υποχρεωτικά πεδία."
                                    : "Please fill in all required fields.");
                return;
            }
            if (!pass.isEmpty() && !pass.equals(confirm)) {
                errorLbl.setText(gr ? "Οι κωδικοί δεν ταιριάζουν."
                                    : "Passwords do not match.");
                return;
            }

            saveBtn.setDisable(true);
            Thread t = new Thread(() -> {
                DatabaseManager.UserRecord updated = DatabaseManager.getInstance()
                        .updateUserInfo(currentUser.id, first, last, email, phone,
                                        pass.isEmpty() ? null : pass, currentUser.isAdmin);
                javafx.application.Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    if (updated != null) {
                        // Update session user in MainApp via callback
                        if (onUserUpdated != null) onUserUpdated.accept(updated);
                        rootStack.getChildren().remove(overlay);
                    } else {
                        errorLbl.setText(gr ? "Σφάλμα αποθήκευσης. Δοκιμάστε ξανά."
                                            : "Save failed. Please try again.");
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        });
    }

    private void showLogoutConfirm() {
        boolean gr = lang.isGreek();

        Label title = new Label(gr ? "Επιβεβαίωση Εξόδου" : "Confirm Logout");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        Label msg = new Label(gr ? "Είστε σίγουροι ότι θέλετε να αποσυνδεθείτε;"
                                 : "Are you sure you want to log out?");
        msg.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
        msg.setWrapText(true);

        Button cancelBtn = new Button(gr ? "Ακύρωση" : "Cancel");
        cancelBtn.getStyleClass().add("location-btn");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);

        Button logoutBtn = new Button(gr ? "Έξοδος" : "Log Out");
        logoutBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px; " +
                "-fx-font-weight: bold;");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(cancelBtn, Priority.ALWAYS);
        HBox.setHgrow(logoutBtn, Priority.ALWAYS);
        HBox btnRow = new HBox(12, cancelBtn, logoutBtn);

        VBox card = new VBox(12, title, msg, btnRow);
        card.setPadding(new Insets(20, 28, 20, 28));
        card.setMaxWidth(340);
        card.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.6),20,0,0,4);");

        StackPane overlay = new StackPane(card);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        rootStack.getChildren().add(overlay);

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) rootStack.getChildren().remove(overlay);
        });
        cancelBtn.setOnAction(e -> rootStack.getChildren().remove(overlay));
        logoutBtn.setOnAction(e -> {
            rootStack.getChildren().remove(overlay);
            if (onLogout != null) onLogout.run();
        });
    }

    private TextField settingsField(String value) {
        TextField tf = new TextField(value);
        tf.getStyleClass().add("auth-field");
        return tf;
    }

    private VBox settingsRow(String labelText, javafx.scene.control.Control field) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        field.setMaxWidth(Double.MAX_VALUE);
        return new VBox(4, lbl, field);
    }

    // ─── SCROLL AREA ───────────────────────────────────────────────────────────

    private ScrollPane buildScrollArea() {
        VBox content = new VBox(0);
        content.getChildren().addAll(buildHero(), buildHowItWorks(), buildFooter());
        content.setStyle("-fx-background-color: rgba(4,4,18,0.52);");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return sp;
    }

    // ─── HERO ──────────────────────────────────────────────────────────────────

    private StackPane buildHero() {
        StackPane hero = new StackPane();
        hero.setPrefHeight(460);
        hero.setStyle("-fx-background-color: #0f172a;");

        // High-quality ImageView background (sharper than CSS background-image)
        javafx.scene.image.ImageView heroBg = new javafx.scene.image.ImageView();
        try {
            heroBg.setImage(new javafx.scene.image.Image(
                    new java.io.File("resources/bridge.jpg").toURI().toString()));
        } catch (Exception ignored) {}
        heroBg.setPreserveRatio(false);
        heroBg.setSmooth(true);
        heroBg.setCache(true);
        heroBg.setCacheHint(javafx.scene.CacheHint.QUALITY);
        heroBg.fitWidthProperty().bind(hero.widthProperty());
        heroBg.fitHeightProperty().bind(hero.heightProperty());

        // Dark gradient overlay so text stays readable
        javafx.scene.layout.Region overlay = new javafx.scene.layout.Region();
        overlay.setStyle("-fx-background-color: linear-gradient(" +
                "to bottom, rgba(0,0,0,0.35) 0%, rgba(0,0,0,0.55) 100%);");

        VBox card = buildHeroCard();
        hero.getChildren().addAll(heroBg, overlay, card);
        StackPane.setAlignment(card, Pos.CENTER);
        return hero;
    }

    private VBox buildHeroCard() {
        heroTitle = new Label(lang.hero_title());
        heroTitle.getStyleClass().add("hero-title");
        heroTitle.setWrapText(true);
        heroTitle.setTextAlignment(TextAlignment.CENTER);

        heroSubtitle = new Label(lang.hero_subtitle());
        heroSubtitle.getStyleClass().add("hero-subtitle");
        heroSubtitle.setWrapText(true);
        heroSubtitle.setTextAlignment(TextAlignment.CENTER);

        boolean isAdmin = currentUser != null && currentUser.isAdmin;
        ctaBtn = new Button(isAdmin
                ? (lang.isGreek() ? "Δες Όλες τις Αναφορές" : "See All Reports")
                : lang.hero_cta());
        ctaBtn.getStyleClass().add("cta-btn");
        ctaBtn.setOnAction(e -> {
            if (isAdmin) { if (onReports != null) onReports.run(); }
            else onReportClicked();
        });

        VBox card = new VBox(18, heroTitle, heroSubtitle, ctaBtn);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50, 40, 50, 40));
        card.getStyleClass().add("hero-card");
        card.setMaxWidth(620);
        return card;
    }

    // ─── HOW IT WORKS ──────────────────────────────────────────────────────────

    private VBox buildHowItWorks() {
        howTitle = new Label(lang.how_title());
        howTitle.getStyleClass().add("section-title");

        // 3 cards in a single row — exactly as screenshots 1 & 2 show
        step1Card = buildStepCard("📸", lang.step1_title(), lang.step1_desc());
        step2Card = buildStepCard("📝", lang.step2_title(), lang.step2_desc());
        step3Card = buildStepCard("📍", lang.step3_title(), lang.step3_desc());

        HBox cardsRow = new HBox(30, step1Card, step2Card, step3Card);
        cardsRow.setAlignment(Pos.CENTER);

        VBox section = new VBox(55, howTitle, cardsRow);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(90, 60, 120, 60));
        section.getStyleClass().add("how-section");
        return section;
    }

    private VBox buildStepCard(String icon, String title, String desc) {
        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("step-card-icon");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("step-card-title");
        titleLbl.setWrapText(true);
        titleLbl.setTextAlignment(TextAlignment.CENTER);

        Label descLbl = new Label(desc);
        descLbl.getStyleClass().add("step-card-desc");
        descLbl.setWrapText(true);
        descLbl.setTextAlignment(TextAlignment.CENTER);

        VBox card = new VBox(14, iconLbl, titleLbl, descLbl);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(38, 28, 38, 28));
        card.getStyleClass().add("step-card");
        card.setPrefSize(340, 220);
        card.setMaxWidth(360);
        return card;
    }

    // ─── FOOTER ────────────────────────────────────────────────────────────────

    private HBox buildFooter() {
        footerLabel = new Label(lang.footer());
        footerLabel.getStyleClass().add("footer-text");

        HBox footer = new HBox(footerLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(36, 0, 36, 0));
        footer.getStyleClass().add("footer");
        return footer;
    }

    // ─── REFRESH ───────────────────────────────────────────────────────────────

    private void refreshTexts() {
        navHome.setText(lang.nav_home());
        navReports.setText(lang.nav_reports());
        navUseful.setText(lang.nav_useful());
        if (currentUser != null) {
            loginBtn.setText(lang.isGreek()
                    ? "Έξοδος (" + currentUser.firstName + ")"
                    : "Logout (" + currentUser.firstName + ")");
        } else {
            loginBtn.setText(lang.nav_login());
        }
        heroTitle.setText(lang.hero_title());
        heroSubtitle.setText(lang.hero_subtitle());
        boolean adminRefresh = currentUser != null && currentUser.isAdmin;
        ctaBtn.setText(adminRefresh
                ? (lang.isGreek() ? "Δες Όλες τις Αναφορές" : "See All Reports")
                : lang.hero_cta());
        howTitle.setText(lang.how_title());
        footerLabel.setText(lang.footer());

        // Refresh step card labels
        refreshStepCard(step1Card, lang.step1_title(), lang.step1_desc());
        refreshStepCard(step2Card, lang.step2_title(), lang.step2_desc());
        refreshStepCard(step3Card, lang.step3_title(), lang.step3_desc());
    }

    /** Updates the title and description labels inside a step card VBox. */
    private void refreshStepCard(VBox card, String title, String desc) {
        if (card == null) return;
        // children order: 0=icon, 1=title, 2=desc
        ((Label) card.getChildren().get(1)).setText(title);
        ((Label) card.getChildren().get(2)).setText(desc);
    }

    private void onReportClicked() {
        if (onReport != null) onReport.run();
    }
}