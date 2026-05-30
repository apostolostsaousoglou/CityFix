package com.citydamage.app;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.List;

public class ReportsPageView {


    private final LanguageManager lang = LanguageManager.getInstance();
    private final Runnable onHome;
    private final Runnable onUseful;
    private final DatabaseManager.UserRecord currentUser;

    private BorderPane rootRef;
    private ImageView  logoView;
    private VBox       listContainer;
    private TileMapPane mapPane;
    private List<DatabaseManager.ReportRecord> currentReports;

    public ReportsPageView(Runnable onHome, Runnable onReport, Runnable onUseful,
                           DatabaseManager.UserRecord currentUser) {
        this.onHome      = onHome;
        this.onUseful    = onUseful;
        this.currentUser = currentUser;
    }

    // ─── ROOT ─────────────────────────────────────────────────────────────────

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        rootRef = root;
        root.setTop(buildNavBar());
        root.setCenter(buildSplitLayout());
        root.setBottom(buildFooter());
        loadReports();
        return root;
    }

    // ─── SPLIT LAYOUT ─────────────────────────────────────────────────────────

    private HBox buildSplitLayout() {
        // ── Left: report list ────────────────────────────────────────────────
        boolean gr    = lang.isGreek();
        boolean admin = isAdmin();

        Label title = new Label(admin
                ? (gr ? "Όλες οι Αναφορές" : "All Reports")
                : (currentUser != null
                        ? (gr ? "Οι Αναφορές Μου" : "My Reports")
                        : (gr ? "Αναφορές" : "Reports")));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        listContainer = new VBox(10);
        listContainer.setPadding(new Insets(0, 0, 20, 0));

        Label loading = new Label(gr ? "Φόρτωση..." : "Loading...");
        loading.setStyle("-fx-text-fill: #94a3b8;");
        listContainer.getChildren().add(loading);

        VBox listWrapper = new VBox(14, title, listContainer);
        listWrapper.setPadding(new Insets(20, 16, 20, 16));

        ScrollPane listScroll = new ScrollPane(listWrapper);
        listScroll.setFitToWidth(true);
        listScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listScroll.getStyleClass().add("main-scroll");
        listScroll.setPrefWidth(380);
        listScroll.setMinWidth(300);
        listScroll.setMaxWidth(420);

        // ── Right: map ───────────────────────────────────────────────────────
        mapPane = new TileMapPane();
        mapPane.setLanguage(!lang.isGreek());
        HBox.setHgrow(mapPane, Priority.ALWAYS);

        HBox split = new HBox(listScroll, mapPane);
        HBox.setHgrow(mapPane, Priority.ALWAYS);
        return split;
    }

    // ─── LOAD & RENDER ────────────────────────────────────────────────────────

    private void loadReports() {
        Thread t = new Thread(() -> {
            List<DatabaseManager.ReportRecord> reports;
            if (currentUser == null) {
                reports = List.of();
            } else if (isAdmin()) {
                reports = DatabaseManager.getInstance().getAllReports();
            } else {
                reports = DatabaseManager.getInstance().getReportsByPhone(currentUser.phone);
            }
            Platform.runLater(() -> {
                currentReports = reports;
                renderList(reports);
                renderPins(reports);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void renderList(List<DatabaseManager.ReportRecord> reports) {
        boolean gr = lang.isGreek();
        listContainer.getChildren().clear();

        if (currentUser == null) {
            Label msg = new Label(gr ? "Συνδεθείτε για να δείτε τις αναφορές σας."
                                     : "Log in to see your reports.");
            msg.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            msg.setWrapText(true);
            listContainer.getChildren().add(msg);
            return;
        }

        if (reports.isEmpty()) {
            Label empty = new Label(gr ? "Δεν υπάρχουν αναφορές." : "No reports found.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            listContainer.getChildren().add(empty);
            return;
        }

        for (DatabaseManager.ReportRecord r : reports) {
            listContainer.getChildren().add(buildCard(r));
        }
    }

    private void renderPins(List<DatabaseManager.ReportRecord> reports) {
        if (mapPane == null) return;
        mapPane.clearPins();

        if (reports.isEmpty()) return;

        // Centre map on first report
        DatabaseManager.ReportRecord first = reports.get(0);
        mapPane.panTo(first.latitude, first.longitude);

        for (DatabaseManager.ReportRecord r : reports) {
            String color = statusColor(r.status);
            boolean gr   = lang.isGreek();
            String label = localizeType(r.type, gr) + "\n"
                    + r.street + " " + r.streetNumber + ", " + r.area + "\n"
                    + (gr ? "Κατάσταση: " : "Status: ") + localizeStatus(r.status, gr) + "\n"
                    + (gr ? "Ημ/νία: " : "Date: ") + r.date
                    + (isAdmin() ? "\n" + (gr ? "Χρήστης: " : "User: ") + r.userPhone : "");

            mapPane.addPin(r.latitude, r.longitude, color, label, () -> scrollToReport(r.id));
        }
    }

    private void scrollToReport(int reportId) {
        // Highlight the card in the list (find it and pulse style)
        listContainer.getChildren().stream()
            .filter(n -> n instanceof VBox && Integer.valueOf(reportId).equals(n.getUserData()))
            .findFirst()
            .ifPresent(n -> {
                n.setStyle(((VBox) n).getStyle() + " -fx-border-color: #6366f1; -fx-border-width: 2; -fx-border-radius: 8;");
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(1.5));
                pause.setOnFinished(e -> n.setStyle(((VBox) n).getStyle()
                        .replace(" -fx-border-color: #6366f1; -fx-border-width: 2; -fx-border-radius: 8;", "")));
                pause.play();
            });
    }

    // ─── CARD ─────────────────────────────────────────────────────────────────

    private VBox buildCard(DatabaseManager.ReportRecord r) {
        boolean gr    = lang.isGreek();
        boolean admin = isAdmin();
        String  color = statusColor(r.status);

        Region bar = new Region();
        bar.setPrefWidth(5);
        bar.setMinWidth(5);
        bar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4 0 0 4;");

        Label typeLbl = new Label(localizeType(r.type, gr));
        typeLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
        typeLbl.setWrapText(true);

        Label addrLbl = new Label(r.street + " " + r.streetNumber + ", " + r.area);
        addrLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label dateLbl = new Label((gr ? "Ημ/νία: " : "Date: ") + r.date);
        dateLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        Label statusLbl = new Label(localizeStatus(r.status, gr));
        statusLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        VBox info = new VBox(4, typeLbl, addrLbl, statusLbl, dateLbl);

        if (r.description != null && !r.description.isBlank()) {
            Label desc = new Label(r.description);
            desc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
            desc.setWrapText(true);
            info.getChildren().add(1, desc);
        }
        if (admin) {
            Label phoneLbl = new Label((gr ? "Χρήστης: " : "User: ") + r.userPhone);
            phoneLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");
            info.getChildren().add(phoneLbl);
        }

        HBox.setHgrow(info, Priority.ALWAYS);

        // Actions
        VBox actions = new VBox(6);
        actions.setAlignment(Pos.TOP_RIGHT);
        actions.setMinWidth(130);

        if (admin) {
            ComboBox<String> statusBox = new ComboBox<>();
            statusBox.getItems().addAll("received", "approved", "in progress", "completed", "rejected");
            statusBox.setValue(r.status);
            statusBox.getStyleClass().add("report-combo");
            statusBox.setMaxWidth(Double.MAX_VALUE);
            statusBox.setPrefWidth(128);

            Button applyBtn = new Button(gr ? "Εφαρμογή" : "Apply");
            applyBtn.getStyleClass().add("location-btn");
            applyBtn.setMaxWidth(Double.MAX_VALUE);
            applyBtn.setOnAction(e -> {
                applyBtn.setDisable(true);
                Thread t = new Thread(() -> {
                    DatabaseManager.getInstance().updateReportStatus(r.id, statusBox.getValue());
                    Platform.runLater(() -> { applyBtn.setDisable(false); loadReports(); });
                });
                t.setDaemon(true);
                t.start();
            });
            actions.getChildren().addAll(statusBox, applyBtn);

            if (r.hasPhoto) {
                Button photoBtn = new Button(gr ? "Εικόνα" : "View Photo");
                photoBtn.setStyle("-fx-background-color: #0f172a; -fx-text-fill: #94a3b8; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px; " +
                        "-fx-border-color: #334155; -fx-border-radius: 6; -fx-border-width: 1;");
                photoBtn.setMaxWidth(Double.MAX_VALUE);
                photoBtn.setOnAction(e -> showPhotoDialog(r.id, gr));
                actions.getChildren().add(photoBtn);
            }
        } else if ("received".equals(r.status)) {
            Button delBtn = new Button(gr ? "Διαγραφή" : "Delete");
            delBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
            delBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        gr ? "Διαγραφή αναφοράς;" : "Delete this report?",
                        ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText(null);
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.YES) {
                        Thread t = new Thread(() -> {
                            DatabaseManager.getInstance().deleteReport(r.id);
                            Platform.runLater(this::loadReports);
                        });
                        t.setDaemon(true);
                        t.start();
                    }
                });
            });
            actions.getChildren().add(delBtn);
        }

        // "Show on map" button
        Button mapBtn = new Button(gr ? "Στον χάρτη" : "Show on map");
        mapBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px;");
        mapBtn.setOnAction(e -> {
            if (mapPane != null) {
                mapPane.panTo(r.latitude, r.longitude);
            }
        });
        actions.getChildren().add(mapBtn);

        HBox body = new HBox(12, info, actions);
        body.setAlignment(Pos.TOP_LEFT);
        body.setPadding(new Insets(12, 12, 12, 12));

        HBox card = new HBox(bar, body);
        HBox.setHgrow(body, Priority.ALWAYS);
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 0 8 8 0;");

        VBox wrapper = new VBox(card);
        wrapper.setStyle("-fx-background-radius: 8; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.3),4,0,0,1);");
        wrapper.setUserData(r.id);
        return wrapper;
    }

    // ─── PHOTO DIALOG ─────────────────────────────────────────────────────────

    private void showPhotoDialog(int reportId, boolean gr) {
        Thread t = new Thread(() -> {
            byte[] bytes = DatabaseManager.getInstance().getReportPhoto(reportId);
            Platform.runLater(() -> {
                if (bytes == null || bytes.length == 0) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION,
                            gr ? "Δεν βρέθηκε φωτογραφία." : "No photo found.",
                            ButtonType.OK);
                    a.setHeaderText(null);
                    a.showAndWait();
                    return;
                }
                Image img = new Image(new ByteArrayInputStream(bytes));
                ImageView iv = new ImageView(img);
                iv.setPreserveRatio(true);
                iv.setFitWidth(700);
                iv.setFitHeight(560);

                ScrollPane sp = new ScrollPane(iv);
                sp.setFitToWidth(true);
                sp.setFitToHeight(true);
                sp.setStyle("-fx-background: #0f172a; -fx-background-color: #0f172a;");

                Stage dialog = new Stage();
                dialog.setTitle(gr ? "Φωτογραφία Αναφοράς #" + reportId
                                   : "Report Photo #" + reportId);
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setScene(new Scene(sp, 720, 580));
                dialog.show();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin;
    }

    private static final java.util.Map<String, String> TYPE_TRANSLATIONS = java.util.Map.ofEntries(
        java.util.Map.entry("Χαλασμένος σωλήνας νερού",                      "Broken water pipe"),
        java.util.Map.entry("Διαρροές αερίου",                                "Gas leaks"),
        java.util.Map.entry("Χαλασμένα φανάρια",                              "Broken traffic lights"),
        java.util.Map.entry("Εκτεθειμένα καλώδια ή ηλεκτρολογικοί κίνδυνοι","Exposed wires or electrical hazards"),
        java.util.Map.entry("Λακκούβες",                                       "Potholes"),
        java.util.Map.entry("Πεσμένα δέντρα ή κλαδιά",                        "Fallen trees or branches"),
        java.util.Map.entry("Σπασμένα πεζοδρόμια",                            "Broken sidewalks"),
        java.util.Map.entry("Κατεστραμμένα παγκάκια",                         "Damaged benches"),
        java.util.Map.entry("Παράνομη απόρριψη απορριμμάτων",                 "Illegal dumping of garbage"),
        java.util.Map.entry("Ανοιχτά ή χωρίς κάλυμμα φρεάτια",               "Open or uncovered manholes"),
        java.util.Map.entry("Σπασμένες παιδικές χαρές",                       "Broken playground equipment"),
        java.util.Map.entry("Χαλασμένος δημοτικός φωτισμός",                  "Broken municipal lighting"),
        java.util.Map.entry("Κατεστραμμένες στάσεις λεωφορείων",              "Damaged bus stops"),
        java.util.Map.entry("Ρωγμές σε τοίχους δημόσιων κτιρίων",             "Cracks in public building walls"),
        java.util.Map.entry("Άλλο",                                            "Other")
    );

    private static String localizeType(String type, boolean gr) {
        if (gr) return type;
        return TYPE_TRANSLATIONS.getOrDefault(type, type);
    }

    private static String statusColor(String status) {
        return switch (status) {
            case "received"    -> "#3b82f6";
            case "approved"    -> "#a855f7";
            case "in progress" -> "#f97316";
            case "completed"   -> "#22c55e";
            case "rejected"    -> "#ef4444";
            default            -> "#64748b";
        };
    }

    private static String localizeStatus(String status, boolean gr) {
        if (!gr) return status;
        return switch (status) {
            case "received"    -> "Παραλήφθηκε";
            case "approved"    -> "Εγκρίθηκε";
            case "in progress" -> "Σε εξέλιξη";
            case "completed"   -> "Ολοκληρώθηκε";
            case "rejected"    -> "Απορρίφθηκε";
            default            -> status;
        };
    }

    // ─── NAVBAR ───────────────────────────────────────────────────────────────

    private StackPane buildNavBar() {
        StackPane nav = new StackPane();
        nav.getStyleClass().add("navbar-container");
        nav.setPrefHeight(72);

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
        navHome.setOnMouseClicked(e   -> { if (onHome   != null) onHome.run();   });
        navUseful.setOnMouseClicked(e -> { if (onUseful != null) onUseful.run(); });
        navReports.setStyle("-fx-text-fill: #818cf8; -fx-underline: true;");

        HBox links = new HBox(28, navHome, navReports, navUseful);
        links.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(links, new Insets(0, 0, 0, 24));

        HBox controls = buildControls();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navItems = new HBox(0, logoView, links, spacer, controls);
        navItems.setAlignment(Pos.CENTER_LEFT);
        navItems.setPadding(new Insets(0, 32, 0, 12));
        navItems.getStyleClass().add("navbar");

        nav.getChildren().add(navItems);
        return nav;
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

        grFlag.setOnMouseClicked(e -> { lang.setGreek(true);  grFlag.setOpacity(1.0); enFlag.setOpacity(0.35); rootRef.setTop(buildNavBar()); rootRef.setCenter(buildSplitLayout()); loadReports(); });
        enFlag.setOnMouseClicked(e -> { lang.setGreek(false); grFlag.setOpacity(0.35); enFlag.setOpacity(1.0); rootRef.setTop(buildNavBar()); rootRef.setCenter(buildSplitLayout()); loadReports(); });

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

    // ─── FOOTER ───────────────────────────────────────────────────────────────

    private HBox buildFooter() {
        Label lbl = new Label(lang.footer());
        lbl.getStyleClass().add("footer-text");
        HBox footer = new HBox(lbl);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(14, 0, 14, 0));
        footer.getStyleClass().add("footer");
        return footer;
    }
}
