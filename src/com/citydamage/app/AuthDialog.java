package com.citydamage.app;

import javafx.scene.layout.BorderPane;

public class AuthDialog {

    private final Runnable onBack;

    public AuthDialog(Runnable onBack) {
        this.onBack = onBack;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        return root;
    }
}
