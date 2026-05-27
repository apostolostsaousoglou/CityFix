package com.citydamage.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UsefulPageView {

    private final Runnable onHome;
    private final Runnable onReport;

    public UsefulPageView(Runnable onHome, Runnable onReport) {
        this.onHome   = onHome;
        this.onReport = onReport;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        Button backBtn = new Button("← Home");
        backBtn.getStyleClass().add("login-btn");
        backBtn.setOnAction(e -> { if (onHome != null) onHome.run(); });
        HBox topBar = new HBox(backBtn);
        topBar.setPadding(new Insets(12, 32, 12, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("navbar-container");
        root.setTop(topBar);

        VBox col = new VBox(22,
            card("Police", new String[][]{
                {"1st Police Department", "261 440 9750", "138 Panepistimiou St., Anthoupoli, 264 43 Patras"},
                {"2nd Police Department", "261 089 5282", "95 Ermou St., 26110 Patras"},
                {"3rd Police Department", "261 034 4850", "Filippou & Olympiados – Nikopoleos, 26332 Patras"},
                {"Traffic Department",    "261 440 9770", "138 Panepistimiou St., Anthoupoli, 264 43 Patras"},
            }),
            card("Fire Service", new String[][]{
                {"1st Fire Station", "261 023 3211", "Klirou Agion Martiron, 26335 Patras"},
                {"2nd Fire Station", "261 034 4880", "Klirou Agion Martiron, Patras"},
            }),
            card("DEYAP", new String[][]{
                {"Patras Service", "2610 566 184", "81 Pavlou St., 262 32 Patras"},
            }),
            card("DEI — Public Power", new String[][]{
                {"Patras Service", "261 064 2793", "95 Ermou St., 26110 Patras"},
            }),
            card("Municipality of Patras", new String[][]{
                {"Patras Service", "261 461 6336", "7 Gounarei St., 26332 Patras"},
            })
        );
        col.setAlignment(Pos.TOP_CENTER);
        col.setMaxWidth(700);

        HBox centred = new HBox(col);
        centred.setAlignment(Pos.TOP_CENTER);
        centred.setPadding(new Insets(48, 40, 48, 40));

        ScrollPane sp = new ScrollPane(centred);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(sp);

        return root;
    }

    private VBox card(String title, String[][] rows) {
        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.15);");
        sep.setPadding(new Insets(2, 0, 4, 0));

        VBox box = new VBox(10, lbl, sep);
        box.setStyle(
            "-fx-background-color: rgba(14, 14, 32, 0.76);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255,255,255,0.18);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 16;" +
            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.55),22,0,0,6);"
        );
        box.setPadding(new Insets(24, 28, 24, 28));
        box.setMaxWidth(700);

        for (String[] r : rows) box.getChildren().add(buildEntry(r[0], r[1], r[2]));
        return box;
    }

    private VBox buildEntry(String name, String phone, String address) {
        Label nameLbl = new Label(name + " –");
        nameLbl.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label phonePfx = new Label("Phone:");
        phonePfx.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        Label phoneLbl = new Label(phone);
        phoneLbl.setStyle("-fx-text-fill: #c084fc; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox phoneRow = new HBox(6, phonePfx, phoneLbl);
        phoneRow.setAlignment(Pos.CENTER_LEFT);

        Label addrPfx = new Label("Address:");
        addrPfx.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        Label addrLbl = new Label(address);
        addrLbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
        addrLbl.setWrapText(true);
        HBox addrRow = new HBox(6, addrPfx, addrLbl);
        addrRow.setAlignment(Pos.TOP_LEFT);

        VBox entry = new VBox(3, nameLbl, phoneRow, addrRow);
        entry.setPadding(new Insets(8, 0, 4, 0));
        return entry;
    }
}
