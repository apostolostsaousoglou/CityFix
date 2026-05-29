package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;

public class AuthDialog {

    private static final Map<String, String> users = new HashMap<>();

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Runnable onBack;
    private StackPane cardContainer;

    public AuthDialog(Runnable onBack) {
        this.onBack = onBack;
    }

    private void done() {
        onBack.run();
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setTop(buildTopBar());

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

    private HBox buildTopBar() {
        Button backBtn = new Button(lang.isGreek() ? "← Πίσω" : "← Back");
        backBtn.getStyleClass().add("login-btn");
        backBtn.setOnAction(e -> onBack.run());

        HBox bar = new HBox(backBtn);
        bar.setPadding(new Insets(12, 32, 12, 32));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("navbar-container");
        return bar;
    }

    private VBox buildLoginCard() {
        Label title = new Label(lang.isGreek() ? "Είσοδος Χρήστη" : "User Login");
        title.getStyleClass().add("auth-card-title");

        Label emailLbl = new Label(lang.isGreek() ? "Email ή Κινητό" : "Email or Mobile");
        emailLbl.getStyleClass().add("auth-field-label");
        TextField emailField = new TextField();
        emailField.getStyleClass().add("auth-field");
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label passLbl = new Label(lang.isGreek() ? "Κωδικός" : "Password");
        passLbl.getStyleClass().add("auth-field-label");
        PasswordField passField = new PasswordField();
        passField.getStyleClass().add("auth-field");
        passField.setMaxWidth(Double.MAX_VALUE);

        Label errorLbl = new Label("");
        errorLbl.getStyleClass().add("auth-error-label");
        errorLbl.setWrapText(true);

        Button loginBtn = new Button(lang.isGreek() ? "Είσοδος" : "Login");
        loginBtn.getStyleClass().add("auth-submit-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            String id   = emailField.getText().trim();
            String pass = passField.getText();
            if (id.isEmpty() || pass.isEmpty()) {
                errorLbl.setText(lang.isGreek()
                        ? "Συμπλήρωσε όλα τα πεδία."
                        : "Please fill in all fields.");
                return;
            }
            if (users.containsKey(id) && users.get(id).equals(pass)) {
                done();
            } else {
                errorLbl.setText(lang.isGreek()
                        ? "Λάθος στοιχεία εισόδου."
                        : "Incorrect email/mobile or password.");
            }
        });

        Label toggleLbl = new Label(lang.isGreek() ? "Δεν έχεις λογαριασμό;" : "Don't have an account?");
        toggleLbl.getStyleClass().add("auth-toggle-link");
        toggleLbl.setOnMouseClicked(ev -> cardContainer.getChildren().setAll(buildRegisterCard()));

        VBox card = new VBox(14,
                title,
                new VBox(6, emailLbl, emailField),
                new VBox(6, passLbl, passField),
                loginBtn,
                errorLbl,
                toggleLbl);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(40, 40, 40, 40));
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(360);
        return card;
    }

    private VBox buildRegisterCard() {
        Label title = new Label(lang.isGreek() ? "Στοιχεία Εγγραφής" : "Register");
        title.getStyleClass().add("auth-card-title");

        Label firstLbl = new Label(lang.isGreek() ? "Όνομα" : "First Name");
        firstLbl.getStyleClass().add("auth-field-label");
        TextField firstField = new TextField();
        firstField.getStyleClass().add("auth-field");
        firstField.setMaxWidth(Double.MAX_VALUE);

        Label lastLbl = new Label(lang.isGreek() ? "Επώνυμο" : "Last Name");
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

        Label mobileLbl = new Label(lang.isGreek() ? "Κινητό Τηλέφωνο" : "Mobile Phone");
        mobileLbl.getStyleClass().add("auth-field-label");
        TextField mobileField = new TextField();
        mobileField.getStyleClass().add("auth-field");
        mobileField.setMaxWidth(Double.MAX_VALUE);

        Label passLbl = new Label(lang.isGreek() ? "Κωδικός" : "Password");
        passLbl.getStyleClass().add("auth-field-label");
        PasswordField passField = new PasswordField();
        passField.getStyleClass().add("auth-field");
        passField.setMaxWidth(Double.MAX_VALUE);

        Label confirmLbl = new Label(lang.isGreek() ? "Επαλήθευση Κωδικού" : "Confirm Password");
        confirmLbl.getStyleClass().add("auth-field-label");
        PasswordField confirmField = new PasswordField();
        confirmField.getStyleClass().add("auth-field");
        confirmField.setMaxWidth(Double.MAX_VALUE);

        Label errorLbl = new Label("");
        errorLbl.getStyleClass().add("auth-error-label");
        errorLbl.setWrapText(true);

        Button registerBtn = new Button(lang.isGreek() ? "Εγγραφή" : "Register");
        registerBtn.getStyleClass().add("auth-submit-btn");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> {
            String first   = firstField.getText().trim();
            String last    = lastField.getText().trim();
            String email   = emailField.getText().trim();
            String mobile  = mobileField.getText().trim();
            String pass    = passField.getText();
            String confirm = confirmField.getText();

            if (first.isEmpty() || last.isEmpty() || email.isEmpty()
                    || mobile.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                errorLbl.setText(lang.isGreek()
                        ? "Συμπλήρωσε όλα τα πεδία."
                        : "Please fill in all fields.");
                return;
            }
            if (!pass.equals(confirm)) {
                errorLbl.setText(lang.isGreek()
                        ? "Οι κωδικοί δεν ταιριάζουν."
                        : "Passwords do not match.");
                return;
            }
            if (users.containsKey(email)) {
                errorLbl.setText(lang.isGreek()
                        ? "Αυτό το email χρησιμοποιείται ήδη."
                        : "This email is already in use.");
                return;
            }
            users.put(email, pass);
            users.put(mobile, pass);
            done();
        });

        Label toggleLbl = new Label(lang.isGreek() ? "Έχεις ήδη λογαριασμό;" : "Already have an account?");
        toggleLbl.getStyleClass().add("auth-toggle-link");
        toggleLbl.setOnMouseClicked(ev -> cardContainer.getChildren().setAll(buildLoginCard()));

        VBox card = new VBox(10,
                title, nameRow,
                new VBox(6, emailLbl, emailField),
                new VBox(6, mobileLbl, mobileField),
                new VBox(6, passLbl, passField),
                new VBox(6, confirmLbl, confirmField),
                registerBtn,
                errorLbl,
                toggleLbl);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(380);
        return card;
    }
}
