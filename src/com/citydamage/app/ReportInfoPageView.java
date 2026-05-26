package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class ReportInfoPageView {

    private final Runnable onBack;
    private final Runnable onSubmit;
    private final Runnable onUseful;
    private final double lat;
    private final double lon;

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

        // Minimal top bar
        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("login-btn");
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });
        HBox topBar = new HBox(backBtn);
        topBar.setPadding(new Insets(12, 32, 12, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("navbar-container");
        root.setTop(topBar);

        // Card placeholder
        VBox card = new VBox(20);
        card.setPadding(new Insets(40, 36, 40, 36));
        card.getStyleClass().add("report-left-panel");
        card.setPrefWidth(480);

        Label title = new Label("Report Information");
        title.getStyleClass().add("report-card-title");

        Button submitBtn = new Button("Submit");
        submitBtn.getStyleClass().add("cta-btn");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> { if (onSubmit != null) onSubmit.run(); });

        card.getChildren().addAll(title, submitBtn);

        HBox split = new HBox(card);
        split.getStyleClass().add("report-split");
        root.setCenter(split);

        return root;
    }

    public String getDamageType() { return ""; }
    public String getComments()   { return ""; }
    public java.io.File getPhotoFile() { return null; }
}
