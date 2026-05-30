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

    // — LRU tile image cache (max 1200 tiles) ———————————————————————————————
    private final Map<String, Image> tileCache = new LinkedHashMap<>(1300, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Image> e) {
            return size() > 1200;
        }
    };

    // Retained ImageViews — never destroyed between refreshes if key is still visible
    private final Map<String, ImageView> liveViews = new HashMap<>();

    private final Set<String>     pendingFetch = new HashSet<>();
    private final ExecutorService executor     = Executors.newFixedThreadPool(24);

    // — Scene graph ——————————————————————————————————————————————————————————
    // world group is translated during drag — zero-cost movement
    private final Group world      = new Group();
    private final Group tileLayer  = new Group();   // retained tile views
    private final Group fallbackLayer = new Group();// temporary fallbacks

    private final Circle marker;
    private final Line   crossH, crossV;

    // — Drag state ————————————————————————————————————————————————————————————
    private boolean dragEnabled  = false;
    private double  dragStartX, dragStartY;
    private double  dragStartLat, dragStartLon;
    private boolean wasDragged   = false;

    // — Picked location (for select mode) ————————————————————————————————————
    private double  pickedLat    = 38.2466;
    private double  pickedLon    = 21.7346;
    private boolean markerVisible = false;

    // — Report pins ————————————————————————————————————————————————————————————
    private final java.util.List<PinData> pins = new java.util.ArrayList<>();
    private final Group pinLayer = new Group();

    // — Debounce: fetch tiles 20 ms after last scroll/drag-release —————————————
    private final PauseTransition fetchDebounce = new PauseTransition(Duration.millis(20));

    // ————————————————————————————————————————————————————————————————————————————

    public TileMapPane() {
        setStyle("-fx-background-color: #ddd8cf;");

        marker = new Circle(8, Color.web("#6C63FF"));
        marker.setStroke(Color.WHITE);
        marker.setStrokeWidth(2.5);
        marker.setVisible(false);

        crossH = crossLine(); crossV = crossLine();

        world.getChildren().addAll(fallbackLayer, tileLayer);
        getChildren().addAll(world, pinLayer, crossH, crossV, marker);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        // — Scroll: zoom around cursor, debounce tile fetch ———————————————————
        fetchDebounce.setOnFinished(e -> fetchMissingTiles());

        setOnScroll(e -> {
            if (e.getDeltaY() == 0) return;
            int newZoom = zoom + (e.getDeltaY() > 0 ? 1 : -1);
            if (newZoom < 3 || newZoom > 19) return;

            // Zoom toward mouse cursor
            double mx = e.getX(), my = e.getY();
            double[] ll = pixelToLatLon(mx, my);
            zoom = newZoom;
            // Re-centre so the tile under cursor stays fixed
            centerLat = ll[0] + (centerLat - ll[0]) * 0.0; // handled via repositioning
            centerLon = ll[1] + (centerLon - ll[1]) * 0.0;

            pendingFetch.clear();
            layoutTiles();           // instantly reposition retained views
            fetchDebounce.playFromStart(); // fetch any gaps after 60 ms
            e.consume();
        });

        // — Drag: translate entire world group — no per-pixel refresh ——————————
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
            // Translate the tile world — instant, no scene-graph rebuild
            world.setTranslateX(dx);
            world.setTranslateY(dy);
            // Keep logical centre in sync for marker updates
            double[] c = offsetToLatLon(dragStartLat, dragStartLon, -dx, -dy);
            centerLat = c[0]; centerLon = c[1];
            updateMarkerPosition();
            updateAllPins();
        });

        setOnMouseReleased(e -> {
            if (!dragEnabled) return;
            if (wasDragged) {
                // Snap world back to origin, re-layout from new centre
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
                updateMarkerPosition();
                marker.setVisible(true);
                crossH.setVisible(true);
                crossV.setVisible(true);
            }
        });

        widthProperty().addListener((o, a, b)  -> { layoutTiles(); fetchDebounce.playFromStart(); });
        heightProperty().addListener((o, a, b) -> { layoutTiles(); fetchDebounce.playFromStart(); });
    }

    // — Public API ————————————————————————————————————————————————————————————

    public void enableDrag()  { dragEnabled = true;  setCursor(Cursor.CROSSHAIR); }
    public void disableDrag() { dragEnabled = false; setCursor(Cursor.DEFAULT);   }

    public void setLanguage(boolean english) {
        if (this.useEnglish == english) return;
        this.useEnglish = english;
        tileCache.clear();
        pendingFetch.clear();
        liveViews.clear();
        tileLayer.getChildren().clear();
        fallbackLayer.getChildren().clear();
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

    // — Layout: reposition retained views, add cached tiles, show fallbacks ——

    /**
     * Repositions all retained tile views for the current centre/zoom.
     * Does NOT fetch anything — zero network I/O, runs on the FX thread.
     */
    private void layoutTiles() {
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        world.setTranslateX(0); world.setTranslateY(0);

        double cTX = tileX(centerLon, zoom);
        double cTY = tileY(centerLat, zoom);
        int extra  = 3;
        int tilesW = (int) Math.ceil(w / TILE_SIZE) + extra * 2;
        int tilesH = (int) Math.ceil(h / TILE_SIZE) + extra * 2;
        int startX = (int) Math.floor(cTX - tilesW / 2.0);
        int startY = (int) Math.floor(cTY - tilesH / 2.0);
        int maxTile = 1 << zoom;

        Set<String> needed = new HashSet<>();
        for (int tx = startX; tx <= startX + tilesW; tx++) {
            for (int ty = startY; ty <= startY + tilesH; ty++) {
                if (ty < 0 || ty >= maxTile) continue;
                int wtx = ((tx % maxTile) + maxTile) % maxTile;
                needed.add(zoom + "/" + wtx + "/" + ty);
            }
        }

        // Remove views for tiles that scrolled off screen
        liveViews.keySet().removeIf(key -> {
            if (!needed.contains(key)) {
                tileLayer.getChildren().remove(liveViews.get(key));
                return true;
            }
            return false;
        });

        // Clear stale fallbacks
        fallbackLayer.getChildren().clear();

        for (int tx = startX; tx <= startX + tilesW; tx++) {
            for (int ty = startY; ty <= startY + tilesH; ty++) {
                if (ty < 0 || ty >= maxTile) continue;
                int    wtx = ((tx % maxTile) + maxTile) % maxTile;
                double px  = w / 2.0 + (tx - cTX) * TILE_SIZE;
                double py  = h / 2.0 + (ty - cTY) * TILE_SIZE;
                String key = zoom + "/" + wtx + "/" + ty;

                if (liveViews.containsKey(key)) {
                    // Already displayed — just move it
                    ImageView iv = liveViews.get(key);
                    iv.setX(px); iv.setY(py);
                } else if (tileCache.containsKey(key)) {
                    // Cached image — create view and keep it alive
                    ImageView iv = makeTileView(tileCache.get(key), px, py);
                    liveViews.put(key, iv);
                    tileLayer.getChildren().add(iv);
                } else {
                    // Not yet available — show a fallback tile while fetching
                    ImageView fb = makeFallback(wtx, ty, px, py);
                    if (fb != null) fallbackLayer.getChildren().add(fb);
                }
            }
        }

        updateMarkerPosition();
        updateAllPins();
    }

    /**
     * Fetches tiles that are visible but not yet in cache.
     * Runs on the FX thread to read liveViews safely; downloads happen on executor.
     */
    private void fetchMissingTiles() {
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        double cTX = tileX(centerLon, zoom);
        double cTY = tileY(centerLat, zoom);
        int extra  = 3;
        int tilesW = (int) Math.ceil(w / TILE_SIZE) + extra * 2;
        int tilesH = (int) Math.ceil(h / TILE_SIZE) + extra * 2;
        int startX = (int) Math.floor(cTX - tilesW / 2.0);
        int startY = (int) Math.floor(cTY - tilesH / 2.0);
        int maxTile = 1 << zoom;

        // Main tiles — submit center tiles first so the middle of the viewport
        // is always the first to appear (no visible grey in the center).
        java.util.List<int[]> mainTiles = new java.util.ArrayList<>();
        for (int tx = startX; tx <= startX + tilesW; tx++) {
            for (int ty = startY; ty <= startY + tilesH; ty++) {
                if (ty < 0 || ty >= maxTile) continue;
                int wtx = ((tx % maxTile) + maxTile) % maxTile;
                String key = zoom + "/" + wtx + "/" + ty;
                if (!tileCache.containsKey(key) && !pendingFetch.contains(key)) {
                    int dx = tx - (int) cTX, dy = ty - (int) cTY;
                    mainTiles.add(new int[]{wtx, ty, zoom, dx * dx + dy * dy});
                }
            }
        }
        mainTiles.sort((a, b) -> Integer.compare(a[3], b[3]));
        for (int[] t : mainTiles) submitFetch(t[0], t[1], t[2]);

        // Pre-fetch parent zoom levels (fallbacks for zoom-out, up to 5 levels)
        for (int dz = 1; dz <= 5; dz++) {
            int pz = zoom - dz;
            if (pz < 2) break;
            int pm = 1 << pz;
            int pSX = startX >> dz, pSY = startY >> dz;
            int pEX = (startX + tilesW) >> dz, pEY = (startY + tilesH) >> dz;
            for (int tx = pSX; tx <= pEX + 1; tx++)
                for (int ty = pSY; ty <= pEY + 1; ty++) {
                    if (ty < 0 || ty >= pm) continue;
                    int wtx = ((tx % pm) + pm) % pm;
                    submitFetch(wtx, ty, pz);
                }
        }

        // Pre-fetch child zoom levels (fallbacks for zoom-in, 2 levels)
        for (int dz = 1; dz <= 2; dz++) {
            int cz = zoom + dz;
            if (cz > 19) break;
            int cm = 1 << cz;
            int cSX = startX << dz, cSY = startY << dz;
            int cEX = (startX + tilesW) << dz, cEY = (startY + tilesH) << dz;
            for (int tx = cSX; tx <= cEX; tx++)
                for (int ty = cSY; ty <= cEY; ty++) {
                    if (ty < 0 || ty >= cm) continue;
                    int wtx = ((tx % cm) + cm) % cm;
                    submitFetch(wtx, ty, cz);
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
                // Only update the scene if this tile is currently visible
                if (liveViews.containsKey(key)) return; // already displayed
                double w = getWidth(), h = getHeight();
                if (w <= 0 || h <= 0) return;
                double cTX = tileX(centerLon, zoom);
                double cTY = tileY(centerLat, zoom);
                // Parse key to get z/x/y
                String[] p = key.split("/");
                int kz = Integer.parseInt(p[0]);
                int kx = Integer.parseInt(p[1]);
                int ky = Integer.parseInt(p[2]);
                if (kz != zoom) {
                    // A background pre-fetch arrived; just keep in cache
                    return;
                }
                double px = w / 2.0 + (kx - cTX) * TILE_SIZE;
                double py = h / 2.0 + (ky - cTY) * TILE_SIZE;
                // Check it's still on screen
                if (px > -TILE_SIZE && px < w + TILE_SIZE &&
                    py > -TILE_SIZE && py < h + TILE_SIZE) {
                    ImageView iv = makeTileView(img, px, py);
                    liveViews.put(key, iv);
                    tileLayer.getChildren().add(iv);
                    // Remove matching fallback for this tile
                    fallbackLayer.getChildren().removeIf(n -> {
                        if (n instanceof ImageView iv2)
                            return iv2.getUserData() != null && iv2.getUserData().equals(key);
                        return false;
                    });
                }
            });
        });
    }

    // — Fallback: nearest ancestor tile, cropped + scaled ———————————————————

    private ImageView makeFallback(int x, int y, double px, double py) {
        for (int dz = 1; dz <= 8; dz++) {
            int pz = zoom - dz;
            if (pz < 0) break;
            int ax = x >> dz, ay = y >> dz;
            String key = pz + "/" + ax + "/" + ay;
            if (!tileCache.containsKey(key)) continue;
            int scale   = 1 << dz;
            int srcSize = TILE_SIZE / scale;
            int offX    = (x % scale) * srcSize;
            int offY    = (y % scale) * srcSize;
            ImageView iv = new ImageView(tileCache.get(key));
            iv.setViewport(new javafx.geometry.Rectangle2D(offX, offY, srcSize, srcSize));
            iv.setFitWidth(TILE_SIZE);
            iv.setFitHeight(TILE_SIZE);
            iv.setSmooth(true);
            iv.setMouseTransparent(true);
            iv.setX(px); iv.setY(py);
            return iv;
        }
        return null;
    }

    // — Tile ImageView factory ————————————————————————————————————————————————

    private ImageView makeTileView(Image img, double x, double y) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(TILE_SIZE);
        iv.setFitHeight(TILE_SIZE);
        iv.setSmooth(true);
        iv.setMouseTransparent(true);
        iv.setX(x); iv.setY(y);
        return iv;
    }

    // — Marker ————————————————————————————————————————————————————————————————

    private void updateMarkerPosition() {
        marker.setVisible(markerVisible);
        crossH.setVisible(markerVisible);
        crossV.setVisible(markerVisible);
        if (!markerVisible) return;

        // Marker is outside world group (no translation effect)
        double[] px = latLonToPixel(pickedLat, pickedLon);
        double mx = px[0] + world.getTranslateX();
        double my = px[1] + world.getTranslateY();
        marker.setCenterX(mx); marker.setCenterY(my);
        crossH.setStartX(mx - 14); crossH.setEndX(mx + 14);
        crossH.setStartY(my);      crossH.setEndY(my);
        crossV.setStartX(mx);      crossV.setEndX(mx);
        crossV.setStartY(my - 14); crossV.setEndY(my + 14);
    }

    // — Network ———————————————————————————————————————————————————————————————

    private String buildUrl(int z, int x, int y) {
        return (useEnglish ? TILE_URL_EN : TILE_URL_GR)
            .replace("{z}", String.valueOf(z))
            .replace("{x}", String.valueOf(x))
            .replace("{y}", String.valueOf(y));
    }

    private Image downloadTile(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "image/png,image/*");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(6000);
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

    // — Coordinate math ———————————————————————————————————————————————————————

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

    private static Line crossLine() {
        Line l = new Line();
        l.setStroke(Color.web("#6C63FF"));
        l.setStrokeWidth(2);
        l.setVisible(false);
        return l;
    }

    // — Pin API ———————————————————————————————————————————————————————————————

    public void clearPins() {
        pins.clear();
        pinLayer.getChildren().clear();
    }

    /**
     * Adds a report pin at the given lat/lon.
     * @param color   CSS colour string e.g. "#ef4444"
     * @param label   Short text shown in a Tooltip on hover
     * @param onClick Called when the pin is clicked
     */
    public void addPin(double lat, double lon, String color, String label, Runnable onClick) {
        Circle dot = new Circle(9, Color.web(color));
        dot.setStroke(Color.WHITE);
        dot.setStrokeWidth(2);
        dot.setCursor(Cursor.HAND);

        // White drop-shadow ring
        Circle shadow = new Circle(11, Color.TRANSPARENT);
        shadow.setStroke(Color.color(0, 0, 0, 0.25));
        shadow.setStrokeWidth(2);
        shadow.setMouseTransparent(true);

        javafx.scene.control.Tooltip tp = new javafx.scene.control.Tooltip(label);
        tp.setStyle("-fx-font-size: 12px;");
        javafx.scene.control.Tooltip.install(dot, tp);

        dot.setOnMouseClicked(e -> { if (onClick != null) onClick.run(); e.consume(); });

        Group pinGroup = new Group(shadow, dot);
        pinLayer.getChildren().add(pinGroup);

        PinData pd = new PinData(lat, lon, pinGroup, dot, shadow);
        pins.add(pd);
        repositionPin(pd);
    }

    private void repositionPin(PinData pd) {
        double[] px = latLonToPixel(pd.lat, pd.lon);
        pd.group.setLayoutX(px[0]);
        pd.group.setLayoutY(px[1]);
    }

    private void updateAllPins() {
        for (PinData pd : pins) repositionPin(pd);
    }

    private static class PinData {
        final double lat, lon;
        final Group  group;
        final Circle dot, shadow;
        PinData(double lat, double lon, Group group, Circle dot, Circle shadow) {
            this.lat = lat; this.lon = lon;
            this.group = group; this.dot = dot; this.shadow = shadow;
        }
    }

    // — Legacy public method kept for compatibility —————————————————————————
    public void refresh() {
        layoutTiles();
        fetchDebounce.playFromStart();
    }
}
