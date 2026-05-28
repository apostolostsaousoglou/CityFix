package com.citydamage.app;

import javafx.application.Application;
import javafx.scene.Scene;
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
        HomePageView homePage = new HomePageView(this::showReportPage, this::showAuthPage, this::showUsefulPage);
        if (scene == null) {
            scene = new Scene(homePage.build(), 1280, 750);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(homePage.build());
        }
    }

    public void showAuthPage() {
        AuthDialog authPage = new AuthDialog(this::showHomePage);
        scene.setRoot(authPage.build());
    }

    public void showReportPage() {
        ReportPageView[] ref = new ReportPageView[1];
        ref[0] = new ReportPageView(
            this::showHomePage,
            () -> showReportInfoPage(ref[0].getLat(), ref[0].getLng()),
            this::showAuthPage,
            this::showUsefulPage
        );
        scene.setRoot(ref[0].build());
    }

    public void showReportInfoPage(double lat, double lon) {
        ReportInfoPageView infoPage = new ReportInfoPageView(
            this::showReportPage,
            () -> System.out.println("Report submitted"),
            this::showUsefulPage,
            lat,
            lon
        );
        scene.setRoot(infoPage.build());
    }

    public void showUsefulPage() {
        UsefulPageView usefulPage = new UsefulPageView(this::showHomePage, this::showReportPage);
        scene.setRoot(usefulPage.build());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
