package fantasyrpg.ui.world;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {

    final int screenWidth = 1280;
    final int screenHeight = 720;
    private static final String PRIMARY_MAP_PATH =
            "assets/maps/maps.tmx";
    private static final String VARIANT_MAP_PATH =
            "assets/maps/dungeon.tmx";
    private static final int MAP_TRANSITION_COOLDOWN_FRAMES = 20;
    private static final int EDGE_TRANSITION_BAND_TILES = 1;
    private static final int SPAWN_MARGIN_TILES = 1;

    Thread gameThread;

    KeyboardInput keyInput =
            new KeyboardInput();

    Player player;

    TiledMapLoader mapLoader;

    public ArrayList<CollisionBlock> collisions =
            new ArrayList<>();

    private final ArrayList<EnemySpawnPoint> monsterPlaceholders =
            new ArrayList<>();
    private boolean battleTriggered;
    private boolean playerNearMonster;
    private boolean playerMovedSinceSpawn;
    private int framesSinceWorldStart;
    private int mapTransitionCooldownFrames;
    private String currentMapPath = PRIMARY_MAP_PATH;

    // Map Transition Fade System
    private boolean isTransitioning = false;
    private float transitionAlpha = 0f;
    private boolean transitionHalfway = false;
    private String nextMapPath = null;
    private int nextTargetX = 0;
    private int nextTargetY = 0;

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

        String mapToLoad = PRIMARY_MAP_PATH;
        if (fantasyrpg.GameState.playerX != -1) {
            mapToLoad = fantasyrpg.GameState.currentMapPath;
        }

        mapLoader =
                new TiledMapLoader(
                        mapToLoad
                );
        currentMapPath = mapToLoad;

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

        if (fantasyrpg.GameState.playerX != -1) {
            player.setPosition(fantasyrpg.GameState.playerX, fantasyrpg.GameState.playerY);
        }

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
        if (isTransitioning) {
            if (!transitionHalfway) {
                // Fade out
                transitionAlpha += 0.05f;
                if (transitionAlpha >= 1f) {
                    transitionAlpha = 1f;
                    transitionHalfway = true;
                    loadMapWithPlayerPosition(nextMapPath, nextTargetX, nextTargetY);
                }
            } else {
                // Fade in
                transitionAlpha -= 0.05f;
                if (transitionAlpha <= 0f) {
                    transitionAlpha = 0f;
                    isTransitioning = false;
                    transitionHalfway = false;
                }
            }
            return;
        }

        int previousX = player.x;
        int previousY = player.y;

        player.update();
        framesSinceWorldStart++;

        if (player.x != previousX || player.y != previousY) {
            playerMovedSinceSpawn = true;
        }

        if (mapTransitionCooldownFrames > 0) {
            mapTransitionCooldownFrames--;
        }

        checkMapTransition();
        checkMonsterProximity();
    }

    // =========================
    // DRAW
    // =========================

    private int getMonsterBaseY(EnemySpawnPoint point) {
        if ("golem".equals(EnemySpawnConfig.normalizeEnemyId(point.getEnemyId()))) {
            return point.getY() + 32;
        }
        return point.getY() + 24;
    }

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

        // Draw background layers (like Ground)
        drawMap(g2, false);

        // Dynamic Y-Sorting based on entities' feet/base position
        int playerFeetY = player.y + player.getDrawHeight();
        int playerRow = playerFeetY / mapLoader.tileHeight;

        // Draw entities that might be above the map bounds
        if (playerRow < 0) {
            player.draw(g2);
        }
        for (EnemySpawnPoint point : monsterPlaceholders) {
            int monsterBaseY = getMonsterBaseY(point);
            int monsterRow = monsterBaseY / mapLoader.tileHeight;
            if (monsterRow < 0) {
                EnemySpawnConfig.drawPlaceholderMarker(g2, point);
            }
        }

        // Draw upper layers (Object, Wall) row by row, sorting player/monsters inside
        for (int row = 0; row < mapLoader.mapHeight; row++) {
            boolean playerInRow = (playerRow == row);
            boolean playerBehind = playerInRow && (playerFeetY < (row + 1) * mapLoader.tileHeight);

            // Draw player first if they are behind objects in this row
            if (playerInRow && playerBehind) {
                player.draw(g2);
            }

            // Draw monsters first if they are behind objects in this row
            for (EnemySpawnPoint point : monsterPlaceholders) {
                int monsterBaseY = getMonsterBaseY(point);
                int monsterRow = monsterBaseY / mapLoader.tileHeight;
                if (monsterRow == row && (monsterBaseY < (row + 1) * mapLoader.tileHeight)) {
                    EnemySpawnConfig.drawPlaceholderMarker(g2, point);
                }
            }

            // Draw objects of this row
            drawMapRow(g2, row);

            // Draw player second if they are in front of objects in this row
            if (playerInRow && !playerBehind) {
                player.draw(g2);
            }

            // Draw monsters second if they are in front of objects in this row
            for (EnemySpawnPoint point : monsterPlaceholders) {
                int monsterBaseY = getMonsterBaseY(point);
                int monsterRow = monsterBaseY / mapLoader.tileHeight;
                if (monsterRow == row && (monsterBaseY >= (row + 1) * mapLoader.tileHeight)) {
                    EnemySpawnConfig.drawPlaceholderMarker(g2, point);
                }
            }
        }

        // Draw entities that might be below the map bounds
        if (playerRow >= mapLoader.mapHeight) {
            player.draw(g2);
        }
        for (EnemySpawnPoint point : monsterPlaceholders) {
            int monsterBaseY = getMonsterBaseY(point);
            int monsterRow = monsterBaseY / mapLoader.tileHeight;
            if (monsterRow >= mapLoader.mapHeight) {
                EnemySpawnConfig.drawPlaceholderMarker(g2, point);
            }
        }

        // Draw UI overlay (arrows, textbox, and interact prompt)
        drawUIOverlay(g2);

        g2.dispose();

        // Draw Screen Fade Transition Overlay
        if (isTransitioning && transitionAlpha > 0f) {
            Graphics2D gFade = (Graphics2D) g.create();
            gFade.setColor(new Color(0, 0, 0, (int)(transitionAlpha * 255)));
            gFade.fillRect(0, 0, getWidth(), getHeight());
            gFade.dispose();
        }
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

        if (PRIMARY_MAP_PATH.equals(currentMapPath)) {
            addDefaultMonsterPoints(
                    mapPixelWidth,
                    mapPixelHeight
            );
        } else {
            addMonsterPointsFromMapData(
                    mapPixelWidth,
                    mapPixelHeight
            );
        }
    }

    private void addMonsterPointsFromMapData(
            int mapPixelWidth,
            int mapPixelHeight
    ) {

        if (mapPixelWidth <= 0 || mapPixelHeight <= 0) {
            return;
        }

        int index = 0;
        for (EnemySpawnPoint spawnPoint : mapLoader.enemySpawnPoints) {
            boolean isDefeated = false;
            if (index == 0 && fantasyrpg.GameState.map2Enemy1Defeated) isDefeated = true;
            if (index == 1 && fantasyrpg.GameState.map2Enemy2Defeated) isDefeated = true;

            if (!isDefeated) {
                addMonsterPointFromMapIfValid(
                        spawnPoint.getX(),
                        spawnPoint.getY(),
                        spawnPoint.getEnemyId(),
                        mapPixelWidth,
                        mapPixelHeight
                );
            }
            index++;
        }
    }

    private void addMonsterPointFromMapIfValid(
            int candidateX,
            int candidateY,
            String enemyId,
            int mapPixelWidth,
            int mapPixelHeight
    ) {

        int safeX =
                clampInt(candidateX, 0, mapPixelWidth - 1);

        int safeY =
                clampInt(candidateY, 0, mapPixelHeight - 1);

        monsterPlaceholders.add(
                new EnemySpawnPoint(
                        safeX,
                        safeY,
                        enemyId
                )
        );
    }

    private void addDefaultMonsterPoints(
            int mapPixelWidth,
            int mapPixelHeight
    ) {

        if (fantasyrpg.GameState.map1Enemy1X == -1) {
            java.awt.Point p1 = findRandomValidPosition(mapPixelWidth, mapPixelHeight);
            fantasyrpg.GameState.map1Enemy1X = p1.x;
            fantasyrpg.GameState.map1Enemy1Y = p1.y;

            java.awt.Point p2 = findRandomValidPosition(mapPixelWidth, mapPixelHeight);
            while (p2.distance(p1) < 150) {
                p2 = findRandomValidPosition(mapPixelWidth, mapPixelHeight);
            }
            fantasyrpg.GameState.map1Enemy2X = p2.x;
            fantasyrpg.GameState.map1Enemy2Y = p2.y;
        }

        if (!fantasyrpg.GameState.map1Enemy1Defeated) {
            monsterPlaceholders.add(new EnemySpawnPoint(
                    fantasyrpg.GameState.map1Enemy1X,
                    fantasyrpg.GameState.map1Enemy1Y,
                    "goblin"
            ));
        }

        if (!fantasyrpg.GameState.map1Enemy2Defeated) {
            monsterPlaceholders.add(new EnemySpawnPoint(
                    fantasyrpg.GameState.map1Enemy2X,
                    fantasyrpg.GameState.map1Enemy2Y,
                    "golem"
            ));
        }
    }

    private java.awt.Point findRandomValidPosition(int mapPixelWidth, int mapPixelHeight) {
        java.util.Random rand = new java.util.Random();
        int margin = 64;

        while (true) {
            int x = margin + rand.nextInt(mapPixelWidth - 2 * margin);
            int y = margin + rand.nextInt(mapPixelHeight - 2 * margin);

            // Check distance from spawn
            int dx = x - 64;
            int dy = y - 64;
            if ((dx * dx) + (dy * dy) < 180 * 180) {
                continue;
            }

            // Check collision
            boolean collides = false;
            java.awt.Rectangle candidate = new java.awt.Rectangle(x, y, 32, 32);
            for (CollisionBlock block : collisions) {
                if (block.rectangle.intersects(candidate)) {
                    collides = true;
                    break;
                }
            }

            if (!collides) {
                return new java.awt.Point(x, y);
            }
        }
    }

    private void drawUIOverlay(Graphics2D g2) {

        // Draw lock warning if player is trying to transition to Map 2 but hasn't defeated both Map 1 enemies
        if (PRIMARY_MAP_PATH.equals(currentMapPath) && player.getSolidRight() >= (mapLoader.mapWidth - 2) * mapLoader.tileWidth) {
            if (!fantasyrpg.GameState.map1Enemy1Defeated || !fantasyrpg.GameState.map1Enemy2Defeated) {
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
                g2.setColor(new Color(255, 64, 64));
                String msg = "Segel belum terbuka! Kalahkan semua musuh di Map 1 terlebih dahulu.";
                int stringWidth = g2.getFontMetrics().stringWidth(msg);
                g2.drawString(msg, (screenWidth - stringWidth) / 2, 80);
            }
        }

        if (playerNearMonster) {
            drawInteractPrompt(g2);
        }
    }

    private void drawMapRow(Graphics2D g2, int row) {
        for (int layerIndex = 0;
             layerIndex < mapLoader.mapLayers.size();
             layerIndex++) {

            if (!isUpperLayer(layerIndex)) {
                continue;
            }

            int[][] layerData =
                    mapLoader.mapLayers.get(layerIndex);

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
        EnemySpawnPoint activePoint = null;

        for (EnemySpawnPoint point : monsterPlaceholders) {
            int dx = playerCenterX - point.getX();
            int dy = playerCenterY - point.getY();
            int distanceSquared = (dx * dx) + (dy * dy);

            if (distanceSquared <= triggerDistanceSquared) {
                playerNearMonster = true;
                activePoint = point;
                break;
            }
        }

        if (playerNearMonster && keyInput.consumeInteractPressed()) {
            // Save state to GameState
            fantasyrpg.GameState.currentMapPath = currentMapPath;
            fantasyrpg.GameState.playerX = player.x;
            fantasyrpg.GameState.playerY = player.y;

            if (PRIMARY_MAP_PATH.equals(currentMapPath)) {
                if (activePoint.getX() == fantasyrpg.GameState.map1Enemy1X && activePoint.getY() == fantasyrpg.GameState.map1Enemy1Y) {
                    fantasyrpg.GameState.currentEnemyIndex = 0;
                } else {
                    fantasyrpg.GameState.currentEnemyIndex = 1;
                }
            } else {
                if ("goblin".equals(activePoint.getEnemyId())) {
                    fantasyrpg.GameState.currentEnemyIndex = 0;
                } else {
                    fantasyrpg.GameState.currentEnemyIndex = 1;
                }
            }

            enterBattleScene();
        }
    }

    private void checkMapTransition() {

        if (battleTriggered || mapTransitionCooldownFrames > 0) {
            return;
        }

        if (PRIMARY_MAP_PATH.equals(currentMapPath)) {
            tryMoveToVariantMapFromRightEdge();
            return;
        }

        if (VARIANT_MAP_PATH.equals(currentMapPath)) {
            tryMoveBackToPrimaryMapFromLeftEdge();
        }
    }

    private void startMapTransition(String mapPath, int targetX, int targetY) {
        isTransitioning = true;
        transitionHalfway = false;
        transitionAlpha = 0f;
        nextMapPath = mapPath;
        nextTargetX = targetX;
        nextTargetY = targetY;
    }

    private void tryMoveToVariantMapFromRightEdge() {

        if (!keyInput.rightPressed) {
            return;
        }

        int mapPixelWidth =
                mapLoader.mapWidth * mapLoader.tileWidth;
        int transitionBandStart =
                mapPixelWidth
                        - (EDGE_TRANSITION_BAND_TILES * mapLoader.tileWidth);

        if (player.getSolidRight() < transitionBandStart) {
            return;
        }

        if (!isEdgeOpenForCurrentRow(true)) {
            return;
        }

        // Lock Map 2 until both Map 1 enemies are defeated
        if (!fantasyrpg.GameState.map1Enemy1Defeated || !fantasyrpg.GameState.map1Enemy2Defeated) {
            return;
        }

        int targetX =
                mapLoader.tileWidth * SPAWN_MARGIN_TILES;
        // Align with the dungeon entrance (y = 316 to 384) to avoid spawning in walls
        int targetY = 315;

        startMapTransition(
                VARIANT_MAP_PATH,
                targetX,
                targetY
        );
    }

    private void tryMoveBackToPrimaryMapFromLeftEdge() {

        if (!keyInput.leftPressed) {
            return;
        }

        int transitionBandEnd =
                EDGE_TRANSITION_BAND_TILES * mapLoader.tileWidth;

        if (player.getSolidLeft() > transitionBandEnd) {
            return;
        }

        if (!isEdgeOpenForCurrentRow(false)) {
            return;
        }

        int mapPixelWidth =
                mapLoader.mapWidth * mapLoader.tileWidth;
        int targetX =
                mapPixelWidth
                        - ((SPAWN_MARGIN_TILES + 1) * mapLoader.tileWidth);
        // Align with the primary map entrance (y = 384 to 462) to avoid spawning in walls (shifted 1 tile up to avoid tree collision)
        int targetY = 388;

        startMapTransition(
                PRIMARY_MAP_PATH,
                targetX,
                targetY
        );
    }

    private boolean isEdgeOpenForCurrentRow(boolean rightEdge) {

        int wallLayerIndex = findLayerIndexByName("Wall");

        if (wallLayerIndex < 0) {
            return true;
        }

        int col =
                rightEdge
                        ? mapLoader.mapWidth - 1
                        : 0;

        int topRow =
                clampInt(
                        player.getSolidTop() / mapLoader.tileHeight,
                        0,
                        mapLoader.mapHeight - 1
                );
        int bottomRow =
                clampInt(
                        (player.getSolidBottom() - 1)
                                / mapLoader.tileHeight,
                        0,
                        mapLoader.mapHeight - 1
                );

        int[][] wallLayer =
                mapLoader.mapLayers.get(wallLayerIndex);

        for (int row = topRow; row <= bottomRow; row++) {
            if (wallLayer[row][col] != 0) {
                return false;
            }
        }

        return true;
    }

    private int findLayerIndexByName(String layerName) {

        for (int i = 0; i < mapLoader.mapLayerNames.size(); i++) {
            String currentLayerName =
                    mapLoader.mapLayerNames.get(i);

            if (currentLayerName.equalsIgnoreCase(layerName)) {
                return i;
            }
        }

        return -1;
    }

    private void loadMapWithPlayerPosition(
            String mapPath,
            int targetX,
            int targetY
    ) {

        TiledMapLoader nextMapLoader =
                new TiledMapLoader(mapPath);

        mapLoader = nextMapLoader;
        currentMapPath = mapPath;

        collisions.clear();
        setupCollision();

        int maxX =
                (mapLoader.mapWidth * mapLoader.tileWidth) - player.getDrawWidth();
        int maxY =
                (mapLoader.mapHeight * mapLoader.tileHeight) - player.getDrawHeight();

        player.setPosition(
                clampInt(targetX, 0, Math.max(0, maxX)),
                clampInt(targetY, 0, Math.max(0, maxY))
        );

        setupMonsterPlaceholders();
        framesSinceWorldStart = 0;
        playerMovedSinceSpawn = false;
        playerNearMonster = false;
        mapTransitionCooldownFrames = MAP_TRANSITION_COOLDOWN_FRAMES;
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
            String enemyId,
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

        monsterPlaceholders.add(
                new EnemySpawnPoint(
                        safeX,
                        safeY,
                        enemyId
                )
        );
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
