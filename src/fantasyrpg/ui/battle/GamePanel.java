package fantasyrpg.ui.battle;

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
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import fantasyrpg.services.BattleService;

public class GamePanel extends JPanel {
    private static final int W = 1366;
    private static final int H = 768;
    private static final Color GOLD        = new Color(198, 136, 62);
    private static final Color TEXT        = new Color(255, 245, 220);
    private static final Color PANEL       = new Color(9, 16, 23);
    private static final Color RED_HP      = new Color(220, 50, 50);
    private static final Color GREEN_HP    = new Color(50, 200, 80);
    private static final Color BLUE_MP     = new Color(60, 140, 255);
    private static final Color WARN_YELLOW = new Color(255, 220, 40);

    private final ActionPanel actionPanel = new ActionPanel();
    private final BattleService battleService = new BattleService();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private BufferedImage bg;
    private BufferedImage playerImg;
    private BufferedImage slimeImg;
    private BufferedImage golemImg;
    private BufferedImage goblinImg;
    private BufferedImage orcImg;
    private BufferedImage dragonImg;
    private BufferedImage bg2;
    private BufferedImage endScreenImg;

    private String activeEffect = "";
    private int effectFrame;
    private Timer effectTimer;

    private String[] logLines = new String[4];
    private int logCount = 0;
    private int warningPulse = 0;
    private int shakeFrames = 0;
    private int shakeMagnitude = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setLayout(null);
        setBackground(new Color(14, 20, 31));
        loadImages();
        add(actionPanel);
        actionPanel.setActionListener(this::handleAction);
        setupKeyBindings();
        initBattle();

        setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (battleService.isBattleEnded() && "DRAGON".equals(battleService.getEnemyTypeKey())) {
                    System.exit(0);
                }
            }
        });
    }

    private void initBattle() {
        actionPanel.setVisible(true);
        battleService.initBattle();
        logLines = new String[]{"Pertempuran dimulai!", "Gunakan J, K, L atau klik tombol.", "", ""};
        logCount = 2;
        fantasyrpg.sound.SoundManager.playBGM("BATTLE");
    }

    @Override
    public void doLayout() {
        int[] area = scaledArea();
        double scale = area[2] / (double) W;
        actionPanel.setBounds(
                area[0] + (int) Math.round(405 * scale),
                area[1] + (int) Math.round(586 * scale),
                (int) Math.round(556 * scale),
                (int) Math.round(136 * scale));
    }

    private void setupKeyBindings() {
        bindActionKey(KeyEvent.VK_J, "ATTACK");
        bindActionKey(KeyEvent.VK_K, "SKILL");
        bindActionKey(KeyEvent.VK_L, "DEFEND");
        bindActionKey(KeyEvent.VK_B, "BACK");
    }

    private void bindActionKey(int keyCode, String action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0), action);
        getActionMap().put(action, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                handleAction(action);
            }
        });
    }

    private void handleAction(String action) {
        if (battleService.isBattleEnded() && "DRAGON".equals(battleService.getEnemyTypeKey())) {
            System.exit(0);
        }

        if ("BACK".equals(action)) {
            if (battleService.isBattleEnded()) {
                fantasyrpg.sound.SoundManager.stopBGM();
                battleService.executePlayerAction(BattleService.PlayerAction.BACK);
                returnToWorld();
            }
            return;
        }

        if (battleService.isBattleEnded() || !actionPanel.isEnabled()) return;

        BattleService.PlayerAction playerAction = switch (action) {
            case "ATTACK" -> BattleService.PlayerAction.ATTACK;
            case "SKILL"  -> BattleService.PlayerAction.SKILL;
            case "DEFEND" -> BattleService.PlayerAction.DEFEND;
            default -> null;
        };
        if (playerAction == null) return;

        BattleService.BattleResult result = battleService.executePlayerAction(playerAction);

        if (result.logMessage() != null && !result.logMessage().isEmpty()) {
            addLog(result.logMessage());
        }

        for (BattleService.EffectEvent effect : result.effects()) {
            queueEffect(effect.type());
        }

        if (result.damageDealt() > 0) {
            fantasyrpg.sound.SoundManager.playSFX(playerAction == BattleService.PlayerAction.SKILL ? "SKILL" : "ATTACK");
            triggerShake(6, 3);
            fantasyrpg.sound.SoundManager.playSFX("HIT");

            int floatY = getEnemyFloatY();
            int floatX = isLargeEnemy() ? 1040 : 1080;
            Color floatColor = playerAction == BattleService.PlayerAction.SKILL
                    ? new Color(200, 100, 255) : new Color(255, 220, 80);
            String suffix = playerAction == BattleService.PlayerAction.SKILL ? "!" : "";
            spawnFloating(floatX, floatY, "-" + result.damageDealt() + suffix, floatColor);
        } else if (playerAction == BattleService.PlayerAction.DEFEND) {
            fantasyrpg.sound.SoundManager.playSFX("DEFEND");
            spawnFloating(320, 460, "DEFEND!", new Color(80, 200, 255));
        } else if (result.logMessage().contains("cooldown") || result.logMessage().contains("habis")) {
            spawnFloating(320, 460, result.logMessage().contains("habis") ? "NO USES!" : "COOLDOWN!",
                    new Color(255, 150, 50));
        }

        if (result.battleEnded() && result.playerWon()) {
            handleVictory();
            return;
        }

        if (!result.battleEnded() && battleService.getPhase() == BattleService.BattlePhase.ENEMY_TURN) {
            executeEnemyTurn();
        }

        repaint();
    }

    private void executeEnemyTurn() {
        BattleService.BattleResult enemyResult = battleService.executeEnemyTurn();

        if (enemyResult.logMessage() != null && !enemyResult.logMessage().isEmpty()) {
            addLog(enemyResult.logMessage());
        }

        for (BattleService.EffectEvent effect : enemyResult.effects()) {
            queueEffect(effect.type());
        }

        if (enemyResult.damageDealt() > 0) {
            fantasyrpg.sound.SoundManager.playSFX("HIT");
            spawnFloating(290, 450, "-" + enemyResult.damageDealt(), new Color(255, 100, 100));
        }

        if (battleService.isEnemyEnraged()) {
            spawnFloating(1040, 300, "ENRAGED!", new Color(255, 50, 50));
            addLog(battleService.getEnemy().getName() + " ENRAGED!");
        }

        if (enemyResult.battleEnded() && !enemyResult.playerWon()) {
            fantasyrpg.sound.SoundManager.stopBGM();
            fantasyrpg.sound.SoundManager.playSFX("DEFEAT");
            triggerShake(12, 6);
        }
    }

    private void handleVictory() {
        fantasyrpg.sound.SoundManager.stopBGM();
        fantasyrpg.sound.SoundManager.playSFX("VICTORY");

        if ("DRAGON".equals(battleService.getEnemyTypeKey())) {
            fantasyrpg.sound.SoundManager.playBGM("ENDING");
            actionPanel.setVisible(false);
            actionPanel.setEnabled(false);
            actionPanel.setActionsEnabled(false);
        }
    }

    private void returnToWorld() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
            new fantasyrpg.ui.world.WorldFrame();
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        g.setColor(new Color(14, 20, 31));
        g.fillRect(0, 0, getWidth(), getHeight());

        int[] area = scaledArea();

        int shakeX = 0, shakeY = 0;
        if (shakeFrames > 0) {
            shakeX = (int) (Math.random() * (shakeMagnitude * 2 + 1)) - shakeMagnitude;
            shakeY = (int) (Math.random() * (shakeMagnitude * 2 + 1)) - shakeMagnitude;
            shakeFrames--;
        }

        g.translate(area[0] + shakeX, area[1] + shakeY);
        g.scale(area[2] / (double) W, area[3] / (double) H);

        drawBackground(g);
        drawTopPanels(g);
        drawCharacters(g);
        drawHeroPanel(g);
        drawLogPanel(g);
        drawCooldownPanel(g);
        drawFloatingTexts(g);

        if (battleService.isBattleEnded()) drawEndOverlay(g);

        g.dispose();
    }

    private void loadImages() {
        bg        = readImage("bg.png");
        bg2       = readImage("bg_2.png");
        playerImg = readImage("player.png");
        slimeImg  = readImage("slime.png");
        golemImg  = readImage("golem.png");
        goblinImg = readImage("goblin.png");
        orcImg    = readImage("orcwarrior.png");
        dragonImg = readImage("dragonboss.png");
        try {
            endScreenImg = ImageIO.read(new java.io.File("assets/end/end_screen.png"));
        } catch (IOException | IllegalArgumentException ex) {
            endScreenImg = null;
        }
    }

    private BufferedImage readImage(String name) {
        try {
            return ImageIO.read(new java.io.File("assets/battle/" + name));
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }

    private int[] scaledArea() {
        double scale = Math.min(getWidth() / (double) W, getHeight() / (double) H);
        int w = (int) Math.round(W * scale);
        int h = (int) Math.round(H * scale);
        return new int[]{(getWidth() - w) / 2, (getHeight() - h) / 2, w, h};
    }

    private void drawBackground(Graphics2D g) {
        BufferedImage bgImg = battleService.isDungeon() && bg2 != null ? bg2 : bg;
        if (bgImg != null) {
            g.drawImage(bgImg, 0, 0, W, H, null);
        } else {
            GradientPaint grad = new GradientPaint(0, 0, new Color(12, 18, 30), 0, H, new Color(30, 10, 10));
            g.setPaint(grad);
            g.fillRect(0, 0, W, H);
        }

        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Float(W / 2f, H / 2f),
                Math.max(W, H) * 0.7f,
                new float[]{0.4f, 1.0f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 160)}
        );
        g.setPaint(vignette);
        g.fillRect(0, 0, W, H);
    }

    private void drawTopPanels(Graphics2D g) {
        drawFrame(g, 24, 25, 142, 58);
        drawText(g, "TURN " + battleService.getTurn(), 49, 63, 22, TEXT, Font.BOLD);

        drawFrame(g, 455, 14, 456, 68);
        String name = battleService.getEnemy().getName().toUpperCase();
        Color nameColor = battleService.isEnemyEnraged() ? new Color(255, 80, 80) : TEXT;
        int nameSize = name.length() > 14 ? 24 : 36;
        if (battleService.isEnemyEnraged()) {
            drawCenteredText(g, "\u26A1 " + name + " [ENRAGED] \u26A1", 683, 59, Math.min(nameSize, 24), nameColor, Font.BOLD);
        } else {
            drawCenteredText(g, name, 683, 59, nameSize, nameColor, Font.BOLD);
        }

        drawFrame(g, 1018, 90, 300, 94);
        String label = battleService.getEnemyTypeKey();
        drawText(g, label, 1041, 127, 22, TEXT, Font.BOLD);

        String lvlText = battleService.isEnemyEnraged() ? "ENRAGED" : "LV. " + battleService.getEnemy().getLevel();
        Color lvlColor = battleService.isEnemyEnraged() ? new Color(255, 80, 80) : new Color(255, 194, 35);
        drawText(g, lvlText, 1185, 127, battleService.isEnemyEnraged() ? 16 : 20, lvlColor, Font.BOLD);

        drawText(g, "HP", 1041, 160, 20, TEXT, Font.BOLD);
        Color enemyHpColor = getHpColor((double) battleService.getEnemyHp() / battleService.getEnemyMaxHp());
        HealthBar.draw(g, 1083, 145, 130, 17, battleService.getEnemyHp(), battleService.getEnemyMaxHp(), enemyHpColor);
        drawText(g, battleService.getEnemyHp() + "/" + battleService.getEnemyMaxHp(), 1224, 162, 18, TEXT, Font.BOLD);
    }

    private void drawCharacters(Graphics2D g) {
        int playerY = 362;
        int shadowY = 584;
        int defendY = 342;
        int counterY = 354;
        int effectY = 474;
        String et = battleService.getEnemyTypeKey();

        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(185, shadowY, 190, 36);
        if ("GOLEM".equals(et)) g.fillOval(880, 587, 320, 48);
        else if ("DRAGON".equals(et)) g.fillOval(860, 595, 400, 48);
        else if ("ORC".equals(et)) g.fillOval(920, 585, 280, 42);
        else if ("GOBLIN".equals(et)) g.fillOval(960, 590, 240, 36);
        else g.fillOval(980, 591, 200, 36);

        int heroOffset  = (activeEffect.equals("HERO_ATTACK") || activeEffect.equals("HERO_SKILL"))
                ? Math.min(effectFrame, 6) * 10 : 0;
        int enemyOffset = activeEffect.equals("ENEMY_ATTACK") ? -Math.min(effectFrame, 6) * 9 : 0;

        drawImageOrBox(g, playerImg, 200 + heroOffset, playerY, 240, 256, new Color(32, 79, 116));

        BufferedImage eImg = switch (et) {
            case "GOLEM" -> golemImg; case "GOBLIN" -> goblinImg;
            case "ORC" -> orcImg; case "DRAGON" -> dragonImg;
            default -> slimeImg;
        };
        if ("GOLEM".equals(et)) drawImageOrBox(g, eImg, 895 + enemyOffset, 260, 360, 360, new Color(100, 100, 100));
        else if ("DRAGON".equals(et)) drawImageOrBox(g, eImg, 860 + enemyOffset, 290, 400, 340, new Color(80, 40, 120));
        else if ("ORC".equals(et)) drawImageOrBox(g, eImg, 920 + enemyOffset, 320, 280, 300, new Color(90, 130, 40));
        else if ("GOBLIN".equals(et)) drawImageOrBox(g, eImg, 960 + enemyOffset, 360, 240, 260, new Color(60, 160, 70));
        else drawImageOrBox(g, eImg, 960 + enemyOffset, 428, 240, 190, new Color(60, 200, 30));

        if (activeEffect.equals("HERO_ATTACK") || activeEffect.equals("HERO_SKILL")) {
            float alpha = Math.max(0f, 1f - effectFrame / 10f);
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            if (activeEffect.equals("HERO_SKILL")) {
                int skillCy = switch (et) {
                    case "GOLEM" -> 442; case "DRAGON" -> 460;
                    case "ORC" -> 470; case "GOBLIN" -> 490;
                    default -> 524;
                };
                boolean big = "GOLEM".equals(et) || "DRAGON".equals(et) || "ORC".equals(et);
                drawMagicBurst(g, big ? 1040 : 1075, skillCy, big ? 170 : 110);
                drawSlashEffect(g, 580, 360, 390, 200, new Color(92, 200, 255, 200));
            } else {
                drawSlashEffect(g, 465, 425, 510, 150, new Color(255, 245, 200, 200));
            }
            g.setComposite(old);
        }

        if (activeEffect.equals("ENEMY_HIT")) {
            float intensity = Math.max(0f, 1f - effectFrame / 8f);
            g.setColor(new Color(255, 80, 50, (int)(160 * intensity)));
            if ("GOLEM".equals(et)) g.fillRect(895, 260, 360, 360);
            else if ("DRAGON".equals(et)) g.fillRect(860, 290, 400, 340);
            else if ("ORC".equals(et)) g.fillRect(920, 320, 280, 300);
            else if ("GOBLIN".equals(et)) g.fillRect(960, 360, 240, 260);
            else g.fillRect(960, 428, 240, 190);
        }

        if (activeEffect.equals("ENEMY_ATTACK")) {
            drawSlashEffect(g, 760, 380, 400, 160, new Color(255, 80, 80, 190));
        }

        if (activeEffect.equals("ENEMY_SKILL")) {
            drawMagicBurst(g, 320, effectY, 140);
            drawSlashEffect(g, 720, 340, 440, 200, new Color(200, 50, 255, 180));
        }

        if (activeEffect.equals("HERO_HIT")) {
            float intensity = Math.max(0f, 1f - effectFrame / 8f);
            g.setColor(new Color(255, 50, 45, (int)(150 * intensity)));
            g.fillRect(200, playerY, 240, 256);
        }

        if (activeEffect.equals("HERO_DEFEND") || battleService.isHeroDefendingThisTurn()) {
            float alpha = activeEffect.equals("HERO_DEFEND")
                    ? Math.min(1f, effectFrame / 5f) : 0.35f;
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(new Color(60, 170, 255, 100));
            g.fillOval(170, defendY, 295, 300);
            g.setStroke(new BasicStroke(4));
            g.setColor(new Color(120, 200, 255, 200));
            g.drawOval(170, defendY, 295, 300);
            g.setStroke(new BasicStroke(1));
            g.setComposite(old);
        }

        if (battleService.isHeroDefendedLastTurn() && !battleService.isHeroDefendingThisTurn()) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            g.setColor(new Color(100, 220, 255));
            g.drawString("\u2694 COUNTER READY", 175, counterY);
        }

        int maxHp = battleService.getPlayer() != null ? battleService.getPlayer().getMaxHp() : 180;
        if (!battleService.isBattleEnded() && battleService.getHeroHp() > 0 && (double) battleService.getHeroHp() / maxHp < 0.25) {
            warningPulse = (warningPulse + 1) % 40;
            if (warningPulse < 20) {
                float alpha = (float) Math.sin(warningPulse * Math.PI / 20) * 0.4f;
                Composite old = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g.setColor(new Color(255, 30, 30));
                g.fillRect(0, 0, W, H);
                g.setComposite(old);
            }
        }
    }

    private void drawMagicBurst(Graphics2D g, int cx, int cy, int r) {
        float progress = (float) effectFrame / 12f;
        int curR = (int)(r * progress);
        float alpha = Math.max(0f, 0.9f - progress);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(180, 100, 255));
        g.setStroke(new BasicStroke(5));
        g.drawOval(cx - curR, cy - curR, curR * 2, curR * 2);
        g.setColor(new Color(255, 200, 255, 120));
        g.fillOval(cx - curR / 2, cy - curR / 2, curR, curR);
        g.setStroke(new BasicStroke(1));
        g.setComposite(old);
    }

    private void drawSlashEffect(Graphics2D g, int x, int y, int w, int h, Color color) {
        int shift = effectFrame * 14;
        g.setStroke(new BasicStroke(10));
        g.setColor(color);
        g.drawLine(x + shift, y + h, x + w + shift, y);
        g.setColor(new Color(255, 255, 255, 140));
        g.setStroke(new BasicStroke(4));
        g.drawLine(x + shift + 18, y + h - 10, x + w + shift - 18, y + 10);
        g.setStroke(new BasicStroke(1));
    }

    private void drawImageOrBox(Graphics2D g, Image img, int x, int y, int w, int h, Color fallback) {
        if (img != null) { g.drawImage(img, x, y, w, h, null); return; }
        g.setColor(fallback);
        g.fillOval(x, y, w, h);
    }

    private void drawHeroPanel(Graphics2D g) {
        drawFrame(g, 45, 586, 340, 136);
        drawText(g, "HERO", 72, 625, 24, TEXT, Font.BOLD);
        int lv = battleService.getPlayer() != null ? battleService.getPlayer().getLevel() : 5;
        int maxHpVal = battleService.getPlayer() != null ? battleService.getPlayer().getMaxHp() : 180;
        int chargesLeft = battleService.getPlayer() != null ? battleService.getPlayer().getFireballCharges() : 0;
        int maxCharges = battleService.getPlayer() != null ? battleService.getPlayer().getMaxFireballCharges() : 1;

        drawText(g, "LV." + lv, 258, 625, 22, WARN_YELLOW, Font.BOLD);

        drawText(g, "HP", 72, 658, 20, new Color(100, 255, 130), Font.BOLD);
        Color hpColor = getHpColor((double) battleService.getHeroHp() / maxHpVal);
        HealthBar.draw(g, 112, 642, 160, 16, battleService.getHeroHp(), maxHpVal, hpColor);
        drawText(g, battleService.getHeroHp() + "/" + maxHpVal, 282, 658, 18, TEXT, Font.BOLD);

        drawText(g, "SKILL", 72, 688, 20, BLUE_MP, Font.BOLD);
        HealthBar.draw(g, 140, 672, 132, 16, chargesLeft, maxCharges, BLUE_MP);
        drawText(g, chargesLeft + "/" + maxCharges, 282, 688, 18, TEXT, Font.BOLD);

        if (battleService.isHeroDefendingThisTurn()) {
            drawText(g, "[DEFENDING]", 72, 712, 15, new Color(80, 200, 255), Font.BOLD);
        } else if (battleService.isHeroDefendedLastTurn()) {
            drawText(g, "[COUNTER READY]", 72, 712, 14, new Color(100, 230, 255), Font.BOLD);
        }
    }

    private void drawCooldownPanel(Graphics2D g) {
        int baseX = 405;
        int baseY = 728;

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        drawCooldownBadge(g, baseX,       baseY, "ATTACK", battleService.getAttackCooldownLeft());
        drawCooldownBadge(g, baseX + 192, baseY, "SKILL",  battleService.getSkillCooldownLeft());
        drawCooldownBadge(g, baseX + 384, baseY, "DEFEND", battleService.getDefendCooldownLeft());
    }

    private void drawCooldownBadge(Graphics2D g, int x, int y, String label, int cd) {
        Color bg  = cd > 0 ? new Color(60, 10, 10) : new Color(10, 40, 10);
        Color txt = cd > 0 ? new Color(255, 100, 100) : new Color(80, 255, 120);
        g.setColor(bg);
        g.fillRoundRect(x, y, 170, 24, 6, 6);
        g.setColor(cd > 0 ? new Color(180, 50, 50) : new Color(40, 160, 40));
        g.drawRoundRect(x, y, 170, 24, 6, 6);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        String status = cd > 0 ? label + "  CD: " + cd : label + "  READY";
        g.setColor(new Color(0, 0, 0, 100));
        g.drawString(status, x + 9, y + 17);
        g.setColor(txt);
        g.drawString(status, x + 8, y + 16);
    }

    private void drawLogPanel(Graphics2D g) {
        drawFrame(g, 970, 586, 360, 136);
        drawCenteredText(g, "BATTLE LOG", 1150, 620, 19, new Color(230, 183, 115), Font.BOLD);
        g.setColor(new Color(42, 52, 58));
        g.drawLine(995, 632, 1305, 632);

        int y = 656;
        int lineH = 22;
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        for (int i = 0; i < Math.min(4, logCount); i++) {
            String line = logLines[i];
            if (line == null || line.isEmpty()) continue;
            float alpha = i == 0 ? 1.0f : i == 1 ? 0.80f : i == 2 ? 0.55f : 0.35f;
            Color col = i == 0
                    ? (line.contains("kalah") || line.contains("HP") && line.contains("-") ? new Color(255, 130, 130) : TEXT)
                    : TEXT;
            g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)(255 * alpha)));
            g.drawString(line, 995, y);
            y += lineH;
        }
    }

    private void drawFloatingTexts(Graphics2D g) {
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.update();
            ft.draw(g);
            if (ft.isDead()) floatingTexts.remove(i);
        }
    }

    private void drawEndOverlay(Graphics2D g) {
        boolean won = battleService.isPlayerWon();
        if (won && "DRAGON".equals(battleService.getEnemyTypeKey())) {
            if (endScreenImg != null) {
                g.drawImage(endScreenImg, 0, 0, W, H, null);
            } else {
                g.setColor(new Color(0, 0, 0, 220));
                g.fillRect(0, 0, W, H);
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));
                g.setColor(new Color(255, 215, 0));
                String thanks = "THANKS FOR PLAYING!";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(thanks, (W - fm.stringWidth(thanks)) / 2, H / 2);
            }
            return;
        }

        Color overlay = won ? new Color(0, 180, 80, 60) : new Color(180, 0, 0, 80);
        g.setColor(overlay);
        g.fillRect(0, 0, W, H);

        String headline = won ? "VICTORY!" : "DEFEATED";
        Color headlineColor = won ? new Color(100, 255, 130) : new Color(255, 80, 80);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 72));
        FontMetrics fm = g.getFontMetrics();
        int hx = (W - fm.stringWidth(headline)) / 2;

        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(headline, hx + 4, H / 2 - 20 + 4);
        g.setColor(headlineColor);
        g.drawString(headline, hx, H / 2 - 20);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        String sub = won ? "Enemy defeated!" : "You have fallen...";
        fm = g.getFontMetrics();
        g.setColor(new Color(255, 255, 255, 200));
        g.drawString(sub, (W - fm.stringWidth(sub)) / 2, H / 2 + 40);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        String hint = "Press B to return";
        fm = g.getFontMetrics();
        g.setColor(new Color(255, 255, 255, 255));
        g.drawString(hint, (W - fm.stringWidth(hint)) / 2, H / 2 + 80);
    }

    private Color getHpColor(double ratio) {
        if (ratio > 0.5) return GREEN_HP;
        if (ratio > 0.25) return new Color(255, 180, 30);
        return RED_HP;
    }

    private void addLog(String line) {
        for (int i = logLines.length - 1; i > 0; i--) {
            logLines[i] = logLines[i - 1];
        }
        logLines[0] = line;
        logCount = Math.min(logCount + 1, logLines.length);
    }

    private void spawnFloating(int x, int y, String text, Color color) {
        floatingTexts.add(new FloatingText(x, y, text, color));
    }

    private void queueEffect(String effect) {
        if (effectTimer == null || !effectTimer.isRunning()) {
            startEffect(effect);
        }
    }

    private void startEffect(String effect) {
        activeEffect = effect;
        effectFrame = 0;
        actionPanel.setActionsEnabled(false);
        actionPanel.setEnabled(false);

        effectTimer = new Timer(32, ev -> {
            effectFrame++;
            repaint();
            if (effectFrame >= 14) {
                effectTimer.stop();
                activeEffect = "";
                actionPanel.setActionsEnabled(!battleService.isBattleEnded());
                actionPanel.setEnabled(!battleService.isBattleEnded());
                repaint();
            }
        });
        effectTimer.start();
    }

    private void triggerShake(int frames, int magnitude) {
        shakeFrames = frames;
        shakeMagnitude = magnitude;
    }

    private boolean isLargeEnemy() {
        String et = battleService.getEnemyTypeKey();
        return "GOLEM".equals(et) || "DRAGON".equals(et);
    }

    private int getEnemyFloatY() {
        return switch (battleService.getEnemyTypeKey()) {
            case "GOLEM" -> 420; case "DRAGON" -> 440;
            case "ORC" -> 460; case "GOBLIN" -> 480;
            default -> 480;
        };
    }

    public static void drawFrame(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(new Color(2, 4, 7));
        g.fillRect(x, y, width, height);
        g.setColor(GOLD.darker());
        g.drawRect(x + 2, y + 2, width - 4, height - 4);
        g.setColor(GOLD);
        g.drawRect(x + 6, y + 6, width - 12, height - 12);
        g.setColor(PANEL);
        g.fillRect(x + 11, y + 11, width - 22, height - 22);
        g.setColor(new Color(15, 28, 39));
        g.drawRect(x + 15, y + 15, width - 30, height - 30);
        drawCorner(g, x + 6, y + 6, 1, 1);
        drawCorner(g, x + width - 7, y + 6, -1, 1);
        drawCorner(g, x + 6, y + height - 7, 1, -1);
        drawCorner(g, x + width - 7, y + height - 7, -1, -1);
    }

    private static void drawCorner(Graphics2D g, int x, int y, int sx, int sy) {
        g.setColor(GOLD);
        g.drawLine(x, y, x + sx * 16, y);
        g.drawLine(x, y, x, y + sy * 16);
        g.drawLine(x + sx * 4, y + sy * 4, x + sx * 10, y + sy * 4);
        g.drawLine(x + sx * 4, y + sy * 4, x + sx * 4, y + sy * 10);
    }

    private void drawText(Graphics2D g, String text, int x, int y, int size, Color color, int style) {
        g.setFont(new Font(Font.MONOSPACED, style, size));
        g.setColor(new Color(0, 0, 0, 140));
        g.drawString(text, x + 2, y + 2);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private void drawCenteredText(Graphics2D g, String text, int cx, int by, int size, Color color, int style) {
        g.setFont(new Font(Font.MONOSPACED, style, size));
        FontMetrics fm = g.getFontMetrics();
        drawText(g, text, cx - fm.stringWidth(text) / 2, by, size, color, style);
    }

    private static class FloatingText {
        double x, y;
        final String text;
        final Color color;
        double vy = -1.8;
        double alpha = 1.0;

        FloatingText(int x, int y, String text, Color color) {
            this.x     = x;
            this.y     = y;
            this.text  = text;
            this.color = color;
        }

        void update() {
            y += vy;
            vy *= 0.94;
            alpha -= 0.028;
        }

        boolean isDead() { return alpha <= 0; }

        void draw(Graphics2D g) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 26));
            FontMetrics fm = g.getFontMetrics();
            int tx = (int) x - fm.stringWidth(text) / 2;
            int ty = (int) y;
            float a = (float) Math.max(0, alpha);
            g.setColor(new Color(0, 0, 0, (int)(a * 160)));
            g.drawString(text, tx + 2, ty + 2);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(a * 255)));
            g.drawString(text, tx, ty);
        }
    }
}
