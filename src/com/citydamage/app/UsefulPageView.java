package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UsefulPageView {

    private final Runnable onHome;
    private final Runnable onReport;

    public UsefulPageView(Runnable onHome, Runnable onReport) {
        this.onHome   = onHome;
        this.onReport = onReport;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        Button backBtn = new Button("← Home");
        backBtn.getStyleClass().add("login-btn");
        backBtn.setOnAction(e -> { if (onHome != null) onHome.run(); });
        HBox topBar = new HBox(backBtn);
        topBar.setPadding(new Insets(12, 32, 12, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("navbar-container");
        root.setTop(topBar);

        VBox col = new VBox(22);
        col.setPadding(new Insets(48, 40, 48, 40));
        col.setAlignment(Pos.TOP_CENTER);

        for (String title : new String[]{
                "Police", "Fire Service", "DEYAP", "DEI — Public Power", "Municipality of Patras" }) {
            Label lbl = new Label(title);
            lbl.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");
            VBox card = new VBox(10, lbl);
            card.setPadding(new Insets(24, 28, 24, 28));
            card.setMaxWidth(700);
            card.setStyle(
                "-fx-background-color: rgba(14,14,32,0.76);" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: rgba(255,255,255,0.18);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 16;"
            );
            col.getChildren().add(card);
        }

        ScrollPane sp = new ScrollPane(col);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(sp);

        return root;
    }
}
