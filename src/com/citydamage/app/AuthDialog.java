package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
        cardContainer.getChildren().add(buildLoginCard());

        ScrollPane sp = new ScrollPane(cardContainer);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.getStyleClass().add("main-scroll");
        root.setCenter(sp);

        return root;
    }

    private VBox buildLoginCard() {
        Label title = new Label("User Login");
        title.getStyleClass().add("auth-card-title");

        Label emailLbl = new Label("Email or Mobile");
        emailLbl.getStyleClass().add("auth-field-label");
        TextField emailField = new TextField();
        emailField.getStyleClass().add("auth-field");
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("auth-field-label");
        PasswordField passField = new PasswordField();
        passField.getStyleClass().add("auth-field");
        passField.setMaxWidth(Double.MAX_VALUE);

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("auth-submit-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        VBox card = new VBox(14,
                title,
                new VBox(6, emailLbl, emailField),
                new VBox(6, passLbl, passField),
                loginBtn);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(40, 40, 40, 40));
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(360);
        return card;
    }
}
