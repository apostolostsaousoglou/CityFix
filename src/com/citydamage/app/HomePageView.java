package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

public class HomePageView {

    private final Runnable onReport;
    private final Runnable onLogin;
    private final Runnable onUseful;

    public HomePageView(Runnable onReport, Runnable onLogin, Runnable onUseful) {
        this.onReport = onReport;
        this.onLogin  = onLogin;
        this.onUseful = onUseful;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setTop(buildNavBar());
        root.setCenter(buildScrollArea());
        return root;
    }

    private StackPane buildNavBar() {
        StackPane navContainer = new StackPane();
        navContainer.getStyleClass().add("navbar-container");
        navContainer.setPrefHeight(60);

        Label navHome    = navLink("Home");
        Label navReports = navLink("Reports");
        Label navUseful  = navLink("Useful");
        navUseful.setOnMouseClicked(e -> { if (onUseful != null) onUseful.run(); });

        HBox links = new HBox(28, navHome, navReports, navUseful);
        links.setAlignment(Pos.CENTER_LEFT);

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("login-btn");
        loginBtn.setOnAction(e -> { if (onLogin != null) onLogin.run(); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navItems = new HBox(16, links, spacer, loginBtn);
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
        Label heroTitle = new Label("Report City Damage");
        heroTitle.getStyleClass().add("hero-title");
        heroTitle.setWrapText(true);
        heroTitle.setTextAlignment(TextAlignment.CENTER);

        Label heroSubtitle = new Label("Help improve your city by reporting infrastructure issues");
        heroSubtitle.getStyleClass().add("hero-subtitle");
        heroSubtitle.setWrapText(true);
        heroSubtitle.setTextAlignment(TextAlignment.CENTER);

        Button ctaBtn = new Button("Report a Problem");
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
        Label howTitle = new Label("How It Works");
        howTitle.getStyleClass().add("section-title");

        VBox step1Card = buildStepCard("📸", "Locate",  "Find the exact location on the map");
        VBox step2Card = buildStepCard("📝", "Submit",  "Describe the damage and submit your report");
        VBox step3Card = buildStepCard("📍", "Track",   "Follow the progress of your report");

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
        Label footerLabel = new Label("CityDamageReporter - Helping improve city infrastructure");
        footerLabel.getStyleClass().add("footer-text");

        HBox footer = new HBox(footerLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(36, 0, 36, 0));
        footer.getStyleClass().add("footer");
        return footer;
    }
}
