package fantasyrpg.ui;

import fantasyrpg.entities.Player;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.DragonBoss;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class BattleGUI extends JPanel {
    private BufferedImage bg, playerImg, bossImg, actionPanelImg, gameOverImg, victoryImg;
    private Player player;
    private Enemy currentEnemy;

    private int playerOffsetX = 0;
    private int bossOffsetX = 0;
    private int fireballX = -100;
    private boolean showFireball = false;
    private float bossOpacity = 1.0f;
    private Color playerOverlay = new Color(0,0,0,0);

    private boolean canAction = true;
    private boolean isGameOver = false;
    private boolean showVictoryScreen = false;
    private boolean isDefending = false;

    private int fireballCount = 4;
    private Timer autoAttackTimer;

    public BattleGUI(Player player, Enemy enemy) {
        this.player = player;
        this.currentEnemy = enemy;
        this.setFocusable(true);
        this.requestFocusInWindow();
        loadAssets();

        autoAttackTimer = new Timer(2500, e -> {
            if (!isGameOver && !showVictoryScreen && currentEnemy.isAlive()) {
                playBossAttack();
            }
        });
        autoAttackTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isGameOver || showVictoryScreen) {
                    if (e.getX() >= 415 && e.getX() <= 585 && e.getY() >= 420 && e.getY() <= 485) {
                        resetGame();
                    } else if (showVictoryScreen) {
                        System.exit(0);
                    }
                } else if (canAction) {
                    handleActionClick(e.getX(), e.getY());
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
                if (!canAction || isGameOver || showVictoryScreen) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_1 -> executeAction(0);
                    case KeyEvent.VK_2 -> executeAction(1);
                    case KeyEvent.VK_3 -> executeAction(2);
                    case KeyEvent.VK_4 -> executeAction(4);
                }
            }
        });
    }

    private void loadAssets() {
        try {
            bg = ImageIO.read(new File("assets/background.png"));
            playerImg = ImageIO.read(new File("assets/player.png"));
            bossImg = ImageIO.read(new File("assets/boss.png"));
            actionPanelImg = ImageIO.read(new File("assets/actionpanel.png"));
            gameOverImg = ImageIO.read(new File("assets/game_over.png"));
            victoryImg = ImageIO.read(new File("assets/victory.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetGame() {
        player.setHp(player.getMaxHp());
        currentEnemy.setHp(currentEnemy.getMaxHp());
        isGameOver = false;
        showVictoryScreen = false;
        canAction = true;
        isDefending = false;
        bossOpacity = 1.0f;
        fireballCount = 4;
        autoAttackTimer.restart();
        repaint();
    }

    private void executeAction(int section) {
        if (section == 1 && fireballCount <= 0) return;
        canAction = false;
        switch (section) {
            case 0 -> playPlayerAttack();
            case 1 -> playFireballAnimation();
            case 2 -> playDefendEffect();
            case 3, 4 -> playPotionEffect();
        }
        Timer cooldown = new Timer(1200, e -> {
            if (!isGameOver && !showVictoryScreen) canAction = true;
            ((Timer)e.getSource()).stop();
        });
        cooldown.start();
    }

    private void handleActionClick(int mx, int my) {
        if (mx >= 350 && mx <= 700 && my >= 480 && my <= 580) {
            int section = (mx - 350) / (350 / 4);
            executeAction(section);
        }
    }

    private void playPotionEffect() {
        if (player.usePotion()) {
            playerOverlay = new Color(0, 255, 0, 120);
            repaint();
            new Timer(600, e -> {
                playerOverlay = new Color(0, 0, 0, 0);
                ((Timer)e.getSource()).stop();
            }).start();
        }
    }

    private void playDefendEffect() {
        isDefending = true;
        playerOverlay = new Color(255, 255, 0, 150);
        repaint();
        Timer t = new Timer(1000, e -> {
            playerOverlay = new Color(0, 0, 0, 0);
            isDefending = false;
            ((Timer)e.getSource()).stop();
            repaint();
        });
        t.start();
    }

    private void playFireballAnimation() {
        fireballCount--;
        showFireball = true;
        fireballX = 250;
        Timer t = new Timer(15, null);
        t.addActionListener(e -> {
            if (fireballX < 600) {
                fireballX += 8;
            } else {
                showFireball = false;
                t.stop();
                currentEnemy.receiveDamage(player.getAttackPower() + 25);
                checkBattleStatus();
            }
            repaint();
        });
        t.start();
    }

    private void playPlayerAttack() {
        Timer t = new Timer(10, null);
        t.addActionListener(e -> {
            if (playerOffsetX < 100) {
                playerOffsetX += 15;
            } else {
                playerOffsetX = 0;
                t.stop();
                currentEnemy.receiveDamage(player.getAttackPower());
                checkBattleStatus();
            }
            repaint();
        });
        t.start();
    }

    private void playBossAttack() {
        Timer t = new Timer(20, null);
        t.addActionListener(e -> {
            if (bossOffsetX > -100) {
                bossOffsetX -= 15;
            } else {
                bossOffsetX = 0;
                t.stop();
                int damageDealt = isDefending ? 0 : 25;
                player.receiveDamage(damageDealt);
                if (!player.isAlive()) {
                    isGameOver = true;
                    autoAttackTimer.stop();
                }
            }
            repaint();
        });
        t.start();
    }

    private void checkBattleStatus() {
        if (!currentEnemy.isAlive()) {
            autoAttackTimer.stop();
            playDeathAnimation();
        }
    }

    private void playDeathAnimation() {
        Timer t = new Timer(50, null);
        t.addActionListener(e -> {
            bossOpacity -= 0.05f;
            if (bossOpacity <= 0) {
                bossOpacity = 0;
                t.stop();
                showVictoryScreen = true;
            }
            repaint();
        });
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (bg != null) g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), null);

        if (playerImg != null) {
            g2d.drawImage(playerImg, 150 + playerOffsetX, 250, 200, 250, null);
            if (playerOverlay.getAlpha() > 0) {
                g2d.setColor(playerOverlay);
                g2d.fillOval(150, 250, 200, 250);
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

        if (actionPanelImg != null) {
            if (!canAction || (fireballCount <= 0)) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }
            g2d.drawImage(actionPanelImg, 350, 480, 350, 100, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        drawStatusBars(g2d);

        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Serif", Font.PLAIN, 16));
        g2d.drawString("Fireballs: " + fireballCount, 420, 470);
        g2d.drawString("Potions: " + player.getPotionCount(), 540, 470);

        if (isGameOver || showVictoryScreen) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            BufferedImage currentImg = isGameOver ? gameOverImg : victoryImg;
            if (currentImg != null) {
                int drawW = 600;
                int drawH = 350;
                int x = (getWidth() - drawW) / 2;
                int y = (getHeight() - drawH) / 2;
                g2d.drawImage(currentImg, x, y, drawW, drawH, null);
            }
        }
    }

    private void drawStatusBars(Graphics2D g2d) {
        drawBar(g2d, 50, 30, player.getName(), player.getHp(), player.getMaxHp(), Color.GREEN);
        drawBar(g2d, 680, 30, currentEnemy.getName(), currentEnemy.getHp(), currentEnemy.getMaxHp(), Color.ORANGE);
    }

    private void drawBar(Graphics2D g2d, int x, int y, String name, int hp, int maxHp, Color barColor) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(x, y, 250, 70);
        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font("Serif", Font.PLAIN, 14));
        g2d.drawString(name, x + 10, y + 25);
        g2d.setColor(new Color(60, 0, 0));
        g2d.fillRect(x + 10, y + 40, 200, 10);
        g2d.setColor(barColor);
        int barWidth = (int) (200 * ((double) Math.max(0, hp) / maxHp));
        g2d.fillRect(x + 10, y + 40, barWidth, 10);
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