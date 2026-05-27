package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;

public class AuthDialog {

    // in-memory user store: key = email or mobile, value = password
    private static final Map<String, String> users = new HashMap<>();

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
        loginBtn.setOnAction(e -> {
            String id   = emailField.getText().trim();
            String pass = passField.getText();
            if (users.containsKey(id) && users.get(id).equals(pass)) {
                onBack.run();
            }
        });

        Label toggleLbl = new Label("Don't have an account?");
        toggleLbl.getStyleClass().add("auth-toggle-link");
        toggleLbl.setOnMouseClicked(ev -> cardContainer.getChildren().setAll(buildRegisterCard()));

        VBox card = new VBox(14,
                title,
                new VBox(6, emailLbl, emailField),
                new VBox(6, passLbl, passField),
                loginBtn,
                toggleLbl);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(40, 40, 40, 40));
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(360);
        return card;
    }

    private VBox buildRegisterCard() {
        Label title = new Label("Register");
        title.getStyleClass().add("auth-card-title");

        Label firstLbl = new Label("First Name");
        firstLbl.getStyleClass().add("auth-field-label");
        TextField firstField = new TextField();
        firstField.getStyleClass().add("auth-field");
        firstField.setMaxWidth(Double.MAX_VALUE);

        Label lastLbl = new Label("Last Name");
        lastLbl.getStyleClass().add("auth-field-label");
        TextField lastField = new TextField();
        lastField.getStyleClass().add("auth-field");
        lastField.setMaxWidth(Double.MAX_VALUE);

        VBox firstBox = new VBox(4, firstLbl, firstField);
        VBox lastBox  = new VBox(4, lastLbl, lastField);
        HBox.setHgrow(firstBox, Priority.ALWAYS);
        HBox.setHgrow(lastBox, Priority.ALWAYS);
        HBox nameRow = new HBox(10, firstBox, lastBox);

        Label emailLbl = new Label("Email");
        emailLbl.getStyleClass().add("auth-field-label");
        TextField emailField = new TextField();
        emailField.getStyleClass().add("auth-field");
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label mobileLbl = new Label("Mobile Phone");
        mobileLbl.getStyleClass().add("auth-field-label");
        TextField mobileField = new TextField();
        mobileField.getStyleClass().add("auth-field");
        mobileField.setMaxWidth(Double.MAX_VALUE);

        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("auth-field-label");
        PasswordField passField = new PasswordField();
        passField.getStyleClass().add("auth-field");
        passField.setMaxWidth(Double.MAX_VALUE);

        Label confirmLbl = new Label("Confirm Password");
        confirmLbl.getStyleClass().add("auth-field-label");
        PasswordField confirmField = new PasswordField();
        confirmField.getStyleClass().add("auth-field");
        confirmField.setMaxWidth(Double.MAX_VALUE);

        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().add("auth-submit-btn");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> {
            String email  = emailField.getText().trim();
            String mobile = mobileField.getText().trim();
            String pass   = passField.getText();
            users.put(email, pass);
            users.put(mobile, pass);
            onBack.run();
        });

        Label toggleLbl = new Label("Already have an account?");
        toggleLbl.getStyleClass().add("auth-toggle-link");
        toggleLbl.setOnMouseClicked(ev -> cardContainer.getChildren().setAll(buildLoginCard()));

        VBox card = new VBox(10,
                title, nameRow,
                new VBox(6, emailLbl, emailField),
                new VBox(6, mobileLbl, mobileField),
                new VBox(6, passLbl, passField),
                new VBox(6, confirmLbl, confirmField),
                registerBtn, toggleLbl);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(380);
        return card;
    }
}
