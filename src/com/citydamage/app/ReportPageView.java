package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ReportPageView {

    private final Runnable onBack;
    private final Runnable onNext;
    private final Runnable onLogin;
    private final Runnable onUseful;

    private TextField streetField;
    private TextField numberField;
    private TextField zipField;
    private ComboBox<String> areaCombo;

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
        title.setMaxWidth(Double.MAX_VALUE);

        streetField = styledField("e.g. Main St");
        numberField = styledField("e.g. 12, 12A");
        zipField    = styledField("e.g. 264 41");
        numberField.setPrefWidth(100);
        zipField.setPrefWidth(120);

        VBox streetBox = labeled("Street", streetField);
        VBox numberBox = labeled("Number", numberField);
        VBox zipBox    = labeled("ZIP",    zipField);
        HBox.setHgrow(streetBox, Priority.ALWAYS);
        HBox row1 = new HBox(12, streetBox, numberBox, zipBox);

        areaCombo = new ComboBox<>();
        areaCombo.getItems().addAll(areaList());
        areaCombo.getSelectionModel().selectFirst();
        areaCombo.getStyleClass().add("report-combo");
        areaCombo.setMaxWidth(Double.MAX_VALUE);
        VBox areaBox = labeled("Area", areaCombo);

        Button nextBtn = new Button("Next");
        nextBtn.getStyleClass().add("cta-btn");
        nextBtn.setMaxWidth(Double.MAX_VALUE);
        nextBtn.setOnAction(e -> { if (onNext != null) onNext.run(); });

        VBox card = new VBox(24, title, row1, areaBox, nextBtn);
        card.setPadding(new Insets(40, 36, 40, 36));
        card.setAlignment(Pos.TOP_CENTER);
        return card;
    }

    private static List<String> areaList() {
        return List.of(
            "Select area...",
            "Patras City Center", "Psilalonia", "Upper Town (Ano Poli)", "Lower Town (Kato Poli)",
            "Agia Sofia", "Agyia", "Rio", "Zarouchleika", "Taraboura",
            "Eglykada", "Perivola", "Bozaitika", "Ovrya", "Souli", "Koukoulí"
        );
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("report-field");
        return tf;
    }

    private VBox labeled(String text, Control field) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("report-field-label");
        VBox box = new VBox(6, lbl, field);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    public double getLat() { return selectedLat; }
    public double getLng() { return selectedLng; }
}
