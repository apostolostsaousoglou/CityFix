package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ReportInfoPageView {

    private final Runnable onBack;
    private final Runnable onSubmit;
    private final Runnable onUseful;
    private final double lat;
    private final double lon;

    private ComboBox<String> damageCombo;

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

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("login-btn");
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });
        HBox topBar = new HBox(backBtn);
        topBar.setPadding(new Insets(12, 32, 12, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("navbar-container");
        root.setTop(topBar);

        VBox card = buildCardPanel();
        card.getStyleClass().add("report-left-panel");
        card.setPrefWidth(480);

        HBox split = new HBox(card);
        split.getStyleClass().add("report-split");
        root.setCenter(split);

        return root;
    }

    private VBox buildCardPanel() {
        Label title = new Label("Report Information");
        title.getStyleClass().add("report-card-title");

        Label damageTypeLbl = new Label("Damage Type");
        damageTypeLbl.getStyleClass().add("report-field-label");
        damageCombo = new ComboBox<>();
        damageCombo.getItems().addAll(damageTypeList());
        damageCombo.getSelectionModel().selectFirst();
        damageCombo.getStyleClass().add("report-combo");
        damageCombo.setMaxWidth(Double.MAX_VALUE);
        VBox damageBox = new VBox(6, damageTypeLbl, damageCombo);
        HBox.setHgrow(damageBox, Priority.ALWAYS);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("location-btn");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        Button submitBtn = new Button("Submit");
        submitBtn.getStyleClass().add("cta-btn");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> { if (onSubmit != null) onSubmit.run(); });

        HBox.setHgrow(backBtn, Priority.ALWAYS);
        HBox.setHgrow(submitBtn, Priority.ALWAYS);
        HBox bottomRow = new HBox(16, backBtn, submitBtn);

        VBox card = new VBox(24, title, damageBox, bottomRow);
        card.setPadding(new Insets(40, 36, 40, 36));
        card.setAlignment(Pos.TOP_CENTER);
        return card;
    }

    private static List<String> damageTypeList() {
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

    public String getDamageType() { return damageCombo != null ? damageCombo.getValue() : ""; }
    public String getComments()   { return ""; }
    public java.io.File getPhotoFile() { return null; }
}
