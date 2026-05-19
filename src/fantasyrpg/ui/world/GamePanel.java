package fantasyrpg.ui.world;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Window;
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

    private final ArrayList<Point> monsterPlaceholders =
            new ArrayList<>();
    private boolean battleTriggered;
    private boolean playerNearMonster;
    private boolean playerMovedSinceSpawn;
    private int framesSinceWorldStart;

    private static final boolean ENABLE_WORLD_BATTLE_TRIGGER = true;
    private static final int WORLD_ENTRY_GRACE_FRAMES = 120;
    private static final int MONSTER_TRIGGER_DISTANCE = 70;
    private static final int MIN_SPAWN_TO_MONSTER_DISTANCE = 140;
    private static final String MONSTER_PLACEHOLDER_TEXT =
            "monster icon placeholder,  get near to enter battle. GUI to be added";
    private static final String INTERACT_PROMPT_TEXT =
            "Press E to enter battle";

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

        setupMonsterPlaceholders();
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

        int previousX = player.x;
        int previousY = player.y;

        player.update();
        framesSinceWorldStart++;

        if (player.x != previousX || player.y != previousY) {
            playerMovedSinceSpawn = true;
        }

        checkMonsterProximity();
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

        drawMonsterPlaceholders(g2);

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

    private void setupMonsterPlaceholders() {

        monsterPlaceholders.clear();

        if (!isWorldReadyForBattleTrigger()) {
            return;
        }

        int mapPixelWidth = mapLoader.mapWidth * mapLoader.tileWidth;
        int mapPixelHeight = mapLoader.mapHeight * mapLoader.tileHeight;

        addMonsterPointsFromMapData(
                mapPixelWidth,
                mapPixelHeight
        );

        if (monsterPlaceholders.isEmpty()) {
            addDefaultMonsterPoints(
                    mapPixelWidth,
                    mapPixelHeight
            );
        }

        // Keep three placeholders even if map bounds are tight.
        while (!monsterPlaceholders.isEmpty() && monsterPlaceholders.size() < 3) {
            monsterPlaceholders.add(new Point(monsterPlaceholders.get(monsterPlaceholders.size() - 1)));
        }
    }

    private void addMonsterPointsFromMapData(
            int mapPixelWidth,
            int mapPixelHeight
    ) {

        if (mapPixelWidth <= 0 || mapPixelHeight <= 0) {
            return;
        }

        for (Point spawnPoint : mapLoader.enemySpawnPoints) {
            addMonsterPointFromMapIfValid(
                    spawnPoint.x,
                    spawnPoint.y,
                    mapPixelWidth,
                    mapPixelHeight
            );
        }
    }

    private void addMonsterPointFromMapIfValid(
            int candidateX,
            int candidateY,
            int mapPixelWidth,
            int mapPixelHeight
    ) {

        int safeX =
                clampInt(candidateX, 0, mapPixelWidth - 1);

        int safeY =
                clampInt(candidateY, 0, mapPixelHeight - 1);

        monsterPlaceholders.add(new Point(safeX, safeY));
    }

    private void addDefaultMonsterPoints(
            int mapPixelWidth,
            int mapPixelHeight
    ) {

        int spawnCenterX = mapLoader.spawnX + 32;
        int spawnCenterY = mapLoader.spawnY + 32;
        int minDistanceSquared = MIN_SPAWN_TO_MONSTER_DISTANCE * MIN_SPAWN_TO_MONSTER_DISTANCE;

        addMonsterPointIfValid(
                spawnCenterX - 100,
                spawnCenterY - 220,
                spawnCenterX,
                spawnCenterY,
                mapPixelWidth,
                mapPixelHeight,
                minDistanceSquared
        );

        addMonsterPointIfValid(
                spawnCenterX,
                spawnCenterY - 260,
                spawnCenterX,
                spawnCenterY,
                mapPixelWidth,
                mapPixelHeight,
                minDistanceSquared
        );

        addMonsterPointIfValid(
                spawnCenterX + 100,
                spawnCenterY - 220,
                spawnCenterX,
                spawnCenterY,
                mapPixelWidth,
                mapPixelHeight,
                minDistanceSquared
        );
    }

    private void drawMonsterPlaceholders(Graphics2D g2) {

        if (monsterPlaceholders.isEmpty()) {
            return;
        }

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(new Color(21, 24, 31, 220));

        Point anchorPoint = monsterPlaceholders.get(monsterPlaceholders.size() / 2);

        int anchorX = anchorPoint.x - 230;
        int anchorY = anchorPoint.y - 120;
        int textBoxWidth = 460;
        int textBoxHeight = 34;

        g2.fillRoundRect(anchorX, anchorY, textBoxWidth, textBoxHeight, 12, 12);
        g2.setColor(new Color(248, 248, 248));
        g2.drawString(MONSTER_PLACEHOLDER_TEXT, anchorX + 12, anchorY + 22);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(255, 89, 89));

        for (Point point : monsterPlaceholders) {
            drawArrow(g2, anchorX + (textBoxWidth / 2), anchorY + textBoxHeight, point.x, point.y);
            g2.fillOval(point.x - 7, point.y - 7, 14, 14);
            g2.setColor(new Color(255, 205, 205));
            g2.drawOval(point.x - 11, point.y - 11, 22, 22);
            g2.setColor(new Color(255, 89, 89));
        }

        g2.setStroke(new BasicStroke(1f));

        if (playerNearMonster) {
            drawInteractPrompt(g2);
        }
    }

    private void drawArrow(
            Graphics2D g2,
            int startX,
            int startY,
            int endX,
            int endY
    ) {

        g2.drawLine(startX, startY, endX, endY);

        double angle = Math.atan2(endY - startY, endX - startX);
        int arrowLength = 12;
        int leftX = (int) (endX - arrowLength * Math.cos(angle - Math.PI / 7));
        int leftY = (int) (endY - arrowLength * Math.sin(angle - Math.PI / 7));
        int rightX = (int) (endX - arrowLength * Math.cos(angle + Math.PI / 7));
        int rightY = (int) (endY - arrowLength * Math.sin(angle + Math.PI / 7));

        g2.drawLine(endX, endY, leftX, leftY);
        g2.drawLine(endX, endY, rightX, rightY);
    }

    private void checkMonsterProximity() {

        if (battleTriggered || !ENABLE_WORLD_BATTLE_TRIGGER || monsterPlaceholders.isEmpty()) {
            return;
        }

        if (!isBattleTriggerArmed()) {
            return;
        }

        int playerCenterX = player.x + 32;
        int playerCenterY = player.y + 32;
        int triggerDistanceSquared = MONSTER_TRIGGER_DISTANCE * MONSTER_TRIGGER_DISTANCE;
        playerNearMonster = false;

        for (Point point : monsterPlaceholders) {
            int dx = playerCenterX - point.x;
            int dy = playerCenterY - point.y;
            int distanceSquared = (dx * dx) + (dy * dy);

            if (distanceSquared <= triggerDistanceSquared) {
                playerNearMonster = true;
                break;
            }
        }

        if (playerNearMonster && keyInput.consumeInteractPressed()) {
            enterBattleScene();
        }
    }

    private void drawInteractPrompt(Graphics2D g2) {

        int boxX = player.x - 10;
        int boxY = player.y - 34;
        int boxWidth = 170;
        int boxHeight = 24;

        g2.setColor(new Color(14, 18, 24, 220));
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        g2.setColor(new Color(250, 250, 250));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g2.drawString(INTERACT_PROMPT_TEXT, boxX + 10, boxY + 16);
    }

    private void addMonsterPointIfValid(
            int candidateX,
            int candidateY,
            int spawnCenterX,
            int spawnCenterY,
            int mapPixelWidth,
            int mapPixelHeight,
            int minDistanceSquared
    ) {

        if (mapPixelWidth <= 0 || mapPixelHeight <= 0) {
            return;
        }

        int safeX =
                clampInt(candidateX, 0, mapPixelWidth - 1);
        int safeY =
                clampInt(candidateY, 0, mapPixelHeight - 1);

        if (isTooCloseToSpawn(safeX, safeY, spawnCenterX, spawnCenterY, minDistanceSquared)) {
            int fallbackY =
                    clampInt(spawnCenterY + 220, 0, mapPixelHeight - 1);

            if (!isTooCloseToSpawn(safeX, fallbackY, spawnCenterX, spawnCenterY, minDistanceSquared)) {
                safeY = fallbackY;
            }
        }

        if (isTooCloseToSpawn(safeX, safeY, spawnCenterX, spawnCenterY, minDistanceSquared)) {
            return;
        }

        monsterPlaceholders.add(new Point(safeX, safeY));
    }

    private boolean isTooCloseToSpawn(
            int pointX,
            int pointY,
            int spawnCenterX,
            int spawnCenterY,
            int minDistanceSquared
    ) {

        int dx = pointX - spawnCenterX;
        int dy = pointY - spawnCenterY;
        int distanceSquared = (dx * dx) + (dy * dy);

        return distanceSquared < minDistanceSquared;
    }

    private boolean isWorldReadyForBattleTrigger() {

        return mapLoader.mapWidth > 0
                && mapLoader.mapHeight > 0
                && mapLoader.tileWidth > 0
                && mapLoader.tileHeight > 0;
    }

    private boolean isBattleTriggerArmed() {

        return framesSinceWorldStart >= WORLD_ENTRY_GRACE_FRAMES
                && playerMovedSinceSpawn;
    }

    private void enterBattleScene() {

        battleTriggered = true;
        gameThread = null;

        SwingUtilities.invokeLater(() -> {
            Window window = SwingUtilities.getWindowAncestor(this);

            if (window != null) {
                window.dispose();
            }

            new fantasyrpg.ui.battle.GameFrame();
        });
    }

    private int clampInt(int value, int min, int max) {

        return Math.max(min, Math.min(max, value));
    }
}
