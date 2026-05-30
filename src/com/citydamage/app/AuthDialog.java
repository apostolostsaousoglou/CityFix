package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

import java.util.function.Consumer;

/**
 * Full-page login/register view. Call build() to get the root node,
 * then set it as the scene root via MainApp.
 * Users are authenticated against the PostgreSQL database.
 */
public class AuthDialog {

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Consumer<DatabaseManager.UserRecord> onSuccess;
    private final Runnable onBack;
    private StackPane cardContainer;

    public AuthDialog(Runnable onBack, Consumer<DatabaseManager.UserRecord> onSuccess) {
        this.onBack     = onBack;
        this.onSuccess  = onSuccess;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setTop(buildTopBar());

        cardContainer = new StackPane();
        cardContainer.setAlignment(Pos.CENTER);
        cardContainer.setPadding(new Insets(60, 0, 60, 0));
        cardContainer.getChildren().add(buildLoginCard());
        cardContainer.setStyle("-fx-background-color: rgba(4,4,18,0.52);");

        ScrollPane sp = new ScrollPane(cardContainer);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        ImageView bgView = new ImageView();
        try { bgView.setImage(new Image(
                new java.io.File("resources/bg.jpg").toURI().toString())); }
        catch (Exception ignored) {}
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);

        StackPane center = new StackPane(bgView, sp);
        bgView.fitWidthProperty().bind(center.widthProperty());
        bgView.fitHeightProperty().bind(center.heightProperty());
        root.setCenter(center);

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

    private void done(DatabaseManager.UserRecord user) {
        if (onSuccess != null) onSuccess.accept(user);
        else onBack.run();
    }

