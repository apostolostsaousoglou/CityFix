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

    // ── Coordinate math ───────────────────────────────────────────────────────

    private static double tileX(double lon, int z) {
        return (lon + 180.0) / 360.0 * (1 << z);
    }

    private static double tileY(double lat, int z) {
        double r = Math.toRadians(lat);
        return (1.0 - Math.log(Math.tan(r) + 1.0 / Math.cos(r)) / Math.PI) / 2.0 * (1 << z);
    }

    private double[] latLonToPixel(double lat, double lon) {
        return new double[]{
            getWidth()  / 2.0 + (tileX(lon, zoom) - tileX(centerLon, zoom)) * TILE_SIZE,
            getHeight() / 2.0 + (tileY(lat, zoom) - tileY(centerLat, zoom)) * TILE_SIZE
        };
    }

    private double[] pixelToLatLon(double px, double py) {
        double tx = tileX(centerLon, zoom) + (px - getWidth()  / 2.0) / TILE_SIZE;
        double ty = tileY(centerLat, zoom) + (py - getHeight() / 2.0) / TILE_SIZE;
        return new double[]{
            Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1.0 - 2.0 * ty / (1 << zoom))))),
            tx / (1 << zoom) * 360.0 - 180.0
        };
    }

    private String buildUrl(int z, int x, int y) {
        return (useEnglish ? TILE_URL_EN : TILE_URL_GR)
            .replace("{z}", String.valueOf(z))
            .replace("{x}", String.valueOf(x))
            .replace("{y}", String.valueOf(y));
    }
}
