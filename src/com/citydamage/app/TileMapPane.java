package com.citydamage.app;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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

    private final Map<String, Image> tileCache = new LinkedHashMap<>(512, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Image> e) {
            return size() > 500;
        }
    };

    private final Map<String, ImageView> liveViews    = new HashMap<>();
    private final Set<String>            pendingFetch = new HashSet<>();
    private final ExecutorService        executor     = Executors.newFixedThreadPool(12);

    private final PauseTransition fetchDebounce = new PauseTransition(Duration.millis(60));

    private final Group world     = new Group();
    private final Group tileLayer = new Group();

    private final Circle marker;
    private final Line   crossH, crossV;

    private boolean dragEnabled  = false;
    private double  dragStartX, dragStartY;
    private double  dragStartLat, dragStartLon;
    private boolean wasDragged   = false;

    private double  pickedLat    = 38.2466;
    private double  pickedLon    = 21.7346;
    private boolean markerVisible = false;

    public TileMapPane() {
        setStyle("-fx-background-color: #ddd8cf;");

        marker = new Circle(8, Color.web("#6C63FF"));
        marker.setStroke(Color.WHITE);
        marker.setStrokeWidth(2.5);
        marker.setVisible(false);

        crossH = crossLine(); crossV = crossLine();

        world.getChildren().add(tileLayer);
        getChildren().addAll(world, crossH, crossV, marker);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        fetchDebounce.setOnFinished(e -> fetchMissingTiles());

        setOnScroll(e -> {
            if (e.getDeltaY() == 0) return;
            int newZoom = zoom + (e.getDeltaY() > 0 ? 1 : -1);
            if (newZoom < 3 || newZoom > 19) return;
            zoom = newZoom;
            pendingFetch.clear();
            layoutTiles();
            fetchDebounce.playFromStart();
            e.consume();
        });

        setOnMousePressed(e -> {
            if (!dragEnabled) return;
            dragStartX   = e.getX();   dragStartY   = e.getY();
            dragStartLat = centerLat;  dragStartLon = centerLon;
            wasDragged   = false;
        });

        setOnMouseDragged(e -> {
            if (!dragEnabled) return;
            wasDragged = true;
            double dx = e.getX() - dragStartX;
            double dy = e.getY() - dragStartY;
            world.setTranslateX(dx);
            world.setTranslateY(dy);
            double[] c = offsetToLatLon(dragStartLat, dragStartLon, -dx, -dy);
            centerLat = c[0]; centerLon = c[1];
            if (markerVisible) {
                double[] px = latLonToPixel(pickedLat, pickedLon);
                double mx = px[0] + world.getTranslateX();
                double my = px[1] + world.getTranslateY();
                marker.setCenterX(mx); marker.setCenterY(my);
                crossH.setStartX(mx - 14); crossH.setEndX(mx + 14);
                crossH.setStartY(my);      crossH.setEndY(my);
                crossV.setStartX(mx);      crossV.setEndX(mx);
                crossV.setStartY(my - 14); crossV.setEndY(my + 14);
            }
        });

        setOnMouseReleased(e -> {
            if (!dragEnabled) return;
            if (wasDragged) {
                world.setTranslateX(0); world.setTranslateY(0);
                layoutTiles();
                fetchDebounce.playFromStart();
                wasDragged = false;
            }
        });

        setOnMouseClicked(e -> {
            if (dragEnabled && e.isStillSincePress()) {
                double[] ll = pixelToLatLon(e.getX(), e.getY());
                pickedLat = ll[0]; pickedLon = ll[1];
                markerVisible = true;
                double[] px = latLonToPixel(pickedLat, pickedLon);
                marker.setCenterX(px[0]); marker.setCenterY(px[1]);
                crossH.setStartX(px[0] - 14); crossH.setEndX(px[0] + 14);
                crossH.setStartY(px[1]);       crossH.setEndY(px[1]);
                crossV.setStartX(px[0]);       crossV.setEndX(px[0]);
                crossV.setStartY(px[1] - 14);  crossV.setEndY(px[1] + 14);
                marker.setVisible(true);
                crossH.setVisible(true);
                crossV.setVisible(true);
            }
        });

        widthProperty().addListener((o, a, b)  -> { layoutTiles(); fetchDebounce.playFromStart(); });
        heightProperty().addListener((o, a, b) -> { layoutTiles(); fetchDebounce.playFromStart(); });
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void enableDrag()  { dragEnabled = true;  setCursor(Cursor.CROSSHAIR); }
    public void disableDrag() { dragEnabled = false; setCursor(Cursor.DEFAULT);   }

    public void setLanguage(boolean english) {
        if (this.useEnglish == english) return;
        this.useEnglish = english;
        tileCache.clear();
        pendingFetch.clear();
        liveViews.clear();
        tileLayer.getChildren().clear();
        layoutTiles();
        fetchDebounce.playFromStart();
    }

    public double getPickedLat() { return pickedLat; }
    public double getPickedLon() { return pickedLon; }

    public void panTo(double lat, double lon) {
        centerLat = lat; centerLon = lon;
        pickedLat = lat; pickedLon = lon;
        zoom = 16; markerVisible = true;
        pendingFetch.clear();
        layoutTiles();
        fetchDebounce.playFromStart();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void layoutTiles() {
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        world.setTranslateX(0); world.setTranslateY(0);

        double cTX  = tileX(centerLon, zoom);
        double cTY  = tileY(centerLat, zoom);
        int extra   = 2;
        int tilesW  = (int) Math.ceil(w / TILE_SIZE) + extra * 2;
        int tilesH  = (int) Math.ceil(h / TILE_SIZE) + extra * 2;
        int startX  = (int) Math.floor(cTX - tilesW / 2.0);
        int startY  = (int) Math.floor(cTY - tilesH / 2.0);
        int maxTile = 1 << zoom;

        Set<String> needed = new HashSet<>();
        for (int tx = startX; tx <= startX + tilesW; tx++) {
            for (int ty = startY; ty <= startY + tilesH; ty++) {
                if (ty < 0 || ty >= maxTile) continue;
                int wtx = ((tx % maxTile) + maxTile) % maxTile;
                needed.add(zoom + "/" + wtx + "/" + ty);
            }
        }

        liveViews.keySet().removeIf(key -> {
            if (!needed.contains(key)) {
                tileLayer.getChildren().remove(liveViews.get(key));
                return true;
            }
            return false;
        });

        for (int tx = startX; tx <= startX + tilesW; tx++) {
            for (int ty = startY; ty <= startY + tilesH; ty++) {
                if (ty < 0 || ty >= maxTile) continue;
                int    wtx = ((tx % maxTile) + maxTile) % maxTile;
                double px  = w / 2.0 + (tx - cTX) * TILE_SIZE;
                double py  = h / 2.0 + (ty - cTY) * TILE_SIZE;
                String key = zoom + "/" + wtx + "/" + ty;

                if (liveViews.containsKey(key)) {
                    ImageView iv = liveViews.get(key);
                    iv.setX(px); iv.setY(py);
                } else if (tileCache.containsKey(key)) {
                    ImageView iv = makeTileView(tileCache.get(key), px, py);
                    liveViews.put(key, iv);
                    tileLayer.getChildren().add(iv);
                }
            }
        }

        marker.setVisible(markerVisible);
        crossH.setVisible(markerVisible);
        crossV.setVisible(markerVisible);
        if (markerVisible) {
            double[] px = latLonToPixel(pickedLat, pickedLon);
            marker.setCenterX(px[0]); marker.setCenterY(px[1]);
            crossH.setStartX(px[0] - 14); crossH.setEndX(px[0] + 14);
            crossH.setStartY(px[1]);       crossH.setEndY(px[1]);
            crossV.setStartX(px[0]);       crossV.setEndX(px[0]);
            crossV.setStartY(px[1] - 14);  crossV.setEndY(px[1] + 14);
        }
    }

    private void fetchMissingTiles() {
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        double cTX  = tileX(centerLon, zoom);
        double cTY  = tileY(centerLat, zoom);
        int extra   = 2;
        int tilesW  = (int) Math.ceil(w / TILE_SIZE) + extra * 2;
        int tilesH  = (int) Math.ceil(h / TILE_SIZE) + extra * 2;
        int startX  = (int) Math.floor(cTX - tilesW / 2.0);
        int startY  = (int) Math.floor(cTY - tilesH / 2.0);
        int maxTile = 1 << zoom;

        for (int tx = startX; tx <= startX + tilesW; tx++) {
            for (int ty = startY; ty <= startY + tilesH; ty++) {
                if (ty < 0 || ty >= maxTile) continue;
                int wtx = ((tx % maxTile) + maxTile) % maxTile;
                submitFetch(wtx, ty, zoom);
            }
        }
    }

    private void submitFetch(int x, int y, int z) {
        String key = z + "/" + x + "/" + y;
        if (tileCache.containsKey(key) || pendingFetch.contains(key)) return;
        pendingFetch.add(key);
        String url = buildUrl(z, x, y);
        executor.submit(() -> {
            Image img = downloadTile(url);
            Platform.runLater(() -> {
                pendingFetch.remove(key);
                if (img == null) return;
                tileCache.put(key, img);
                if (liveViews.containsKey(key)) return;
                double w = getWidth(), h = getHeight();
                if (w <= 0 || h <= 0) return;
                double cTX = tileX(centerLon, zoom);
                double cTY = tileY(centerLat, zoom);
                String[] p = key.split("/");
                int kz = Integer.parseInt(p[0]);
                int kx = Integer.parseInt(p[1]);
                int ky = Integer.parseInt(p[2]);
                if (kz != zoom) return;
                double screenPx = w / 2.0 + (kx - cTX) * TILE_SIZE;
                double screenPy = h / 2.0 + (ky - cTY) * TILE_SIZE;
                if (screenPx > -TILE_SIZE && screenPx < w + TILE_SIZE &&
                    screenPy > -TILE_SIZE && screenPy < h + TILE_SIZE) {
                    ImageView iv = makeTileView(img, screenPx, screenPy);
                    liveViews.put(key, iv);
                    tileLayer.getChildren().add(iv);
                }
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

    private static Line crossLine() {
        Line l = new Line();
        l.setStroke(Color.web("#6C63FF"));
        l.setStrokeWidth(2);
        l.setVisible(false);
        return l;
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

    private double[] offsetToLatLon(double lat, double lon, double dx, double dy) {
        double tx = tileX(lon, zoom) + dx / TILE_SIZE;
        double ty = tileY(lat, zoom) + dy / TILE_SIZE;
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
