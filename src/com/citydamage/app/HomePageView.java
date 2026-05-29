package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

public class HomePageView {

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Runnable onReport;
    private final Runnable onLogin;
    private final Runnable onUseful;

    private BorderPane root;

    // Stored for refreshTexts()
    private Label navHome, navReports, navUseful;
    private Button loginBtn, ctaBtn;
    private Label heroTitle, heroSubtitle, howTitle, footerLabel;
    private VBox step1Card, step2Card, step3Card;

    public HomePageView(Runnable onReport, Runnable onLogin, Runnable onUseful) {
        this.onReport = onReport;
        this.onLogin  = onLogin;
        this.onUseful = onUseful;
    }

    public BorderPane build() {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setTop(buildNavBar());
        root.setCenter(buildScrollArea());
        return root;
    }

    private StackPane buildNavBar() {
        StackPane navContainer = new StackPane();
        navContainer.getStyleClass().add("navbar-container");
        navContainer.setPrefHeight(60);

        navHome    = navLink(lang.nav_home());
        navReports = navLink(lang.nav_reports());
        navUseful  = navLink(lang.nav_useful());
        navUseful.setOnMouseClicked(e -> { if (onUseful != null) onUseful.run(); });

        HBox links = new HBox(28, navHome, navReports, navUseful);
        links.setAlignment(Pos.CENTER_LEFT);

        HBox controls = buildControls();

        loginBtn = new Button(lang.nav_login());
        loginBtn.getStyleClass().add("login-btn");
        loginBtn.setOnAction(e -> { if (onLogin != null) onLogin.run(); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navItems = new HBox(16, links, spacer, controls, loginBtn);
        navItems.setAlignment(Pos.CENTER);
        navItems.setPadding(new Insets(0, 32, 0, 32));
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
        Label grFlag = new Label("🇬🇷");
        Label enFlag = new Label("🇬🇧");
        grFlag.getStyleClass().add("flag-icon");
        enFlag.getStyleClass().add("flag-icon");

        updateFlagOpacity(grFlag, enFlag, lang.isGreek());
        grFlag.setOnMouseClicked(e -> { lang.setGreek(true);  updateFlagOpacity(grFlag, enFlag, true);  refreshTexts(); });
        enFlag.setOnMouseClicked(e -> { lang.setGreek(false); updateFlagOpacity(grFlag, enFlag, false); refreshTexts(); });

        CheckBox themeToggle = new CheckBox();
        themeToggle.getStyleClass().add("theme-toggle");
        themeToggle.selectedProperty().addListener((obs, was, isLight) -> {
            ColorAdjust ca = new ColorAdjust();
            if (isLight) { root.getStyleClass().add("light-theme");    ca.setBrightness(-0.8); }
            else         { root.getStyleClass().remove("light-theme"); ca.setBrightness(0); }
        });

        HBox box = new HBox(10, grFlag, enFlag, themeToggle);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void updateFlagOpacity(Label gr, Label en, boolean isGr) {
        gr.setOpacity(isGr ? 1.0 : 0.35);
        en.setOpacity(isGr ? 0.35 : 1.0);
    }

    private void refreshTexts() {
        if (navHome    != null) navHome.setText(lang.nav_home());
        if (navReports != null) navReports.setText(lang.nav_reports());
        if (navUseful  != null) navUseful.setText(lang.nav_useful());
        if (loginBtn   != null) loginBtn.setText(lang.nav_login());
        if (heroTitle  != null) heroTitle.setText(lang.hero_title());
        if (heroSubtitle != null) heroSubtitle.setText(lang.hero_subtitle());
        if (ctaBtn     != null) ctaBtn.setText(lang.hero_cta());
        if (howTitle   != null) howTitle.setText(lang.how_title());
        if (footerLabel != null) footerLabel.setText(lang.footer());
        if (step1Card  != null) { /* rebuild steps */ }
        root.setCenter(buildScrollArea());
    }

    private ScrollPane buildScrollArea() {
        VBox content = new VBox(0);
        content.getChildren().addAll(buildHero(), buildHowItWorks(), buildFooter());
        content.getStyleClass().add("main-content-bg");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.getStyleClass().add("main-scroll");
        return sp;
    }

    private StackPane buildHero() {
        heroTitle = new Label(lang.hero_title());
        heroTitle.getStyleClass().add("hero-title");
        heroTitle.setWrapText(true);
        heroTitle.setTextAlignment(TextAlignment.CENTER);

        heroSubtitle = new Label(lang.hero_subtitle());
        heroSubtitle.getStyleClass().add("hero-subtitle");
        heroSubtitle.setWrapText(true);
        heroSubtitle.setTextAlignment(TextAlignment.CENTER);

        ctaBtn = new Button(lang.hero_cta());
        ctaBtn.getStyleClass().add("cta-btn");
        ctaBtn.setOnAction(e -> { if (onReport != null) onReport.run(); });

        VBox card = new VBox(18, heroTitle, heroSubtitle, ctaBtn);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50, 40, 50, 40));
        card.getStyleClass().add("hero-card");
        card.setMaxWidth(620);

        StackPane hero = new StackPane(card);
        hero.getStyleClass().add("hero-section");
        hero.setPrefHeight(460);
        StackPane.setAlignment(card, Pos.CENTER);
        return hero;
    }

    private VBox buildHowItWorks() {
        howTitle = new Label(lang.how_title());
        howTitle.getStyleClass().add("section-title");

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
        Label iconLbl  = new Label(icon);
        iconLbl.getStyleClass().add("step-card-icon");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("step-card-title");
        titleLbl.setWrapText(true);
        titleLbl.setTextAlignment(TextAlignment.CENTER);

        Label descLbl  = new Label(desc);
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

    private HBox buildFooter() {
        footerLabel = new Label(lang.footer());
        footerLabel.getStyleClass().add("footer-text");

        HBox footer = new HBox(footerLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(36, 0, 36, 0));
        footer.getStyleClass().add("footer");
        return footer;
    }
}
