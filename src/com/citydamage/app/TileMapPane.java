package com.citydamage.app;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TileMapPane extends Pane {

    private static final int    TILE_SIZE   = 256;
    private static final String TILE_URL_GR = "https://a.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png";
    private static final String TILE_URL_EN = "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}";
    private static final String USER_AGENT  = "CityDamageReporter/1.0 (educational project)";

    private boolean useEnglish = false;
    private double  centerLat  = 38.2466;
    private double  centerLon  = 21.7346;
    private int     zoom       = 13;

    private final Map<String, Image> tileCache = new HashMap<>();
    private final ExecutorService    executor  = Executors.newFixedThreadPool(1);

    private final Group world     = new Group();
    private final Group tileLayer = new Group();

    public TileMapPane() {
        setStyle("-fx-background-color: #ddd8cf;");

        world.getChildren().add(tileLayer);
        getChildren().add(world);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        widthProperty().addListener((o, a, b)  -> layoutTiles());
        heightProperty().addListener((o, a, b) -> layoutTiles());
    }

    private void layoutTiles() {
        tileLayer.getChildren().clear();
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        double cTX  = tileX(centerLon, zoom);
        double cTY  = tileY(centerLat, zoom);
        int tilesW  = (int) Math.ceil(w / TILE_SIZE) + 2;
        int tilesH  = (int) Math.ceil(h / TILE_SIZE) + 2;
        int startX  = (int) Math.floor(cTX - tilesW / 2.0);
        int startY  = (int) Math.floor(cTY - tilesH / 2.0);
        int maxTile = 1 << zoom;

        for (int tx = startX; tx <= startX + tilesW; tx++) {
            for (int ty = startY; ty <= startY + tilesH; ty++) {
                if (ty < 0 || ty >= maxTile) continue;
                int    wtx = ((tx % maxTile) + maxTile) % maxTile;
                double px  = w / 2.0 + (tx - cTX) * TILE_SIZE;
                double py  = h / 2.0 + (ty - cTY) * TILE_SIZE;
                String key = zoom + "/" + wtx + "/" + ty;
                if (tileCache.containsKey(key)) {
                    tileLayer.getChildren().add(makeTileView(tileCache.get(key), px, py));
                } else {
                    submitFetch(wtx, ty, zoom, px, py);
                }
            }
        }
    }

    private void submitFetch(int x, int y, int z, double px, double py) {
        String key = z + "/" + x + "/" + y;
        if (tileCache.containsKey(key)) return;
        String url = buildUrl(z, x, y);
        executor.submit(() -> {
            Image img = downloadTile(url);
            Platform.runLater(() -> {
                if (img == null) return;
                tileCache.put(key, img);
                tileLayer.getChildren().add(makeTileView(img, px, py));
            });
        });
    }

    private ImageView makeTileView(Image img, double x, double y) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(TILE_SIZE);
        iv.setFitHeight(TILE_SIZE);
        iv.setSmooth(true);
        iv.setMouseTransparent(true);
        iv.setX(x); iv.setY(y);
        return iv;
    }

    // ── Network ───────────────────────────────────────────────────────────────

    private Image downloadTile(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "image/png,image/*");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(10000);
            conn.connect();
            if (conn.getResponseCode() != 200) return null;
            try (InputStream is = conn.getInputStream()) {
                ByteArrayOutputStream buf = new ByteArrayOutputStream(32768);
                byte[] chunk = new byte[8192];
                int n;
                while ((n = is.read(chunk)) != -1) buf.write(chunk, 0, n);
                return new Image(new ByteArrayInputStream(buf.toByteArray()));
            }
        } catch (Exception e) {
            return null;
        }
    }

    // ── Coordinate math ───────────────────────────────────────────────────────

    private static double tileX(double lon, int z) {
        return (lon + 180.0) / 360.0 * (1 << z);
    }

    private static double tileY(double lat, int z) {
        double r = Math.toRadians(lat);
        return (1.0 - Math.log(Math.tan(r) + 1.0 / Math.cos(r)) / Math.PI) / 2.0 * (1 << z);
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
