package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;

public class UsefulPageView {

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Runnable onHome;
    private final Runnable onReport;

    private BorderPane rootRef;
    private ImageView  logoView;

    public UsefulPageView(Runnable onHome, Runnable onReport) {
        this.onHome   = onHome;
        this.onReport = onReport;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        rootRef = root;
        root.setTop(buildNavBar());
        root.setCenter(buildCenter());
        return root;
    }

    private StackPane buildCenter() {
        VBox col = new VBox(22,
            card("Police",              "https://www.astynomia.gr/",     new String[][]{
                {"1st Police Department", "261 440 9750", "138 Panepistimiou St., Anthoupoli, 264 43 Patras"},
                {"2nd Police Department", "261 089 5282", "95 Ermou St., 26110 Patras"},
                {"3rd Police Department", "261 034 4850", "Filippou & Olympiados – Nikopoleos, 26332 Patras"},
                {"Traffic Department",    "261 440 9770", "138 Panepistimiou St., Anthoupoli, 264 43 Patras"},
            }),
            card("Fire Service",        "https://www.fireservice.gr/el", new String[][]{
                {"1st Fire Station", "261 023 3211", "Klirou Agion Martiron, 26335 Patras"},
                {"2nd Fire Station", "261 034 4880", "Klirou Agion Martiron, Patras"},
            }),
            card("DEYAP",               "https://deyaponline.gr/",       new String[][]{
                {"Patras Service", "2610 566 184", "81 Pavlou St., 262 32 Patras"},
            }),
            card("DEI — Public Power",  "https://www.dei.gr/el",         new String[][]{
                {"Patras Service", "261 064 2793", "95 Ermou St., 26110 Patras"},
            }),
            card("Municipality of Patras", "https://www.patras.gr/",     new String[][]{
                {"Patras Service", "261 461 6336", "7 Gounarei St., 26332 Patras"},
            })
        );
        col.setAlignment(Pos.TOP_CENTER);
        col.setMaxWidth(700);

        HBox centred = new HBox(col);
        centred.setAlignment(Pos.TOP_CENTER);
        centred.setPadding(new Insets(48, 40, 48, 40));

        VBox overlay = new VBox(centred, buildFooter());
        overlay.setStyle("-fx-background-color: rgba(4, 4, 18, 0.48);");

        ScrollPane sp = new ScrollPane(overlay);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;"
        );

        return new StackPane(sp);
    }

    private VBox card(String title, String url, String[][] rows) {
        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Label websitePfx = new Label("Website:");
        websitePfx.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        Hyperlink link = new Hyperlink(url);
        link.setStyle(
            "-fx-text-fill: #60a5fa;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: transparent;" +
            "-fx-padding: 0;" +
            "-fx-cursor: hand;" +
            "-fx-underline: true;"
        );
        link.setOnAction(e -> openUrl(url));

        HBox websiteRow = new HBox(6, websitePfx, link);
        websiteRow.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.15);");
        sep.setPadding(new Insets(2, 0, 4, 0));

        VBox box = new VBox(10, lbl, websiteRow, sep);
        box.setStyle(
            "-fx-background-color: rgba(14, 14, 32, 0.76);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255,255,255,0.18);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 16;" +
            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.55),22,0,0,6);"
        );
        box.setPadding(new Insets(24, 28, 24, 28));
        box.setMaxWidth(700);

        for (String[] r : rows) box.getChildren().add(buildEntry(r[0], r[1], r[2]));
        return box;
    }

    private VBox buildEntry(String name, String phone, String address) {
        Label nameLbl = new Label(name + " –");
        nameLbl.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label phonePfx = new Label("Phone:");
        phonePfx.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        Label phoneLbl = new Label(phone);
        phoneLbl.setStyle("-fx-text-fill: #c084fc; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox phoneRow = new HBox(6, phonePfx, phoneLbl);
        phoneRow.setAlignment(Pos.CENTER_LEFT);

        Label addrPfx = new Label("Address:");
        addrPfx.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        Label addrLbl = new Label(address);
        addrLbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
        addrLbl.setWrapText(true);
        HBox addrRow = new HBox(6, addrPfx, addrLbl);
        addrRow.setAlignment(Pos.TOP_LEFT);

        VBox entry = new VBox(3, nameLbl, phoneRow, addrRow);
        entry.setPadding(new Insets(8, 0, 4, 0));
        return entry;
    }

    private StackPane buildNavBar() {
        StackPane navContainer = new StackPane();
        navContainer.getStyleClass().add("navbar-container");
        navContainer.setPrefHeight(60);

        Label navHome    = navLink(lang.nav_home());
        Label navReports = navLink(lang.nav_reports());
        Label navUseful  = navLink(lang.nav_useful());
        navHome.setOnMouseClicked(e   -> { if (onHome   != null) onHome.run(); });
        navReports.setOnMouseClicked(e -> { if (onReport != null) onReport.run(); });

        HBox links = new HBox(28, navHome, navReports, navUseful);
        links.setAlignment(Pos.CENTER_LEFT);

        HBox controls  = buildControls();
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
        grFlag.setOnMouseClicked(e -> { lang.setGreek(true);  updateFlagOpacity(grFlag, enFlag, true);  rebuild(); });
        enFlag.setOnMouseClicked(e -> { lang.setGreek(false); updateFlagOpacity(grFlag, enFlag, false); rebuild(); });

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

    private void rebuild() {
        javafx.application.Platform.runLater(() -> {
            rootRef.setTop(buildNavBar());
            rootRef.setCenter(buildCenter());
        });
    }

    private HBox buildFooter() {
        Label footerLabel = new Label(lang.footer());
        footerLabel.getStyleClass().add("footer-text");
        HBox footer = new HBox(footerLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 24, 0));
        footer.getStyleClass().add("footer");
        return footer;
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ignored) {}
    }
}