    // ─── LOGIN CARD ────────────────────────────────────────────────────────────

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
            loginBtn.setDisable(true);
            Thread t = new Thread(() -> {
                DatabaseManager.UserRecord user = DatabaseManager.getInstance().login(id, pass);
                javafx.application.Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    if (user != null) {
                        done(user);
                    } else {
                        errorLbl.setText(lang.isGreek()
                                ? "Λάθος στοιχεία εισόδου."
                                : "Incorrect email/mobile or password.");
                    }
                });
            });
            t.setDaemon(true);
            t.start();
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

    // ─── REGISTER CARD ─────────────────────────────────────────────────────────

    private VBox buildRegisterCard() {
        Label title = new Label(lang.isGreek() ? "Στοιχεία Εγγραφής" : "Register");
        title.getStyleClass().add("auth-card-title");

        // Name row
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
        VBox lastBox  = new VBox(4, lastLbl,  lastField);
        HBox.setHgrow(firstBox, Priority.ALWAYS);
        HBox.setHgrow(lastBox,  Priority.ALWAYS);
        HBox nameRow = new HBox(10, firstBox, lastBox);

        // Email with domain autocomplete
        Label emailLbl = new Label("Email");
        emailLbl.getStyleClass().add("auth-field-label");
        TextField emailField = new TextField();
        emailField.getStyleClass().add("auth-field");
        emailField.setMaxWidth(Double.MAX_VALUE);

        List<String> domains = List.of(
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com",
            "icloud.com", "live.com", "msn.com", "protonmail.com",
            "gmx.com", "mail.com", "yahoo.gr", "hotmail.gr"
        );
        ContextMenu emailSuggestions = new ContextMenu();
        emailField.textProperty().addListener((obs, old, text) -> {
            emailSuggestions.getItems().clear();
            if (text.isEmpty()) { emailSuggestions.hide(); return; }
            String prefix = text.contains("@") ? text.substring(0, text.indexOf('@')) : text;
            String typed  = text.contains("@") ? text.substring(text.indexOf('@') + 1) : "";
            List<String> matches = domains.stream()
                .filter(d -> typed.isEmpty() || d.startsWith(typed))
                .toList();
            if (matches.isEmpty()) { emailSuggestions.hide(); return; }
            for (String d : matches) {
                String full = prefix + "@" + d;
                MenuItem item = new MenuItem(full);
                item.setOnAction(ev -> { emailField.setText(full); emailField.positionCaret(full.length()); });
                emailSuggestions.getItems().add(item);
            }
            emailSuggestions.show(emailField, Side.BOTTOM, 0, 0);
        });
        emailField.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) emailSuggestions.hide();
        });

        // Mobile — digits only, max 10
        Label mobileLbl = new Label(lang.isGreek() ? "Κινητό Τηλέφωνο" : "Mobile Phone");
        mobileLbl.getStyleClass().add("auth-field-label");
        TextField mobileField = new TextField();
        mobileField.getStyleClass().add("auth-field");
        mobileField.setMaxWidth(Double.MAX_VALUE);
        mobileField.setPromptText(lang.isGreek() ? "10 ψηφία" : "10 digits");
        mobileField.textProperty().addListener((obs, old, val) -> {
            String digits = val.replaceAll("[^0-9]", "");
            if (digits.length() > 10) digits = digits.substring(0, 10);
            if (!val.equals(digits)) mobileField.setText(digits);
        });

        // Password
        Label passLbl = new Label(lang.isGreek() ? "Κωδικός" : "Password");
        passLbl.getStyleClass().add("auth-field-label");
        PasswordField passField = new PasswordField();
        passField.getStyleClass().add("auth-field");
        passField.setMaxWidth(Double.MAX_VALUE);

        // Confirm Password
        Label confirmLbl = new Label(lang.isGreek() ? "Επαλήθευση Κωδικού" : "Confirm Password");
        confirmLbl.getStyleClass().add("auth-field-label");
        PasswordField confirmField = new PasswordField();
        confirmField.getStyleClass().add("auth-field");
        confirmField.setMaxWidth(Double.MAX_VALUE);

        // Role toggle
        Label roleLbl = new Label(lang.isGreek() ? "Τύπος Λογαριασμού" : "Account Type");
        roleLbl.getStyleClass().add("auth-field-label");
        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton userRbtn  = new RadioButton(lang.isGreek() ? "Χρήστης" : "User");
        RadioButton adminRbtn = new RadioButton(lang.isGreek() ? "Διαχειριστής" : "Admin");
        userRbtn.setToggleGroup(roleGroup);
        adminRbtn.setToggleGroup(roleGroup);
        userRbtn.setSelected(true);
        userRbtn.setStyle("-fx-text-fill: #f1f5f9;");
        adminRbtn.setStyle("-fx-text-fill: #f1f5f9;");
        HBox roleRow = new HBox(20, userRbtn, adminRbtn);

        // Admin secret password (shown only when Admin is selected)
        Label adminKeyLbl = new Label(lang.isGreek() ? "Κωδικός Διαχειριστή" : "Admin Password");
        adminKeyLbl.getStyleClass().add("auth-field-label");
        PasswordField adminKeyField = new PasswordField();
        adminKeyField.getStyleClass().add("auth-field");
        adminKeyField.setMaxWidth(Double.MAX_VALUE);
        adminKeyField.setPromptText(lang.isGreek() ? "Εισάγετε κωδικό πρόσβασης" : "Enter admin access code");
        VBox adminKeyBox = new VBox(4, adminKeyLbl, adminKeyField);
        adminKeyBox.setVisible(false);
        adminKeyBox.setManaged(false);

        adminRbtn.selectedProperty().addListener((obs, old, selected) -> {
            adminKeyBox.setVisible(selected);
            adminKeyBox.setManaged(selected);
            if (!selected) adminKeyField.clear();
        });

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
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                errorLbl.setText(lang.isGreek()
                        ? "Μη έγκυρη διεύθυνση email."
                        : "Invalid email address.");
                return;
            }
            if (mobile.length() != 10) {
                errorLbl.setText(lang.isGreek()
                        ? "Το κινητό πρέπει να είναι ακριβώς 10 ψηφία."
                        : "Phone number must be exactly 10 digits.");
                return;
            }
            if (!pass.equals(confirm)) {
                errorLbl.setText(lang.isGreek()
                        ? "Οι κωδικοί δεν ταιριάζουν."
                        : "Passwords do not match.");
                return;
            }
            boolean isAdmin = adminRbtn.isSelected();
            if (isAdmin && !adminKeyField.getText().equals("CEIDPATRAS")) {
                errorLbl.setText(lang.isGreek()
                        ? "Λάθος κωδικός διαχειριστή."
                        : "Wrong admin password.");
                return;
            }

            registerBtn.setDisable(true);
            Thread t = new Thread(() -> {
                DatabaseManager db = DatabaseManager.getInstance();
                if (db.getUserByEmail(email) != null) {
                    javafx.application.Platform.runLater(() -> {
                        registerBtn.setDisable(false);
                        errorLbl.setText(lang.isGreek()
                                ? "Αυτό το email χρησιμοποιείται ήδη."
                                : "This email is already in use.");
                    });
                    return;
                }
                if (db.getUserByPhone(mobile) != null) {
                    javafx.application.Platform.runLater(() -> {
                        registerBtn.setDisable(false);
                        errorLbl.setText(lang.isGreek()
                                ? "Αυτό το κινητό χρησιμοποιείται ήδη."
                                : "This phone number is already in use.");
                    });
                    return;
                }
                DatabaseManager.UserRecord user = db.register(first, last, email, mobile, pass, isAdmin);
                javafx.application.Platform.runLater(() -> {
                    registerBtn.setDisable(false);
                    if (user != null) {
                        done(user);
                    } else {
                        errorLbl.setText(lang.isGreek()
                                ? "Σφάλμα εγγραφής. Δοκιμάστε ξανά."
                                : "Registration failed. Please try again.");
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        });

        Label toggleLbl = new Label(lang.isGreek() ? "Έχεις ήδη λογαριασμό;" : "Already have an account?");
        toggleLbl.getStyleClass().add("auth-toggle-link");
        toggleLbl.setOnMouseClicked(ev -> cardContainer.getChildren().setAll(buildLoginCard()));

        VBox card = new VBox(10,
                title,
                nameRow,
                new VBox(6, emailLbl,   emailField),
                new VBox(6, mobileLbl,  mobileField),
                new VBox(6, passLbl,    passField),
                new VBox(6, confirmLbl, confirmField),
                new VBox(6, roleLbl,    roleRow),
                adminKeyBox,
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
