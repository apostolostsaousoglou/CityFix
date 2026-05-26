package com.citydamage.app;

import javafx.scene.layout.Pane;

public class TileMapPane extends Pane {

    private static final int    TILE_SIZE   = 256;
    private static final String TILE_URL_GR = "https://a.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png";
    private static final String TILE_URL_EN = "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}";
    private static final String USER_AGENT  = "CityDamageReporter/1.0 (educational project)";

    private boolean useEnglish = false;
    private double  centerLat  = 38.2466;
    private double  centerLon  = 21.7346;
    private int     zoom       = 13;

    public TileMapPane() {
        setStyle("-fx-background-color: #ddd8cf;");
    }

    private String buildUrl(int z, int x, int y) {
        return (useEnglish ? TILE_URL_EN : TILE_URL_GR)
            .replace("{z}", String.valueOf(z))
            .replace("{x}", String.valueOf(x))
            .replace("{y}", String.valueOf(y));
    }
}
