package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

public class AuthDialog {

    private final Runnable onBack;
    private StackPane cardContainer;

    public AuthDialog(Runnable onBack) {
        this.onBack = onBack;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        cardContainer = new StackPane();
        cardContainer.setAlignment(Pos.CENTER);
        cardContainer.setPadding(new Insets(60, 0, 60, 0));

        // Placeholders for login and register cards
        VBox loginPlaceholder    = new VBox();
        VBox registerPlaceholder = new VBox();
        loginPlaceholder.getStyleClass().add("auth-card");
        cardContainer.getChildren().add(loginPlaceholder);

        ScrollPane sp = new ScrollPane(cardContainer);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.getStyleClass().add("main-scroll");
        root.setCenter(sp);

        return root;
    }
}
