package fantasyrpg.ui;

import fantasyrpg.entities.DragonBoss;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Player;
import fantasyrpg.services.BattleService;
import fantasyrpg.services.RandomEventService;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class BattleGUI extends JPanel {
    private BufferedImage bg, playerImg, bossImg, actionPanelImg, gameOverImg, victoryImg;
    private BufferedImage arinWinImg, arinLoseImg; // Gambar tambahan untuk Arin

    private final Player player;
    private final Enemy currentEnemy;
    private final BattleService battleService;

    private final Deque<String> battleLogs = new ArrayDeque<>();

    private int round = 1;
    private int playerOffsetX = 0;
    private int bossOffsetX = 0;
    private int fireballX = -100;

    private boolean showFireball = false;
    private boolean canAction = true;
    private boolean isGameOver = false;
    private boolean showVictoryScreen = false;

    private float bossOpacity = 1.0f;
    private Color playerOverlay = new Color(0, 0, 0, 0);

    public BattleGUI(Player player, Enemy enemy) {
        this.player = player;
        this.currentEnemy = enemy;
        this.battleService = new BattleService(new RandomEventService(new Random()));

        setFocusable(true);
        requestFocusInWindow();
        loadAssets();
        beginRound();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                if (isGameOver || showVictoryScreen) {
                    if (mx >= 415 && mx <= 585 && my >= 420 && my <= 485) {
                        resetGame();
                    }
                    return;
                }

                if (canAction) {
                    handleActionClick(mx, my);
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((isGameOver || showVictoryScreen) && e.getKeyCode() == KeyEvent.VK_R) {
                    resetGame();
                    return;
                }

                if (!canAction || isGameOver || showVictoryScreen) {
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_1 -> executeAction(0);
                    case KeyEvent.VK_2 -> executeAction(1);
                    case KeyEvent.VK_3 -> executeAction(2);
                    case KeyEvent.VK_4 -> executeAction(3);
                    default -> {
                    }
                }
            }
        });
    }

    private void loadAssets() {
        bg = loadImage("assets/background.png");
        bossImg = loadImage("assets/boss.png");
        actionPanelImg = loadImage("assets/actionpanel.png");
        gameOverImg = loadImage("assets/game_over.png");
        victoryImg = loadImage("assets/victory.png");

        // Temporary debugging setup: all available characters use DragonBoss sprite.
        playerImg = loadImage("assets/player.png");
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Gagal memuat asset: " + e.getMessage());
            return null;
        }
    }

    private void resetGame() {
        player.setHp(player.getMaxHp());
        currentEnemy.setHp(currentEnemy.getMaxHp());

        round = 1;
        playerOffsetX = 0;
        bossOffsetX = 0;
        fireballX = -100;
        showFireball = false;
        bossOpacity = 1.0f;
        playerOverlay = new Color(0, 0, 0, 0);
        canAction = true;
        isGameOver = false;
        showVictoryScreen = false;

        battleLogs.clear();
        beginRound();
        repaint();
        requestFocusInWindow();
    }

    private void beginRound() {
        String eventMessage = battleService.beginRound(player, currentEnemy);
        addLog("Ronde " + round + ": " + eventMessage);
        canAction = true;
    }

    private void executeAction(int section) {
        BattleService.PlayerAction action = mapAction(section);
        if (action == null) {
            return;
        }

        canAction = false;
        switch (action) {
            case ATTACK -> playPlayerAttack(() -> resolvePlayerAction(action));
            case SKILL -> playFireballAnimation(() -> resolvePlayerAction(action));
            case DEFEND -> playDefendEffect(() -> resolvePlayerAction(action));
            case POTION -> playPotionEffect(() -> resolvePlayerAction(action));
        }
    }

    private BattleService.PlayerAction mapAction(int section) {
        return switch (section) {
            case 0 -> BattleService.PlayerAction.ATTACK;
            case 1 -> BattleService.PlayerAction.SKILL;
            case 2 -> BattleService.PlayerAction.DEFEND;
            case 3 -> BattleService.PlayerAction.POTION;
            default -> null;
        };
    }

    private void handleActionClick(int mx, int my) {
        if (mx >= 350 && mx <= 700 && my >= 480 && my <= 580) {
            int section = (mx - 350) / (350 / 4);
            executeAction(section);
        }
    }

    private void resolvePlayerAction(BattleService.PlayerAction action) {
        BattleService.ActionResult actionResult = battleService.executePlayerAction(player, currentEnemy, action);
        addLog(actionResult.getMessage());

        if (!currentEnemy.isAlive()) {
            BattleService.ActionResult rewardResult = battleService.applyVictoryRewards(player, currentEnemy);
            addLog(rewardResult.getMessage());
            playDeathAnimation();
            return;
        }

        scheduleEnemyTurn();
    }

    private void scheduleEnemyTurn() {
        Timer delay = new Timer(700, e -> {
            ((Timer) e.getSource()).stop();
            playBossAttack(this::resolveEnemyTurn);
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void resolveEnemyTurn() {
        BattleService.ActionResult enemyResult = battleService.executeEnemyTurn(player, currentEnemy);
        addLog(enemyResult.getMessage());

        if (!player.isAlive()) {
            isGameOver = true;
            canAction = false;
            repaint();
            return;
        }

        round++;
        beginRound();
        repaint();
    }

    private void playPotionEffect(Runnable onComplete) {
        playerOverlay = new Color(0, 255, 0, 120);
        repaint();

        Timer t = new Timer(450, e -> {
            playerOverlay = new Color(0, 0, 0, 0);
            ((Timer) e.getSource()).stop();
            onComplete.run();
            repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    private void playDefendEffect(Runnable onComplete) {
        playerOverlay = new Color(255, 255, 0, 150);
        repaint();

        Timer t = new Timer(450, e -> {
            playerOverlay = new Color(0, 0, 0, 0);
            ((Timer) e.getSource()).stop();
            onComplete.run();
            repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    private void playFireballAnimation(Runnable onComplete) {
        showFireball = true;
        fireballX = 250;

        Timer t = new Timer(15, null);
        t.addActionListener(e -> {
            if (fireballX < 600) {
                fireballX += 8;
            } else {
                showFireball = false;
                t.stop();
                onComplete.run();
            }
            repaint();
        });
        t.start();
    }

    private void playPlayerAttack(Runnable onComplete) {
        Timer t = new Timer(10, null);
        t.addActionListener(e -> {
            if (playerOffsetX < 100) {
                playerOffsetX += 15;
            } else {
                playerOffsetX = 0;
                t.stop();
                onComplete.run();
            }
            repaint();
        });
        t.start();
    }

    private void playBossAttack(Runnable onComplete) {
        Timer t = new Timer(20, null);
        t.addActionListener(e -> {
            if (bossOffsetX > -100) {
                bossOffsetX -= 15;
            } else {
                bossOffsetX = 0;
                t.stop();
                onComplete.run();
            }
            repaint();
        });
        t.start();
    }

    private void playDeathAnimation() {
        Timer t = new Timer(50, null);
        t.addActionListener(e -> {
            bossOpacity -= 0.05f;
            if (bossOpacity <= 0) {
                bossOpacity = 0;
                t.stop();
                showVictoryScreen = true;
                canAction = false;
            }
            repaint();
        });
        t.start();
    }

    private void addLog(String message) {
        battleLogs.addFirst(message);
        while (battleLogs.size() > 4) {
            battleLogs.removeLast();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (bg != null) {
            g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        }

        // --- LOGIKA PERUBAHAN GAMBAR ARIN ---
        BufferedImage arinToDraw = playerImg; // Default Arin
        if (showVictoryScreen) arinToDraw = arinWinImg;
        if (isGameOver) arinToDraw = arinLoseImg;

        if (arinToDraw != null) {
            g2d.drawImage(arinToDraw, 150 + playerOffsetX, 250, 200, 250, null);
            if (playerOverlay.getAlpha() > 0) {
                g2d.setColor(playerOverlay);
                g2d.fillOval(150, 250, 180, 220);
            }
        }

        if (showFireball) {
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(fireballX, 300, 40, 40);
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(fireballX + 5, 305, 30, 30);
        }

        if (bossImg != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bossOpacity));
            g2d.drawImage(bossImg, 550 + bossOffsetX, 120, 350, 350, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        drawActionPanel(g2d);
        drawStatusBars(g2d);
        drawBattleLog(g2d);

        if (isGameOver || showVictoryScreen) {
            drawEndOverlay(g2d);
        }
    }

    private void drawActionPanel(Graphics2D g2d) {
        if (actionPanelImg != null) {
            if (!canAction) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            }
            g2d.drawImage(actionPanelImg, 350, 480, 350, 100, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2d.setColor(new Color(25, 25, 25, 200));
            g2d.fillRoundRect(350, 480, 350, 100, 14, 14);
        }

        g2d.setColor(new Color(245, 245, 245));
        g2d.setFont(new Font("Serif", Font.BOLD, 15));

        g2d.setFont(new Font("Serif", Font.PLAIN, 16));
        g2d.drawString("Potion: " + player.getPotionCount(), 370, 590);
        g2d.drawString("Round: " + round, 500, 590);
        g2d.drawString(canAction ? "Giliranmu" : "Menunggu...", 590, 590);
    }

    private void drawBattleLog(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(40, 480, 290, 110, 12, 12);

        g2d.setFont(new Font("Serif", Font.PLAIN, 13));
        g2d.setColor(new Color(236, 236, 236));

        int y = 500;
        for (String line : battleLogs) {
            g2d.drawString(line, 50, y);
            y += 23;
        }
    }

    private void drawEndOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        BufferedImage currentImg = isGameOver ? gameOverImg : victoryImg;
        if (currentImg != null) {
            int drawW = 600;
            int drawH = 350;
            int x = (getWidth() - drawW) / 2;
            int y = (getHeight() - drawH) / 2;
            g2d.drawImage(currentImg, x, y, drawW, drawH, null);
        } else {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Serif", Font.BOLD, 36));
            g2d.drawString(isGameOver ? "GAME OVER" : "VICTORY", 390, 270);
        }

        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(415, 420, 170, 65, 12, 12);
        g2d.setColor(new Color(40, 40, 40));
        g2d.setFont(new Font("Serif", Font.BOLD, 22));
        g2d.drawString("RESTART", 450, 462);
        g2d.setFont(new Font("Serif", Font.PLAIN, 13));
        g2d.drawString("Klik tombol atau tekan R", 427, 480);
    }

    private void drawStatusBars(Graphics2D g2d) {
        drawBar(g2d, 50, 30, player.getName(), player.getHp(), player.getMaxHp(), Color.GREEN,
                "LV " + player.getLevel() + " | EXP " + player.getExperience() + " | Score " + player.getScore());
        drawBar(g2d, 680, 30, currentEnemy.getName(), currentEnemy.getHp(), currentEnemy.getMaxHp(), Color.ORANGE, "Enemy");
    }

    private void drawBar(Graphics2D g2d, int x, int y, String name, int hp, int maxHp, Color barColor, String subtitle) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(x, y, 260, 78);

        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font("Serif", Font.BOLD, 14));
        g2d.drawString(name, x + 10, y + 20);

        g2d.setFont(new Font("Serif", Font.PLAIN, 12));
        g2d.drawString(subtitle, x + 10, y + 35);

        g2d.setColor(new Color(60, 0, 0));
        g2d.fillRect(x + 10, y + 48, 210, 12);

        g2d.setColor(barColor);
        int safeMaxHp = Math.max(1, maxHp);
        int barWidth = (int) (210 * ((double) Math.max(0, hp) / safeMaxHp));
        g2d.fillRect(x + 10, y + 48, barWidth, 12);

        g2d.setColor(new Color(245, 245, 245));
        g2d.drawString(hp + "/" + maxHp, x + 225, y + 59);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Dungeon Battle: Rise of Hero");
        BattleGUI gui = new BattleGUI(new Player("Arin"), new DragonBoss());
        frame.add(gui);
        frame.setSize(1000, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
