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

    // Loaded once, reused across language rebuilds
    private static final Image BG = loadBg();
    private static Image loadBg() {
        try { return new Image(new java.io.File("resources/bg.jpg").toURI().toString()); }
        catch (Exception e) { return null; }
    }

    public UsefulPageView(Runnable onHome, Runnable onReport) {
        this.onHome   = onHome;
        this.onReport = onReport;
    }

    // ─── ROOT ─────────────────────────────────────────────────────────────────

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        rootRef = root;
        root.setTop(buildNavBar());
        root.setCenter(buildCenter());
        return root;
    }

    // ─── BACKGROUND + SCROLL STACK ────────────────────────────────────────────

    private StackPane buildCenter() {
        // Fixed city-photo background — lives OUTSIDE the ScrollPane so it stays put
        ImageView bgView = new ImageView(BG);
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);

        // Transparent scroll pane with the cards
        ScrollPane sp = buildScrollPane();

        StackPane stack = new StackPane(bgView, sp);
        bgView.fitWidthProperty().bind(stack.widthProperty());
        bgView.fitHeightProperty().bind(stack.heightProperty());
        return stack;
    }

    private ScrollPane buildScrollPane() {
        // Semi-transparent dark overlay — improves card contrast over the photo
        VBox overlay = new VBox(buildCardsColumn(), buildFooter());
        overlay.setStyle("-fx-background-color: rgba(4, 4, 18, 0.48);");

        ScrollPane sp = new ScrollPane(overlay);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // Must be fully transparent so the fixed ImageView shows through
        sp.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;"
        );
        return sp;
    }

    // ─── CARDS COLUMN ─────────────────────────────────────────────────────────

    private HBox buildCardsColumn() {
        boolean gr = lang.isGreek();

        VBox col = new VBox(22,
            card(gr, "Αστυνομία", "Police",
                "https://www.astynomia.gr/",
                new String[][]{
                    row(gr, "Α' Αστυνομικό Τμήμα",  "1st Police Department",
                            "261 440 9750",
                            "Πανεπιστημίου 138, Ανθούπολη, 264 43 Πάτρα Αχαίας",
                            "138 Panepistimiou St., Anthoupoli, 264 43 Patras"),
                    row(gr, "Β' Αστυνομικό Τμήμα",  "2nd Police Department",
                            "261 089 5282",
                            "Ερμού 95, 26110 Πάτρα Αχαίας",
                            "95 Ermou St., 26110 Patras"),
                    row(gr, "Γ' Αστυνομικό Τμήμα",  "3rd Police Department",
                            "261 034 4850",
                            "Φιλίππου & Ολυμπιάδος – Νικοπόλεως, 26332 Πάτρα Αχαίας",
                            "Filippou & Olympiados – Nikopoleos, 26332 Patras"),
                    row(gr, "Τμήμα Τροχαίας",        "Traffic Department",
                            "261 440 9770",
                            "Πανεπιστημίου 138, Ανθούπολη, 264 43 Πάτρα Αχαίας",
                            "138 Panepistimiou St., Anthoupoli, 264 43 Patras"),
                }),
            card(gr, "Πυροσβεστική", "Fire Service",
                "https://www.fireservice.gr/el",
                new String[][]{
                    row(gr, "Α' Πυροσβεστικός Σταθμός", "1st Fire Station",
                            "261 023 3211",
                            "Κλήρου Αγίων Μαρτύρων, 26335 Πάτρα Αχαίας",
                            "Klirou Agion Martiron, 26335 Patras"),
                    row(gr, "Β' Πυροσβεστικός Σταθμός", "2nd Fire Station",
                            "261 034 4880",
                            "Κλήρου Αγίων Μαρτύρων, Πάτρα Αχαίας",
                            "Klirou Agion Martiron, Patras"),
                }),
            card(gr, "ΔΕΥΑΠ", "DEYAP",
                "https://deyaponline.gr/",
                new String[][]{
                    row(gr, "Υπηρεσία Πατρών", "Patras Service",
                            "2610 566 184",
                            "Παύλου 81, 262 32 Πάτρα Αχαίας",
                            "81 Pavlou St., 262 32 Patras"),
                }),
            card(gr, "ΔΕΗ", "DEI — Public Power",
                "https://www.dei.gr/el",
                new String[][]{
                    row(gr, "Υπηρεσία Πατρών", "Patras Service",
                            "261 064 2793",
                            "Ερμού 95, 26110 Πάτρα Αχαίας",
                            "95 Ermou St., 26110 Patras"),
                }),
            card(gr, "Δήμος Πατρέων", "Municipality of Patras",
                "https://www.patras.gr/",
                new String[][]{
                    row(gr, "Υπηρεσία Πατρών", "Patras Service",
                            "261 461 6336",
                            "Γούναρη 7, 26332 Πάτρα",
                            "7 Gounarei St., 26332 Patras"),
                })
        );

        col.setAlignment(Pos.TOP_CENTER);
        col.setMaxWidth(700);

        HBox centred = new HBox(col);
        centred.setAlignment(Pos.TOP_CENTER);
        centred.setPadding(new Insets(48, 40, 48, 40));
        return centred;
    }

    // Convenience: build a [name, phone, addrGr, addrEn] row array
    private String[] row(boolean gr,
                          String nameGr, String nameEn,
                          String phone,
                          String addrGr, String addrEn) {
        return new String[]{ gr ? nameGr : nameEn, phone, gr ? addrGr : addrEn };
    }

    // ─── SINGLE SERVICE CARD ──────────────────────────────────────────────────

    private VBox card(boolean gr, String titleGr, String titleEn,
                      String url, String[][] rows) {

        // Title
        Label title = new Label(gr ? titleGr : titleEn);
        title.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;"
        );

        // Website row
        Label websitePfx = new Label(gr ? "Ιστοσελίδα:" : "Website:");
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

        // Separator
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.15);");
        sep.setPadding(new Insets(2, 0, 4, 0));

        VBox box = new VBox(10, title, websiteRow, sep);
        box.setStyle(
            // Glass: dark translucent fill + white-tinted border
            "-fx-background-color: rgba(14, 14, 32, 0.76);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255,255,255,0.18);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 16;" +
            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.55),22,0,0,6);"
        );
        box.setPadding(new Insets(24, 28, 24, 28));
        box.setMaxWidth(700);

        for (String[] r : rows) box.getChildren().add(buildEntry(gr, r[0], r[1], r[2]));

        return box;
    }

    private VBox buildEntry(boolean gr, String name, String phone, String address) {
        Label nameLbl = new Label(name + " –");
        nameLbl.setStyle(
            "-fx-text-fill: #f1f5f9;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;"
        );

        Label phonePfx = new Label(gr ? "Τηλέφωνο:" : "Phone:");
        phonePfx.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        Label phoneLbl = new Label(phone);
        phoneLbl.setStyle("-fx-text-fill: #c084fc; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox phoneRow = new HBox(6, phonePfx, phoneLbl);
        phoneRow.setAlignment(Pos.CENTER_LEFT);

        Label addrPfx = new Label(gr ? "Διεύθυνση:" : "Address:");
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
        navHome.setOnMouseClicked(e   -> { if (onHome   != null) onHome.run(); });
        navReports.setOnMouseClicked(e -> { if (onReport != null) onReport.run(); });

        HBox links = new HBox(28, navHome, navReports, navUseful);
        links.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(links, new Insets(0, 0, 0, 24));

        HBox controls  = buildControls();
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

        grFlag.setOnMouseClicked(e -> { lang.setGreek(true);  grFlag.setOpacity(1.0); enFlag.setOpacity(0.35); rebuild(); });
        enFlag.setOnMouseClicked(e -> { lang.setGreek(false); grFlag.setOpacity(0.35); enFlag.setOpacity(1.0); rebuild(); });

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

    private void rebuild() {
        javafx.application.Platform.runLater(() -> {
            rootRef.setTop(buildNavBar());
            rootRef.setCenter(buildCenter());
        });
    }

    // ─── FOOTER ───────────────────────────────────────────────────────────────

    private HBox buildFooter() {
        Label footerLabel = new Label(lang.footer());
        footerLabel.getStyleClass().add("footer-text");
        HBox footer = new HBox(footerLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 24, 0));
        footer.getStyleClass().add("footer");
        return footer;
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ignored) {}
    }
}
