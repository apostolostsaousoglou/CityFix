package com.citydamage.app;

import javafx.scene.layout.BorderPane;

public class HomePageView {

    private final Runnable onReport;
    private final Runnable onLogin;
    private final Runnable onUseful;

    public HomePageView(Runnable onReport, Runnable onLogin, Runnable onUseful) {
        this.onReport = onReport;
        this.onLogin  = onLogin;
        this.onUseful = onUseful;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        return root;
    }
}
