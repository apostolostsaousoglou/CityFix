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

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navItems = new HBox(16, links, spacer);
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
        content.getChildren().add(buildHero());
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
}
