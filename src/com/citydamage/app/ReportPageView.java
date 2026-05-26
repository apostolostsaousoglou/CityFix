package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ReportPageView {

    private final Runnable onBack;
    private final Runnable onNext;
    private final Runnable onLogin;
    private final Runnable onUseful;

    private TextField streetField;
    private TextField numberField;
    private TextField zipField;

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

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("login-btn");
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });
        HBox topBar = new HBox(backBtn);
        topBar.setPadding(new Insets(12, 32, 12, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("navbar-container");
        root.setTop(topBar);

        VBox cardPanel = buildCardPanel();
        cardPanel.getStyleClass().add("report-left-panel");
        cardPanel.setPrefWidth(480);

        StackPane rightPlaceholder = new StackPane();
        HBox.setHgrow(rightPlaceholder, Priority.ALWAYS);
        rightPlaceholder.setStyle("-fx-background-color: #ddd8cf;");

        HBox split = new HBox(cardPanel, rightPlaceholder);
        split.getStyleClass().add("report-split");
        root.setCenter(split);

        return root;
    }

    private VBox buildCardPanel() {
        Label title = new Label("Report Damage");
        title.getStyleClass().add("report-card-title");

        streetField = new TextField();
        streetField.setPromptText("e.g. Main St");
        streetField.getStyleClass().add("report-field");

        numberField = new TextField();
        numberField.setPromptText("e.g. 12, 12A");
        numberField.getStyleClass().add("report-field");
        numberField.setPrefWidth(100);

        zipField = new TextField();
        zipField.setPromptText("e.g. 264 41");
        zipField.getStyleClass().add("report-field");
        zipField.setPrefWidth(120);

        Label streetLbl = new Label("Street");
        streetLbl.getStyleClass().add("report-field-label");
        Label numberLbl = new Label("Number");
        numberLbl.getStyleClass().add("report-field-label");
        Label zipLbl = new Label("ZIP");
        zipLbl.getStyleClass().add("report-field-label");

        VBox streetBox = new VBox(6, streetLbl, streetField);
        VBox numberBox = new VBox(6, numberLbl, numberField);
        VBox zipBox    = new VBox(6, zipLbl, zipField);
        HBox.setHgrow(streetBox, Priority.ALWAYS);

        HBox row1 = new HBox(12, streetBox, numberBox, zipBox);

        Button nextBtn = new Button("Next");
        nextBtn.getStyleClass().add("cta-btn");
        nextBtn.setMaxWidth(Double.MAX_VALUE);
        nextBtn.setOnAction(e -> { if (onNext != null) onNext.run(); });

        VBox card = new VBox(24, title, row1, nextBtn);
        card.setPadding(new Insets(40, 36, 40, 36));
        card.setAlignment(Pos.TOP_CENTER);
        return card;
    }

    public double getLat() { return selectedLat; }
    public double getLng() { return selectedLng; }
}
