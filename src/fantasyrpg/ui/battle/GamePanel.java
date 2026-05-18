package fantasyrpg.ui.battle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class GamePanel extends JPanel {
    private static final int W = 1366;
    private static final int H = 768;
    private static final Color GOLD = new Color(198, 136, 62);
    private static final Color TEXT = new Color(255, 245, 220);
    private static final Color PANEL = new Color(9, 16, 23);

    private final ActionPanel actionPanel = new ActionPanel();
    private final Random random = new Random();
    private final Queue<String> effectQueue = new ArrayDeque<>();
    private BufferedImage bg;
    private BufferedImage player;
    private BufferedImage slime;
    private Timer effectTimer;
    private String activeEffect = "";
    private int effectFrame;
    private int turn = 1;
    private int heroHp = 95;
    private int heroMp = 20;
    private int enemyHp = 100;
    private boolean battleEnded;
    private String[] logLines = {
            "Pertempuran dimulai!",
            "Gunakan J, K, atau L untuk memilih aksi."
    };

    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setLayout(null);
        setBackground(new Color(14, 20, 31));
        loadImages();
        add(actionPanel);
        actionPanel.setActionListener(this::handleAction);
        setupKeyBindings();
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
    }

    private void bindActionKey(int keyCode, String action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), action);
        getActionMap().put(action, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                handleAction(action);
            }
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
        g.translate(area[0], area[1]);
        g.scale(area[2] / (double) W, area[3] / (double) H);

        drawBackground(g);
        drawTopPanels(g);
        drawCharacters(g);
        drawHeroPanel(g);
        drawLogPanel(g);

        g.dispose();
    }

    private void loadImages() {
        bg = readImage("bg.png");
        player = readImage("player.png");
        slime = readImage("slime.png");
    }

    private BufferedImage readImage(String name) {
        try {
            return ImageIO.read(GamePanel.class.getResource("/fantasyrpg/ui/battle/" + name));
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }

    private int[] scaledArea() {
        double scale = Math.min(getWidth() / (double) W, getHeight() / (double) H);
        int width = (int) Math.round(W * scale);
        int height = (int) Math.round(H * scale);
        return new int[] {(getWidth() - width) / 2, (getHeight() - height) / 2, width, height};
    }

    private void drawBackground(Graphics2D g) {
        if (bg != null) {
            g.drawImage(bg, 0, 0, W, H, null);
            return;
        }

        g.setColor(new Color(191, 137, 82));
        g.fillRect(0, 0, W, H);
    }

    private void drawTopPanels(Graphics2D g) {
        drawFrame(g, 24, 25, 142, 58);
        drawText(g, "TURN " + turn, 49, 63, 24, TEXT, Font.BOLD);

        drawFrame(g, 455, 14, 456, 68);
        drawCenteredText(g, "BOSS SLIME", 683, 59, 38, TEXT, Font.BOLD);

        drawFrame(g, 1018, 90, 300, 94);
        drawText(g, "BOSS", 1041, 131, 24, TEXT, Font.BOLD);
        drawText(g, "LV. 3", 1188, 131, 24, new Color(255, 194, 35), Font.BOLD);
        drawText(g, "HP", 1041, 164, 22, TEXT, Font.BOLD);
        HealthBar.draw(g, 1083, 149, 128, 17, enemyHp, 100, new Color(236, 58, 46));
        drawText(g, enemyHp + " / 100", 1224, 166, 21, TEXT, Font.BOLD);
    }

    private void drawCharacters(Graphics2D g) {
        int heroOffset = activeEffect.equals("HERO_ATTACK") || activeEffect.equals("HERO_SKILL")
                ? Math.min(effectFrame, 6) * 9
                : 0;
        int slimeOffset = activeEffect.equals("SLIME_ATTACK") ? -Math.min(effectFrame, 6) * 8 : 0;

        g.setColor(new Color(45, 32, 24, 120));
        g.fillOval(204, 535, 180, 38);
        g.fillOval(992, 536, 190, 38);

        drawImageOrBox(g, player, 208 + heroOffset, 342, 230, 250, new Color(32, 79, 116));
        drawImageOrBox(g, slime, 970 + slimeOffset, 420, 220, 178, new Color(80, 215, 34));

        if (activeEffect.equals("HERO_SKILL")) {
            drawSlashEffect(g, 585, 365, 385, 200, new Color(92, 180, 255, 180));
        } else if (activeEffect.equals("HERO_ATTACK")) {
            drawSlashEffect(g, 470, 430, 500, 150, new Color(255, 245, 205, 190));
        }

        if (activeEffect.equals("SLIME_HIT")) {
            g.setColor(new Color(255, 66, 46, 90));
            g.fillOval(970, 420, 220, 178);
        }

        if (activeEffect.equals("SLIME_ATTACK")) {
            g.setColor(new Color(105, 255, 80, 95));
            g.fillOval(835, 470, 140, 58);
        }

        if (activeEffect.equals("HERO_HIT")) {
            g.setColor(new Color(255, 50, 45, 85));
            g.fillRect(208, 342, 230, 250);
        }

        if (activeEffect.equals("HERO_DEFEND")) {
            g.setColor(new Color(75, 170, 255, 90));
            g.fillOval(178, 328, 290, 285);
            g.setColor(new Color(170, 230, 255, 170));
            g.setStroke(new BasicStroke(5));
            g.drawOval(178, 328, 290, 285);
            g.setStroke(new BasicStroke(1));
        }
    }

    private void drawSlashEffect(Graphics2D g, int x, int y, int width, int height, Color color) {
        int shift = effectFrame * 16;
        g.setStroke(new BasicStroke(9));
        g.setColor(color);
        g.drawLine(x + shift, y + height, x + width + shift, y);
        g.setColor(new Color(255, 255, 255, 170));
        g.setStroke(new BasicStroke(4));
        g.drawLine(x + shift + 20, y + height - 8, x + width + shift - 20, y + 8);
        g.setStroke(new BasicStroke(1));
    }

    private void drawImageOrBox(Graphics2D g, Image image, int x, int y, int width, int height, Color fallback) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
            return;
        }

        g.setColor(fallback);
        g.fillOval(x, y, width, height);
    }

    private void drawHeroPanel(Graphics2D g) {
        drawFrame(g, 45, 586, 330, 136);
        drawText(g, "HERO", 73, 630, 27, TEXT, Font.BOLD);
        drawText(g, "LV. 5", 272, 630, 24, new Color(255, 194, 35), Font.BOLD);
        drawText(g, "HP", 73, 664, 22, new Color(101, 236, 255), Font.BOLD);
        HealthBar.draw(g, 118, 649, 142, 17, heroHp, 95, new Color(72, 218, 37));
        drawText(g, heroHp + " / 95", 273, 666, 21, TEXT, Font.BOLD);
        drawText(g, "MP", 73, 696, 22, new Color(37, 183, 255), Font.BOLD);
        HealthBar.draw(g, 118, 681, 142, 17, heroMp, 20, new Color(36, 195, 224));
        drawText(g, heroMp + " / 20", 273, 698, 21, TEXT, Font.BOLD);
    }

    private void drawLogPanel(Graphics2D g) {
        drawFrame(g, 970, 586, 350, 136);
        drawCenteredText(g, "LOG", 1145, 625, 22, new Color(230, 183, 115), Font.BOLD);
        g.setColor(new Color(42, 52, 58));
        g.drawLine(995, 638, 1295, 638);

        int y = 666;
        for (String line : logLines) {
            drawText(g, line, 995, y, 16, TEXT, Font.PLAIN);
            y += 24;
        }
    }

    private void handleAction(String action) {
        if (battleEnded || !actionPanel.isEnabled()) {
            return;
        }

        actionPanel.selectAction(action);

        if ("ATTACK".equals(action)) {
            int damage = 12 + random.nextInt(7);
            queueEffect("HERO_ATTACK");
            queueEffect("SLIME_HIT");
            damageEnemy(damage);
            addLog("Hero menyerang: -" + damage + " HP.");
            enemyTurnIfAlive(false);
        } else if ("SKILL".equals(action)) {
            if (heroMp < 5) {
                addLog("MP tidak cukup untuk skill.");
                repaint();
                return;
            }
            heroMp -= 5;
            int damage = 30 + random.nextInt(11);
            queueEffect("HERO_SKILL");
            queueEffect("SLIME_HIT");
            damageEnemy(damage);
            addLog("Power Slash: -" + damage + " HP.");
            enemyTurnIfAlive(false);
        } else if ("DEFEND".equals(action)) {
            int beforeMp = heroMp;
            heroMp = Math.min(20, heroMp + 3);
            queueEffect("HERO_DEFEND");
            addLog("Hero bertahan. MP +" + (heroMp - beforeMp) + ".");
            enemyTurnIfAlive(true);
        }

        repaint();
    }

    private void damageEnemy(int damage) {
        enemyHp = Math.max(0, enemyHp - damage);
        if (enemyHp == 0) {
            battleEnded = true;
            addLog("Slime kalah. Kamu menang!");
        }
    }

    private void enemyTurnIfAlive(boolean defending) {
        if (battleEnded) {
            return;
        }

        int damage = 7 + random.nextInt(6);
        if (defending) {
            damage = Math.max(1, damage / 4);
        }

        queueEffect("SLIME_ATTACK");
        queueEffect(defending ? "HERO_DEFEND" : "HERO_HIT");
        heroHp = Math.max(0, heroHp - damage);
        addLog(defending ? "Serangan ditahan: -" + damage + " HP." : "Slime menyerang: -" + damage + " HP.");
        turn++;

        if (heroHp == 0) {
            battleEnded = true;
            addLog("Hero kalah. Game Over.");
        }
    }

    private void addLog(String line) {
        logLines = new String[] {line, logLines[0], logLines.length > 1 ? logLines[1] : ""};
    }

    private void queueEffect(String effect) {
        effectQueue.add(effect);
        if (effectTimer == null || !effectTimer.isRunning()) {
            startNextEffect();
        }
    }

    private void startNextEffect() {
        activeEffect = effectQueue.poll();
        effectFrame = 0;
        actionPanel.setActionsEnabled(false);
        actionPanel.setEnabled(false);

        if (activeEffect == null) {
            activeEffect = "";
            actionPanel.setActionsEnabled(!battleEnded);
            actionPanel.setEnabled(!battleEnded);
            repaint();
            return;
        }

        effectTimer = new Timer(35, event -> {
            effectFrame++;
            if (effectFrame >= 12) {
                effectTimer.stop();
                startNextEffect();
            }
            repaint();
        });
        effectTimer.start();
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
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(text, x + 3, y + 3);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private void drawCenteredText(Graphics2D g, String text, int centerX, int baselineY, int size, Color color, int style) {
        g.setFont(new Font(Font.MONOSPACED, style, size));
        FontMetrics metrics = g.getFontMetrics();
        drawText(g, text, centerX - metrics.stringWidth(text) / 2, baselineY, size, color, style);
    }
}
