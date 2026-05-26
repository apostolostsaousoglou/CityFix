package com.citydamage.app;

import javafx.scene.layout.Pane;

public class TileMapPane extends Pane {

    private static final int TILE_SIZE = 256;

    private double centerLat = 38.2466;
    private double centerLon = 21.7346;
    private int    zoom      = 13;

    public TileMapPane() {
        setStyle("-fx-background-color: #ddd8cf;");
    }
}
