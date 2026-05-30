package com.citydamage.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private Scene scene;
    private DatabaseManager.UserRecord currentUser = null;

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
        HomePageView homePage = new HomePageView(
            this::showReportPage, this::showAuthPage, this::showUsefulPage,
            this::showReportsPage, this::logout, currentUser,
            updatedUser -> { currentUser = updatedUser; showHomePage(); });
        if (scene == null) {
            scene = new Scene(homePage.build(), 1280, 750);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(homePage.build());
        }
    }

    public void logout() {
        currentUser = null;
        showHomePage();
    }

    public void showAuthPage() {
        AuthDialog authPage = new AuthDialog(this::showHomePage, user -> {
            currentUser = user;
            showHomePage();
        });
        scene.setRoot(authPage.build());
    }

    public void showReportPage() {
        if (currentUser == null) {
            AuthDialog authPage = new AuthDialog(this::showHomePage, user -> {
                currentUser = user;
                showReportPage();
            });
            scene.setRoot(authPage.build());
            return;
        }
        ReportPageView[] ref = new ReportPageView[1];
        ref[0] = new ReportPageView(
            this::showHomePage,
            () -> showReportInfoPage(ref[0]),
            this::showAuthPage,
            this::showUsefulPage
        );
        scene.setRoot(ref[0].build());
    }

    public void showReportInfoPage(ReportPageView reportPage) {
        ReportInfoPageView infoPage = new ReportInfoPageView(
            this::showReportPage,
            this::showHomePage,
            this::showUsefulPage,
            reportPage.getLat(),
            reportPage.getLng(),
            reportPage.getStreet(),
            reportPage.getNumber(),
            reportPage.getZip(),
            reportPage.getArea(),
            currentUser
        );
        scene.setRoot(infoPage.build());
    }

    public void showReportsPage() {
        ReportsPageView reportsPage = new ReportsPageView(
            this::showHomePage,
            this::showReportPage,
            this::showUsefulPage,
            currentUser
        );
        scene.setRoot(reportsPage.build());
    }

    public void showUsefulPage() {
        UsefulPageView usefulPage = new UsefulPageView(this::showHomePage, this::showReportsPage);
        scene.setRoot(usefulPage.build());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
