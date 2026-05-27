package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class ReportInfoPageView {

    private final Runnable onBack;
    private final Runnable onSubmit;
    private final Runnable onUseful;
    private final double lat;
    private final double lon;

    private BorderPane rootRef;

    private ComboBox<String> damageCombo;
    private TextArea commentsArea;
    private Label fileNameLabel;
    private File selectedFile = null;

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
        rootRef = root;

        Button backNavBtn = new Button("← Back");
        backNavBtn.getStyleClass().add("login-btn");
        backNavBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });
        HBox topBar = new HBox(backNavBtn);
        topBar.setPadding(new Insets(12, 32, 12, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("navbar-container");
        root.setTop(topBar);

        VBox card = buildCardPanel();
        card.getStyleClass().add("report-left-panel");
        card.setPrefWidth(480);
        card.setMinWidth(400);
        card.setMaxWidth(520);

        StackPane rightPlaceholder = new StackPane();
        HBox.setHgrow(rightPlaceholder, Priority.ALWAYS);
        rightPlaceholder.setStyle("-fx-background-color: #ddd8cf;");

        HBox split = new HBox(card, rightPlaceholder);
        split.getStyleClass().add("report-split");
        root.setCenter(split);

        return root;
    }

    private VBox buildCardPanel() {
        Label title = new Label("Report Information");
        title.getStyleClass().add("report-card-title");

        // ── Damage Type ──────────────────────────────────────────────────────
        Label damageTypeLbl = new Label("Damage Type");
        damageTypeLbl.getStyleClass().add("report-field-label");
        damageCombo = new ComboBox<>();
        damageCombo.getItems().addAll(damageTypeList());
        damageCombo.getSelectionModel().selectFirst();
        damageCombo.getStyleClass().add("report-combo");
        damageCombo.setMaxWidth(Double.MAX_VALUE);
        VBox damageBox = new VBox(6, damageTypeLbl, damageCombo);
        HBox.setHgrow(damageBox, Priority.ALWAYS);

        // ── Comments ─────────────────────────────────────────────────────────
        Label commentsLbl = new Label("Comments");
        commentsLbl.getStyleClass().add("report-field-label");
        commentsArea = new TextArea();
        commentsArea.setPromptText("Describe the issue here...");
        commentsArea.getStyleClass().add("report-field");
        commentsArea.setPrefRowCount(4);
        commentsArea.setWrapText(true);
        VBox commentsBox = new VBox(6, commentsLbl, commentsArea);
        HBox.setHgrow(commentsBox, Priority.ALWAYS);

        // ── Photo upload ─────────────────────────────────────────────────────
        Label photoLbl = new Label("Photo (optional)");
        photoLbl.getStyleClass().add("report-field-label");
        fileNameLabel = new Label("No file chosen");
        fileNameLabel.getStyleClass().add("report-field-label");
        fileNameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: #94a3b8;");

        Button chooseFileBtn = new Button("Choose File");
        chooseFileBtn.getStyleClass().add("map-select-btn");
        chooseFileBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Photo");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File f = fc.showOpenDialog(rootRef.getScene().getWindow());
            if (f != null) {
                selectedFile = f;
                fileNameLabel.setText(f.getName());
                fileNameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: #f1f5f9;");
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("location-btn");
        clearBtn.setOnAction(e -> {
            selectedFile = null;
            fileNameLabel.setText("No file chosen");
            fileNameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: #94a3b8;");
        });

        HBox fileRow = new HBox(10, chooseFileBtn, clearBtn, fileNameLabel);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        VBox photoBox = new VBox(6, photoLbl, fileRow);

        // ── Bottom buttons ────────────────────────────────────────────────────
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

        VBox card = new VBox(24, title, damageBox, commentsBox, photoBox, bottomRow);
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

    public String getDamageType() { return damageCombo  != null ? damageCombo.getValue()  : ""; }
    public String getComments()   { return commentsArea != null ? commentsArea.getText()   : ""; }
    public File   getPhotoFile()  { return selectedFile; }
}
