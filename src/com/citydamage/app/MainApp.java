package com.citydamage.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showHomePage();
        primaryStage.setTitle("City Damage Reporter");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public void showHomePage() {
        BorderPane placeholder = new BorderPane();
        if (scene == null) {
            scene = new Scene(placeholder, 1280, 750);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(placeholder);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
