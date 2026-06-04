package fantasyrpg.ui.world;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {

    final int screenWidth  = 1280;
    final int screenHeight = 720;

    private static final String PRIMARY_MAP_PATH = "assets/maps/maps.tmx";
    private static final String VARIANT_MAP_PATH = "assets/maps/dungeon.tmx";

    private static final int MAP_TRANSITION_COOLDOWN_FRAMES = 20;
    private static final int EDGE_TRANSITION_BAND_TILES     = 1;
    private static final int SPAWN_MARGIN_TILES             = 1;

    Thread gameThread;

    KeyboardInput keyInput = new KeyboardInput();
    Player player;
    TiledMapLoader mapLoader;

    public ArrayList<CollisionBlock> collisions = new ArrayList<>();

    private final ArrayList<EnemySpawnPoint> monsterPlaceholders = new ArrayList<>();
    private boolean battleTriggered;
    private boolean playerNearMonster;
    private boolean playerMovedSinceSpawn;
    private int framesSinceWorldStart;
    private int mapTransitionCooldownFrames;
    private String currentMapPath = PRIMARY_MAP_PATH;

    
    private boolean isTransitioning    = false;
    private float   transitionAlpha    = 0f;
    private boolean transitionHalfway  = false;
    private String  nextMapPath        = null;
    private int     nextTargetX        = 0;
    private int     nextTargetY        = 0;

    
    private String  hudMessage         = "";
    private int     hudMessageFrames   = 0;
    private Color   hudMessageColor    = Color.WHITE;

    
    private float   dangerPulse        = 0f;
    private float   dangerPulseDir     = 1f;
    private EnemySpawnPoint nearestEnemy = null;

    private static final boolean ENABLE_WORLD_BATTLE_TRIGGER = true;
    private static final int WORLD_ENTRY_GRACE_FRAMES    = 120;
    private static final int MONSTER_TRIGGER_DISTANCE    = 70;
    private static final String INTERACT_PROMPT_TEXT     = "E  ─  Enter Battle";

    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setDoubleBuffered(true);
        addKeyListener(keyInput);
        setFocusable(true);

        
        String mapToLoad = PRIMARY_MAP_PATH;
        if (fantasyrpg.GameState.playerX != -1) mapToLoad = fantasyrpg.GameState.currentMapPath;

        mapLoader     = new TiledMapLoader(mapToLoad);
        currentMapPath = mapToLoad;

        setupCollision();
        player = new Player(this, keyInput);
        if (fantasyrpg.GameState.playerX != -1) player.setPosition(fantasyrpg.GameState.playerX, fantasyrpg.GameState.playerY);

        setupMonsterPlaceholders();
        if (currentMapPath != null && currentMapPath.contains("dungeon")) {
            fantasyrpg.sound.SoundManager.playBGM("DUNGEON");
        } else {
            fantasyrpg.sound.SoundManager.playBGM("WORLD");
        }
    }

    
    private void setupCollision() {
        collisions.clear();
        for (Rectangle r : mapLoader.collisions) {
            collisions.add(new CollisionBlock(r.x, r.y, r.width, r.height));
        }
    }

    
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / 60.0;
        double delta = 0;
        long lastTime = System.nanoTime();
        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - lastTime) / drawInterval;
            lastTime = now;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    
    public void update() {
        if (isTransitioning) {
            updateFade();
            return;
        }

        int prevX = player.x, prevY = player.y;
        player.update();
        framesSinceWorldStart++;

        if (player.x != prevX || player.y != prevY) playerMovedSinceSpawn = true;
        if (mapTransitionCooldownFrames > 0) mapTransitionCooldownFrames--;

        
        dangerPulse += dangerPulseDir * 0.05f;
        if (dangerPulse > 1f) { dangerPulse = 1f; dangerPulseDir = -1f; }
        if (dangerPulse < 0f) { dangerPulse = 0f; dangerPulseDir =  1f; }

        
        if (hudMessageFrames > 0) hudMessageFrames--;

        checkMapTransition();
        checkMonsterProximity();
    }

    private void updateFade() {
        if (!transitionHalfway) {
            transitionAlpha += 0.05f;
            if (transitionAlpha >= 1f) {
                transitionAlpha   = 1f;
                transitionHalfway = true;
                loadMapWithPlayerPosition(nextMapPath, nextTargetX, nextTargetY);
            }
        } else {
            transitionAlpha -= 0.05f;
            if (transitionAlpha <= 0f) {
                transitionAlpha   = 0f;
                isTransitioning   = false;
                transitionHalfway = false;
            }
        }
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        double scale = Math.max(
                getWidth()  / (double)(mapLoader.mapWidth  * mapLoader.tileWidth),
                getHeight() / (double)(mapLoader.mapHeight * mapLoader.tileHeight));

        g2.scale(scale, scale);
        g2.translate(-getCameraX(scale), -getCameraY(scale));

        drawMap(g2, false);

        
        int playerFeetY = player.y + player.getDrawHeight();
        int playerRow   = playerFeetY / mapLoader.tileHeight;

        
        if (playerRow < 0) player.draw(g2);
        for (EnemySpawnPoint pt : monsterPlaceholders) {
            if (getMonsterRow(pt) < 0) drawEnemyWithAura(g2, pt);
        }

        
        for (int row = 0; row < mapLoader.mapHeight; row++) {
            boolean inRow   = (playerRow == row);
            boolean behind  = inRow && (playerFeetY < (row + 1) * mapLoader.tileHeight);

            if (inRow && behind)  player.draw(g2);
            for (EnemySpawnPoint pt : monsterPlaceholders)
                if (getMonsterRow(pt) == row && getMonsterBaseY(pt) < (row + 1) * mapLoader.tileHeight)
                    drawEnemyWithAura(g2, pt);

            drawMapRow(g2, row);

            if (inRow && !behind) player.draw(g2);
            for (EnemySpawnPoint pt : monsterPlaceholders)
                if (getMonsterRow(pt) == row && getMonsterBaseY(pt) >= (row + 1) * mapLoader.tileHeight)
                    drawEnemyWithAura(g2, pt);
        }

        
        if (playerRow >= mapLoader.mapHeight) player.draw(g2);
        for (EnemySpawnPoint pt : monsterPlaceholders)
            if (getMonsterRow(pt) >= mapLoader.mapHeight) drawEnemyWithAura(g2, pt);

        
        drawWorldUI(g2);
        g2.dispose();

        
        Graphics2D gHud = (Graphics2D) g.create();
        gHud.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gHud.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawScreenHUD(gHud);
        gHud.dispose();

        
        if (isTransitioning && transitionAlpha > 0f) {
            Graphics2D gFade = (Graphics2D) g.create();
            gFade.setColor(new Color(0, 0, 0, (int)(transitionAlpha * 255)));
            gFade.fillRect(0, 0, getWidth(), getHeight());
            gFade.dispose();
        }
    }

    
    private void drawEnemyWithAura(Graphics2D g2, EnemySpawnPoint pt) {
        
        if (nearestEnemy == pt) {
            int cx = pt.getX();
            int cy = pt.getY();
            String enemyId = EnemySpawnConfig.normalizeEnemyId(pt.getEnemyId());
            int r;
            Color auraColor;
            if ("dragonboss".equals(enemyId)) {
                r = 64;
                auraColor = new Color(160, 40, 200); 
            } else if ("golem".equals(enemyId) || "orc_warrior".equals(enemyId)) {
                r = 52;
                auraColor = "golem".equals(enemyId) ? new Color(180, 100, 20) : new Color(200, 80, 20); 
            } else {
                r = 38;
                auraColor = new Color(80, 200, 60); 
            }
            float auraAlpha = 0.15f + dangerPulse * 0.20f;
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, auraAlpha));
            g2.setColor(auraColor);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setComposite(old);
        }
        EnemySpawnConfig.drawPlaceholderMarker(g2, pt);
    }

    private boolean isGolemPoint(EnemySpawnPoint pt) {
        return "golem".equals(EnemySpawnConfig.normalizeEnemyId(pt.getEnemyId()));
    }

    private boolean isDragonPoint(EnemySpawnPoint pt) {
        return "dragonboss".equals(EnemySpawnConfig.normalizeEnemyId(pt.getEnemyId()));
    }

    
    private void drawWorldUI(Graphics2D g2) {
        
        if (playerNearMonster && nearestEnemy != null) {
            if (isMonsterLocked(nearestEnemy)) {
                drawLockedMonsterWarning(g2, nearestEnemy);
            } else {
                drawInteractPrompt(g2);
            }
        }
    }

    private void drawScreenHUD(Graphics2D g) {
        int W = getWidth(), H = getHeight();

        
        String mapName = PRIMARY_MAP_PATH.equals(currentMapPath) ? "MAP 1  ─  Village Outskirts" : "MAP 2  ─  The Dungeon";
        drawHudBadge(g, 18, 18, mapName, new Color(180, 140, 60));

        
        int remaining = monsterPlaceholders.size();
        String enemyStatus = remaining == 0 ? "All enemies defeated!" : "Enemies remaining: " + remaining;
        Color statusColor = remaining == 0 ? new Color(80, 230, 100) : new Color(240, 100, 80);
        drawHudBadge(g, W - 280, 18, enemyStatus, statusColor);

        
        drawControlsHint(g, W, H);

        
        if (PRIMARY_MAP_PATH.equals(currentMapPath)) {
            boolean allDefeated = fantasyrpg.GameState.map1Enemy1Defeated
                    && fantasyrpg.GameState.map1Enemy2Defeated;
            if (!allDefeated) {
                int remaining1 = (fantasyrpg.GameState.map1Enemy1Defeated ? 0 : 1)
                        + (fantasyrpg.GameState.map1Enemy2Defeated ? 0 : 1);
                drawLockWarning(g, W, H, remaining1);
            }
        }

        
        if (hudMessageFrames > 0) {
            float alpha = Math.min(1f, hudMessageFrames / 30f);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
            FontMetrics fm = g.getFontMetrics();
            String msg = hudMessage;
            int mx = (W - fm.stringWidth(msg)) / 2;
            int my = H / 2 - 60;
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(mx - 18, my - 28, fm.stringWidth(msg) + 36, 42, 12, 12);
            g.setColor(hudMessageColor);
            g.drawString(msg, mx, my);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        
        if (playerNearMonster) {
            float alpha = 0.06f + dangerPulse * 0.10f;
            g.setColor(new Color(255, 30, 30, (int)(alpha * 255)));
            g.setStroke(new BasicStroke(8));
            g.drawRect(4, 4, W - 8, H - 8);
            g.setStroke(new BasicStroke(1));
        }
    }

    private void drawHudBadge(Graphics2D g, int x, int y, String text, Color accent) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int bw = fm.stringWidth(text) + 24;
        int bh = 30;
        g.setColor(new Color(8, 10, 14, 210));
        g.fillRoundRect(x, y, bw, bh, 8, 8);
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, bw, bh, 8, 8);
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(0, 0, 0, 100));
        g.drawString(text, x + 13, y + 21);
        g.setColor(accent.brighter());
        g.drawString(text, x + 12, y + 20);
    }

    private void drawControlsHint(Graphics2D g, int W, int H) {
        String[] hints = {"WASD  ─  Move", "E  ─  Interact", "Right Edge  ─  Next Map"};
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int lineH = 18;
        int totalH = hints.length * lineH + 16;
        int boxW = 190;
        int bx = 18, by = H - totalH - 18;
        g.setColor(new Color(8, 10, 14, 190));
        g.fillRoundRect(bx, by, boxW, totalH, 8, 8);
        g.setColor(new Color(100, 90, 70, 180));
        g.drawRoundRect(bx, by, boxW, totalH, 8, 8);
        int ty = by + 20;
        for (String hint : hints) {
            g.setColor(new Color(160, 150, 120));
            g.drawString(hint, bx + 12, ty);
            ty += lineH;
        }
    }

    private void drawLockWarning(Graphics2D g, int W, int H, int remaining) {
        
        int mapPxW = mapLoader.mapWidth * mapLoader.tileWidth;
        int approxPlayerWorldX = player.x;
        if (approxPlayerWorldX < mapPxW - 200) return;

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        String msg1 = "🔒  Dungeon Sealed!  Defeat " + remaining + " more enem" + (remaining == 1 ? "y" : "ies") + " first.";
        FontMetrics fm = g.getFontMetrics();
        int bw = fm.stringWidth(msg1) + 32;
        int bh = 36;
        int bx = (W - bw) / 2;
        int by = 60;
        g.setColor(new Color(100, 10, 10, 200));
        g.fillRoundRect(bx, by, bw, bh, 10, 10);
        g.setColor(new Color(220, 50, 50, 220));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(bx, by, bw, bh, 10, 10);
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(255, 130, 130));
        g.drawString(msg1, bx + 16, by + 24);
    }

    private void drawInteractPrompt(Graphics2D g2) {
        
        int bx = player.x - 18;
        int by = player.y - 42;
        int bw = 196;
        int bh = 30;

        
        float pa = 0.7f + dangerPulse * 0.3f;
        g2.setColor(new Color(10, 14, 20, (int)(pa * 220)));
        g2.fillRoundRect(bx, by, bw, bh, 10, 10);
        g2.setColor(new Color(255, 210, 60, 200));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 10, 10);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        g2.setColor(new Color(255, 230, 120));
        g2.drawString(INTERACT_PROMPT_TEXT, bx + 12, by + 21);
    }

    
    private double getCameraX(double scale) {
        double vpW  = getWidth() / scale;
        double mapW = mapLoader.mapWidth * mapLoader.tileWidth;
        return clamp(player.x + 32 - vpW / 2, 0, Math.max(0, mapW - vpW));
    }

    private double getCameraY(double scale) {
        double vpH  = getHeight() / scale;
        double mapH = mapLoader.mapHeight * mapLoader.tileHeight;
        return clamp(player.y + 32 - vpH / 2, 0, Math.max(0, mapH - vpH));
    }

    private double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
    private int    clampInt(int v, int min, int max)        { return Math.max(min, Math.min(max, v)); }

    private void drawMap(Graphics2D g2, boolean upper) {
        for (int li = 0; li < mapLoader.mapLayers.size(); li++) {
            if (isUpperLayer(li) != upper) continue;
            int[][] ld = mapLoader.mapLayers.get(li);
            for (int row = 0; row < mapLoader.mapHeight; row++)
                for (int col = 0; col < mapLoader.mapWidth; col++)
                    drawTile(g2, ld, row, col);
        }
    }

    private void drawMapRow(Graphics2D g2, int row) {
        for (int li = 0; li < mapLoader.mapLayers.size(); li++) {
            if (!isUpperLayer(li)) continue;
            int[][] ld = mapLoader.mapLayers.get(li);
            for (int col = 0; col < mapLoader.mapWidth; col++)
                drawTile(g2, ld, row, col);
        }
    }

    private void drawTile(Graphics2D g2, int[][] ld, int row, int col) {
        int tid = ld[row][col];
        if (tid == 0) return;
        BufferedImage tile = mapLoader.tiles.get(tid);
        if (tile == null) return;
        int x  = col * mapLoader.tileWidth;
        int y  = row * mapLoader.tileHeight;
        int dw = getDrawWidth(tile);
        int dh = getDrawHeight(tile);
        g2.drawImage(tile, x, y + mapLoader.tileHeight - dh, dw, dh, null);
    }

    private boolean isUpperLayer(int li) {
        String n = mapLoader.mapLayerNames.get(li);
        return n.equalsIgnoreCase("Object") || n.equalsIgnoreCase("Wall");
    }

    private int getDrawWidth(BufferedImage t) {
        return (t.getWidth() <= mapLoader.tileWidth && t.getHeight() <= mapLoader.tileHeight)
                ? mapLoader.tileWidth : t.getWidth();
    }

    private int getDrawHeight(BufferedImage t) {
        return (t.getWidth() <= mapLoader.tileWidth && t.getHeight() <= mapLoader.tileHeight)
                ? mapLoader.tileHeight : t.getHeight();
    }

    
    private int getMonsterBaseY(EnemySpawnPoint pt) {
        String enemyId = EnemySpawnConfig.normalizeEnemyId(pt.getEnemyId());
        if ("golem".equals(enemyId) || "dragonboss".equals(enemyId) || "orc_warrior".equals(enemyId)) {
            return pt.getY() + 32;
        }
        return pt.getY() + 24;
    }

    private int getMonsterRow(EnemySpawnPoint pt) {
        return getMonsterBaseY(pt) / mapLoader.tileHeight;
    }

    
    private void setupMonsterPlaceholders() {
        monsterPlaceholders.clear();
        if (!isWorldReadyForBattleTrigger()) return;

        int mapPxW = mapLoader.mapWidth  * mapLoader.tileWidth;
        int mapPxH = mapLoader.mapHeight * mapLoader.tileHeight;

        if (PRIMARY_MAP_PATH.equals(currentMapPath)) addDefaultMonsterPoints(mapPxW, mapPxH);
        else                                          addMonsterPointsFromMapData(mapPxW, mapPxH);
    }

    private void addDefaultMonsterPoints(int mapPxW, int mapPxH) {
        if (fantasyrpg.GameState.map1Enemy1X == -1) {
            java.awt.Point p1 = findRandomValidPosition(mapPxW, mapPxH);
            fantasyrpg.GameState.map1Enemy1X = p1.x;
            fantasyrpg.GameState.map1Enemy1Y = p1.y;
            java.awt.Point p2 = findRandomValidPosition(mapPxW, mapPxH);
            while (p2.distance(p1) < 150) p2 = findRandomValidPosition(mapPxW, mapPxH);
            fantasyrpg.GameState.map1Enemy2X = p2.x;
            fantasyrpg.GameState.map1Enemy2Y = p2.y;
        }
        if (!fantasyrpg.GameState.map1Enemy1Defeated)
            monsterPlaceholders.add(new EnemySpawnPoint(fantasyrpg.GameState.map1Enemy1X, fantasyrpg.GameState.map1Enemy1Y, "slime"));
        if (!fantasyrpg.GameState.map1Enemy2Defeated)
            monsterPlaceholders.add(new EnemySpawnPoint(fantasyrpg.GameState.map1Enemy2X, fantasyrpg.GameState.map1Enemy2Y, "golem"));
    }

    private void addMonsterPointsFromMapData(int mapPxW, int mapPxH) {
        for (EnemySpawnPoint sp : mapLoader.enemySpawnPoints) {
            String id = EnemySpawnConfig.normalizeEnemyId(sp.getEnemyId());
            boolean defeated = switch (id) {
                case "goblin" -> fantasyrpg.GameState.map2Enemy1Defeated;
                case "orc_warrior" -> fantasyrpg.GameState.map2Enemy2Defeated;
                case "dragonboss" -> fantasyrpg.GameState.map2Enemy3Defeated;
                default -> false;
            };
            if (!defeated) {
                int sx = clampInt(sp.getX(), 0, mapPxW - 1);
                int sy = clampInt(sp.getY(), 0, mapPxH - 1);
                monsterPlaceholders.add(new EnemySpawnPoint(sx, sy, sp.getEnemyId()));
            }
        }
    }

    private java.awt.Point findRandomValidPosition(int mapPxW, int mapPxH) {
        java.util.Random rand = new java.util.Random();
        int margin = 64;
        while (true) {
            int x = margin + rand.nextInt(mapPxW - 2 * margin);
            int y = margin + rand.nextInt(mapPxH - 2 * margin);
            int dx = x - 64, dy = y - 64;
            if ((dx * dx + dy * dy) < 180 * 180) continue;
            boolean col = false;
            Rectangle c = new Rectangle(x, y, 32, 32);
            for (CollisionBlock b : collisions) { if (b.rectangle.intersects(c)) { col = true; break; } }
            if (!col) return new java.awt.Point(x, y);
        }
    }

    
    private void checkMonsterProximity() {
        if (battleTriggered || !ENABLE_WORLD_BATTLE_TRIGGER || monsterPlaceholders.isEmpty()) return;
        if (!isBattleTriggerArmed()) return;

        int pcx = player.x + 32;
        int pcy = player.y + 32;
        int trigSq = MONSTER_TRIGGER_DISTANCE * MONSTER_TRIGGER_DISTANCE;
        playerNearMonster = false;
        nearestEnemy      = null;
        int bestDist      = Integer.MAX_VALUE;

        for (EnemySpawnPoint pt : monsterPlaceholders) {
            int dx = pcx - pt.getX(), dy = pcy - pt.getY();
            int dist = dx * dx + dy * dy;
            if (dist <= trigSq) {
                playerNearMonster = true;
                if (dist < bestDist) { bestDist = dist; nearestEnemy = pt; }
            }
        }

        if (playerNearMonster && keyInput.consumeInteractPressed()) {
            if (isMonsterLocked(nearestEnemy)) {
                return;
            }
            fantasyrpg.GameState.currentMapPath = currentMapPath;
            fantasyrpg.GameState.playerX        = player.x;
            fantasyrpg.GameState.playerY        = player.y;

            if (PRIMARY_MAP_PATH.equals(currentMapPath)) {
                fantasyrpg.GameState.currentEnemyIndex =
                        (nearestEnemy.getX() == fantasyrpg.GameState.map1Enemy1X
                                && nearestEnemy.getY() == fantasyrpg.GameState.map1Enemy1Y) ? 0 : 1;
            } else {
                String enemyId = EnemySpawnConfig.normalizeEnemyId(nearestEnemy.getEnemyId());
                if ("goblin".equals(enemyId)) {
                    fantasyrpg.GameState.currentEnemyIndex = 0;
                } else if ("orc_warrior".equals(enemyId)) {
                    fantasyrpg.GameState.currentEnemyIndex = 1;
                } else {
                    fantasyrpg.GameState.currentEnemyIndex = 2;
                }
            }
            enterBattleScene();
        }
    }

    private boolean isMonsterLocked(EnemySpawnPoint pt) {
        if (pt == null) return false;
        if (PRIMARY_MAP_PATH.equals(currentMapPath)) {
            
            boolean isGolem = pt.getX() == fantasyrpg.GameState.map1Enemy2X
                    && pt.getY() == fantasyrpg.GameState.map1Enemy2Y;
            if (isGolem && !fantasyrpg.GameState.map1Enemy1Defeated) {
                return true;
            }
        } else {
            String enemyId = EnemySpawnConfig.normalizeEnemyId(pt.getEnemyId());
            if ("orc_warrior".equals(enemyId) && !fantasyrpg.GameState.map2Enemy1Defeated) {
                return true;
            }
            if ("dragonboss".equals(enemyId) && !fantasyrpg.GameState.map2Enemy2Defeated) {
                return true;
            }
        }
        return false;
    }

    private void drawLockedMonsterWarning(Graphics2D g2, EnemySpawnPoint point) {
        int monsterX = point.getX();
        int monsterY = point.getY();

        
        String warningText = "Defeat Slime first!";
        if (VARIANT_MAP_PATH.equals(currentMapPath)) {
            String enemyId = EnemySpawnConfig.normalizeEnemyId(point.getEnemyId());
            if ("orc_warrior".equals(enemyId)) {
                warningText = "Defeat Goblin first!";
            } else if ("dragonboss".equals(enemyId)) {
                warningText = "Defeat Orc Warrior first!";
            }
        }
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        int textWidth = g2.getFontMetrics().stringWidth(warningText);
        int boxWidth = textWidth + 16;
        int boxHeight = 24;
        int boxX = monsterX - boxWidth / 2;
        int boxY = monsterY - (isGolemPoint(point) || isDragonPoint(point) ? 76 : 60);

        
        g2.setColor(new Color(30, 10, 10, 220));
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);
        g2.setColor(new Color(255, 80, 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);
        g2.setStroke(new BasicStroke(1f));

        
        g2.setColor(new Color(255, 200, 200));
        g2.drawString(warningText, boxX + 8, boxY + 16);
    }

    
    private void checkMapTransition() {
        if (battleTriggered || mapTransitionCooldownFrames > 0) return;
        if (PRIMARY_MAP_PATH.equals(currentMapPath)) tryMoveToVariantMapFromRightEdge();
        else if (VARIANT_MAP_PATH.equals(currentMapPath)) tryMoveBackToPrimaryMapFromLeftEdge();
    }

    private void startMapTransition(String mapPath, int targetX, int targetY) {
        isTransitioning   = true;
        transitionHalfway = false;
        transitionAlpha   = 0f;
        nextMapPath        = mapPath;
        nextTargetX        = targetX;
        nextTargetY        = targetY;
    }

    private void tryMoveToVariantMapFromRightEdge() {
        if (!keyInput.rightPressed) return;
        int mapPxW       = mapLoader.mapWidth * mapLoader.tileWidth;
        int bandStart    = mapPxW - EDGE_TRANSITION_BAND_TILES * mapLoader.tileWidth;
        if (player.getSolidRight() < bandStart) return;
        if (!isEdgeOpenForCurrentRow(true)) return;
        if (!fantasyrpg.GameState.map1Enemy1Defeated || !fantasyrpg.GameState.map1Enemy2Defeated) {
            showHudMessage("Dungeon sealed! Defeat all enemies first.", new Color(255, 100, 100));
            return;
        }
        startMapTransition(VARIANT_MAP_PATH, mapLoader.tileWidth * SPAWN_MARGIN_TILES, 315);
    }

    private void tryMoveBackToPrimaryMapFromLeftEdge() {
        if (!keyInput.leftPressed) return;
        int bandEnd = EDGE_TRANSITION_BAND_TILES * mapLoader.tileWidth;
        if (player.getSolidLeft() > bandEnd) return;
        if (!isEdgeOpenForCurrentRow(false)) return;
        int mapPxW  = mapLoader.mapWidth * mapLoader.tileWidth;
        int targetX = mapPxW - ((SPAWN_MARGIN_TILES + 1) * mapLoader.tileWidth);
        startMapTransition(PRIMARY_MAP_PATH, targetX, 388);
    }

    private boolean isEdgeOpenForCurrentRow(boolean right) {
        int wallIdx = findLayerIndexByName("Wall");
        if (wallIdx < 0) return true;
        int col      = right ? mapLoader.mapWidth - 1 : 0;
        int topRow   = clampInt(player.getSolidTop()  / mapLoader.tileHeight, 0, mapLoader.mapHeight - 1);
        int botRow   = clampInt((player.getSolidBottom() - 1) / mapLoader.tileHeight, 0, mapLoader.mapHeight - 1);
        int[][] wl   = mapLoader.mapLayers.get(wallIdx);
        for (int row = topRow; row <= botRow; row++) if (wl[row][col] != 0) return false;
        return true;
    }

    private int findLayerIndexByName(String name) {
        for (int i = 0; i < mapLoader.mapLayerNames.size(); i++)
            if (mapLoader.mapLayerNames.get(i).equalsIgnoreCase(name)) return i;
        return -1;
    }

    private void loadMapWithPlayerPosition(String mapPath, int tx, int ty) {
        mapLoader      = new TiledMapLoader(mapPath);
        currentMapPath = mapPath;
        setupCollision();
        int maxX = mapLoader.mapWidth  * mapLoader.tileWidth  - player.getDrawWidth();
        int maxY = mapLoader.mapHeight * mapLoader.tileHeight - player.getDrawHeight();
        player.setPosition(clampInt(tx, 0, Math.max(0, maxX)), clampInt(ty, 0, Math.max(0, maxY)));
        setupMonsterPlaceholders();
        framesSinceWorldStart   = 0;
        playerMovedSinceSpawn   = false;
        playerNearMonster       = false;
        mapTransitionCooldownFrames = MAP_TRANSITION_COOLDOWN_FRAMES;

        String mapLabel = PRIMARY_MAP_PATH.equals(mapPath) ? "Map 1 – Village Outskirts" : "Map 2 – The Dungeon";
        showHudMessage("Entering: " + mapLabel, new Color(180, 200, 255));

        if (mapPath != null && mapPath.contains("dungeon")) {
            fantasyrpg.sound.SoundManager.playBGM("DUNGEON");
        } else {
            fantasyrpg.sound.SoundManager.playBGM("WORLD");
        }
    }

    
    private void enterBattleScene() {
        battleTriggered = true;
        gameThread      = null;
        fantasyrpg.sound.SoundManager.stopBGM();
        SwingUtilities.invokeLater(() -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
            new fantasyrpg.ui.battle.GameFrame();
        });
    }

    
    private boolean isWorldReadyForBattleTrigger() {
        return mapLoader.mapWidth > 0 && mapLoader.mapHeight > 0
                && mapLoader.tileWidth > 0 && mapLoader.tileHeight > 0;
    }

    private boolean isBattleTriggerArmed() {
        return framesSinceWorldStart >= WORLD_ENTRY_GRACE_FRAMES && playerMovedSinceSpawn;
    }

    private void showHudMessage(String msg, Color color) {
        hudMessage      = msg;
        hudMessageColor = color;
        hudMessageFrames = 180; 
    }
}