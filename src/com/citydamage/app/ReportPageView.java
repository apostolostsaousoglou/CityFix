package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class ReportPageView {

    private final Runnable onBack;
    private final Runnable onNext;
    private final Runnable onLogin;
    private final Runnable onUseful;

    private double selectedLat = 38.2466;
    private double selectedLng = 21.7346;

    public ReportPageView(Runnable onBack, Runnable onNext, Runnable onLogin, Runnable onUseful) {
        this.onBack   = onBack;
        this.onNext   = onNext;
        this.onLogin  = onLogin;
        this.onUseful = onUseful;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // Placeholder top bar
        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("login-btn");
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });
        HBox topBar = new HBox(backBtn);
        topBar.setPadding(new Insets(12, 32, 12, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("navbar-container");
        root.setTop(topBar);

        // Placeholder split: left panel + right placeholder
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(40, 36, 40, 36));
        leftPanel.getStyleClass().add("report-left-panel");
        leftPanel.setPrefWidth(480);

        Label placeholder = new Label("Report Damage");
        placeholder.getStyleClass().add("report-card-title");
        leftPanel.getChildren().add(placeholder);

        Button nextBtn = new Button("Next");
        nextBtn.getStyleClass().add("cta-btn");
        nextBtn.setOnAction(e -> { if (onNext != null) onNext.run(); });
        leftPanel.getChildren().add(nextBtn);

        StackPane rightPlaceholder = new StackPane();
        HBox.setHgrow(rightPlaceholder, Priority.ALWAYS);
        rightPlaceholder.setStyle("-fx-background-color: #ddd8cf;");

        HBox split = new HBox(leftPanel, rightPlaceholder);
        split.getStyleClass().add("report-split");
        root.setCenter(split);

        return root;
    }

    public double getLat() { return selectedLat; }
    public double getLng() { return selectedLng; }
}
