package fantasyrpg.ui.world;

import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {

    final int screenWidth = 1280;
    final int screenHeight = 720;

    Thread gameThread;

    KeyboardInput keyInput =
            new KeyboardInput();

    Player player;

    TiledMapLoader mapLoader;

    public ArrayList<CollisionBlock> collisions =
            new ArrayList<>();

    public GamePanel() {

        this.setPreferredSize(
                new Dimension(
                        screenWidth,
                        screenHeight
                )
        );

        this.setDoubleBuffered(true);

        this.addKeyListener(keyInput);

        this.setFocusable(true);

        // =========================
        // LOAD MAP
        // =========================

        mapLoader =
                new TiledMapLoader(
                        "assets/maps/maps.tmx"
                );

        // =========================
        // COLLISION
        // =========================

        setupCollision();

        // =========================
        // PLAYER
        // =========================

        player =
                new Player(
                        this,
                        keyInput
                );
    }

    // =========================
    // COLLISION
    // =========================

    private void setupCollision() {

        for (Rectangle rect : mapLoader.collisions) {

            collisions.add(

                    new CollisionBlock(

                            rect.x,
                            rect.y,
                            rect.width,
                            rect.height
                    )
            );
        }
    }

    // =========================
    // THREAD
    // =========================

    public void startGameThread() {

        gameThread = new Thread(this);

        gameThread.start();
    }

    // =========================
    // LOOP
    // =========================

    @Override
    public void run() {

        double drawInterval =
                1000000000 / 60;

        double delta = 0;

        long lastTime =
                System.nanoTime();

        long currentTime;

        while (gameThread != null) {

            currentTime =
                    System.nanoTime();

            delta +=
                    (currentTime - lastTime)
                            / drawInterval;

            lastTime = currentTime;

            if (delta >= 1) {

                update();

                repaint();

                delta--;
            }
        }
    }

    // =========================
    // UPDATE
    // =========================

    public void update() {

        player.update();
    }

    // =========================
    // DRAW
    // =========================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        double scale =
                Math.max(
                        getWidth()
                                / (double) (mapLoader.mapWidth
                                * mapLoader.tileWidth),
                        getHeight()
                                / (double) (mapLoader.mapHeight
                                * mapLoader.tileHeight)
                );

        g2.scale(scale, scale);
        g2.translate(
                -getCameraX(scale),
                -getCameraY(scale)
        );

        drawMap(g2, false);

        player.draw(g2);

        drawMap(g2, true);

        g2.dispose();
    }

    private double getCameraX(double scale) {

        double viewportWidth =
                getWidth() / scale;

        double mapWidth =
                mapLoader.mapWidth * mapLoader.tileWidth;

        double cameraX =
                player.x + 32 - viewportWidth / 2;

        return clamp(
                cameraX,
                0,
                Math.max(0, mapWidth - viewportWidth)
        );
    }

    private double getCameraY(double scale) {

        double viewportHeight =
                getHeight() / scale;

        double mapHeight =
                mapLoader.mapHeight * mapLoader.tileHeight;

        double cameraY =
                player.y + 32 - viewportHeight / 2;

        return clamp(
                cameraY,
                0,
                Math.max(0, mapHeight - viewportHeight)
        );
    }

    private double clamp(
            double value,
            double min,
            double max
    ) {

        return Math.max(min, Math.min(max, value));
    }

    // =========================
    // DRAW MAP
    // =========================

    private void drawMap(
            Graphics2D g2,
            boolean upperLayer
    ) {

        for (int layerIndex = 0;
             layerIndex < mapLoader.mapLayers.size();
             layerIndex++) {

            if (isUpperLayer(layerIndex) != upperLayer) {
                continue;
            }

            int[][] layerData =
                    mapLoader.mapLayers.get(layerIndex);

            for (int row = 0;
                 row < mapLoader.mapHeight;
                 row++) {

                for (int col = 0;
                     col < mapLoader.mapWidth;
                     col++) {

                    int tileId =
                            layerData[row][col];

                    if (tileId == 0) {
                        continue;
                    }

                    BufferedImage tile =
                            mapLoader.tiles.get(tileId);

                    if (tile == null) {
                        continue;
                    }

                    int x =
                            col * mapLoader.tileWidth;

                    int y =
                            row * mapLoader.tileHeight;

                    int drawWidth =
                            getDrawWidth(tile);

                    int drawHeight =
                            getDrawHeight(tile);

                    g2.drawImage(
                            tile,
                            getDrawX(x),
                            getDrawY(y, drawHeight),
                            drawWidth,
                            drawHeight,
                            null
                    );
                }
            }
        }
    }

    private boolean isUpperLayer(int layerIndex) {

        String layerName =
                mapLoader.mapLayerNames.get(layerIndex);

        return layerName.equalsIgnoreCase("Object")
                || layerName.equalsIgnoreCase("Wall");
    }

    private int getDrawX(int tileX) {

        return tileX;
    }

    private int getDrawY(
            int tileY,
            int drawHeight
    ) {

        return tileY + mapLoader.tileHeight - drawHeight;
    }

    private int getDrawWidth(BufferedImage tile) {

        if (tile.getWidth() <= mapLoader.tileWidth
                && tile.getHeight() <= mapLoader.tileHeight) {

            return mapLoader.tileWidth;
        }

        return tile.getWidth();
    }

    private int getDrawHeight(BufferedImage tile) {

        if (tile.getWidth() <= mapLoader.tileWidth
                && tile.getHeight() <= mapLoader.tileHeight) {

            return mapLoader.tileHeight;
        }

        return tile.getHeight();
    }
}
