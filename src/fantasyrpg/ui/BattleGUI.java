package fantasyrpg.ui;

import fantasyrpg.entities.DragonBoss;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Goblin;
import fantasyrpg.entities.Player;
import fantasyrpg.services.BattleService;
import fantasyrpg.services.RandomEventService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class BattleGUI extends JPanel {

    private BufferedImage bg;
    private BufferedImage playerImg;
    private BufferedImage bossImg;
    private BufferedImage actionPanelImg;
    private BufferedImage gameOverImg;
    private BufferedImage victoryImg;
    private BufferedImage arinWinImg;
    private BufferedImage arinLoseImg;

    private final Player player;
    private Enemy currentEnemy;
    private final BattleService battleService;
    private final Deque<String> battleLogs = new ArrayDeque<>();

    private Timer dragonAttackTimer;
    private Timer defendTimer;

    private int round = 1;
    private int stage = 1;

    private int playerOffsetX = 0;
    private int bossOffsetX = 0;
    private int fireballX = -100;
    private int goblinProjectileX = 700;

    private boolean showFireball = false;
    private boolean showGoblinProjectile = false;
    private boolean canAction = true;
    private boolean actionLocked = false;
    private boolean isGameOver = false;
    private boolean showVictoryScreen = false;

    private float bossOpacity = 1.0f;
    private Color playerOverlay = new Color(0, 0, 0, 0);

    public BattleGUI(Player player) {
        this.player = player;
        this.currentEnemy = new DragonBoss();

        this.battleService = new BattleService(
                new RandomEventService(new Random())
        );

        setFocusable(true);
        SwingUtilities.invokeLater(this::requestFocusInWindow);

        loadCommonAssets();
        loadStageAssets();

        beginBattle();
        registerInputs();
    }

    private void loadCommonAssets() {
        actionPanelImg = loadImage("assets/actionpanel.png");
        gameOverImg = loadImage("assets/game_over.png");
        victoryImg = loadImage("assets/victory.png");
        playerImg = loadImage("assets/player.png");
        arinWinImg = loadImage("assets/arin_win.png");
        arinLoseImg = loadImage("assets/arin_lose.png");
    }

    private void loadStageAssets() {
        if (stage == 1) {
            bg = loadImage("assets/background.png");
            bossImg = loadImage("assets/boss.png");
        } else if (stage == 2) {
            bg = loadImage("assets/background_stage2.png");
            bossImg = loadImage("assets/goblin_king.png");
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            return null;
        }
    }

    private void beginBattle() {
        addLog("Stage " + stage + " dimulai!");
        beginRound();
        startEnemyAutoAttack();
    }

    private void beginRound() {
        String eventMessage = battleService.beginRound(player, currentEnemy);
        addLog("Ronde " + round + ": " + eventMessage);
    }

    private void registerInputs() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isGameOver) {
                    resetGame();
                    return;
                }

                if (showVictoryScreen) {
                    System.out.println("Victory! Rewards Collected.");
                    System.exit(0);
                    return;
                }

                if (!canAction || actionLocked) {
                    return;
                }

                int mx = e.getX();
                int my = e.getY();

                if (mx >= 350 && mx <= 700 && my >= 480 && my <= 580) {
                    int section = (mx - 350) / (350 / 4);
                    executeAction(section);
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isGameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        resetGame();
                    }
                    return;
                }

                if (showVictoryScreen) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        System.out.println("Victory! Rewards Collected.");
                        System.exit(0);
                    }
                    return;
                }

                if (!canAction || actionLocked) {
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_1 -> executeAction(0);
                    case KeyEvent.VK_2 -> executeAction(1);
                    case KeyEvent.VK_3 -> executeAction(2);
                    case KeyEvent.VK_4 -> executeAction(3);
                }
            }
        });
    }

    private void executeAction(int section) {
        BattleService.PlayerAction action = switch (section) {
            case 0 -> BattleService.PlayerAction.ATTACK;
            case 1 -> BattleService.PlayerAction.SKILL;
            case 2 -> BattleService.PlayerAction.DEFEND;
            case 3 -> BattleService.PlayerAction.POTION;
            default -> null;
        };

        if (action == null) return;

        switch (action) {
            case ATTACK -> {
                lockActions(800);
                playPlayerAttack(() -> resolvePlayerAction(action));
            }

            case SKILL -> {
                if (!player.canUseFireball()) {
                    addLog("Fireball sudah habis!");
                    repaint();
                    return;
                }

                lockActions(1500);
                playFireballAnimation(() -> resolvePlayerAction(action));
            }

            case DEFEND -> {
                lockActions(3000);
                resolvePlayerAction(action);
                activateDefendMode();
            }

            case POTION -> {
                lockActions(1000);
                playPotionEffect(() -> resolvePlayerAction(action));
            }
        }
    }

    private void lockActions(int duration) {
        actionLocked = true;

        Timer t = new Timer(duration, e -> {
            actionLocked = false;
            ((Timer)e.getSource()).stop();
        });

        t.setRepeats(false);
        t.start();
    }

    private void activateDefendMode() {
        playerOverlay = new Color(255, 255, 0, 150);

        if (defendTimer != null) {
            defendTimer.stop();
        }

        defendTimer = new Timer(3000, e -> {
            player.stopDefending();
            playerOverlay = new Color(0, 0, 0, 0);
            repaint();
            ((Timer)e.getSource()).stop();
        });

        defendTimer.setRepeats(false);
        defendTimer.start();
    }

    private void startEnemyAutoAttack() {
        stopEnemyAutoAttack();

        dragonAttackTimer = new Timer(2000, e -> {
            if (isGameOver || showVictoryScreen) return;
            if (!currentEnemy.isAlive()) return;

            if (currentEnemy instanceof Goblin) {
                playGoblinProjectile(this::resolveEnemyTurn);
            } else {
                playBossAttack(this::resolveEnemyTurn);
            }
        });

        dragonAttackTimer.start();
    }

    private void stopEnemyAutoAttack() {
        if (dragonAttackTimer != null) {
            dragonAttackTimer.stop();
        }
    }

    private void resolvePlayerAction(BattleService.PlayerAction action) {
        BattleService.ActionResult result =
                battleService.executePlayerAction(player, currentEnemy, action);

        addLog(result.getMessage());

        if (!currentEnemy.isAlive()) {
            stopEnemyAutoAttack();

            if (stage == 1) {
                advanceToStage2();
            } else {
                playDeathAnimation();
            }
        }

        repaint();
    }

    private void advanceToStage2() {
        stage = 2;
        currentEnemy = new Goblin();

        player.resetFireballCharges();
        player.addPotion(2);

        round = 1;
        bossOpacity = 1.0f;
        bossOffsetX = 0;
        goblinProjectileX = 700;
        showGoblinProjectile = false;

        loadStageAssets();

        addLog("Stage 1 selesai!");
        addLog("Reward: +2 Potion, Fireball Refill");
        addLog("Stage 2: Goblin King!");

        startEnemyAutoAttack();
        repaint();
    }

    private void resolveEnemyTurn() {
        if (!player.isAlive()) return;

        BattleService.ActionResult result =
                battleService.executeEnemyTurn(player, currentEnemy);

        addLog(result.getMessage());

        if (!player.isAlive()) {
            isGameOver = true;
            stopEnemyAutoAttack();
        } else {
            round++;
            beginRound();
        }

        repaint();
    }

    private void playPotionEffect(Runnable onComplete) {
        playerOverlay = new Color(0, 255, 0, 120);

        Timer t = new Timer(450, e -> {
            playerOverlay = new Color(0, 0, 0, 0);
            ((Timer)e.getSource()).stop();
            onComplete.run();
            repaint();
        });

        t.setRepeats(false);
        t.start();
    }

    private void playFireballAnimation(Runnable onComplete) {
        showFireball = true;
        fireballX = 250;

        Timer t = new Timer(15, e -> {
            if (fireballX < 600) {
                fireballX += 8;
            } else {
                showFireball = false;
                ((Timer)e.getSource()).stop();
                onComplete.run();
            }

            repaint();
        });

        t.start();
    }

    private void playPlayerAttack(Runnable onComplete) {
        Timer t = new Timer(10, e -> {
            if (playerOffsetX < 100) {
                playerOffsetX += 15;
            } else {
                playerOffsetX = 0;
                ((Timer)e.getSource()).stop();
                onComplete.run();
            }

            repaint();
        });

        t.start();
    }

    private void playBossAttack(Runnable onComplete) {
        Timer t = new Timer(20, e -> {
            if (bossOffsetX > -100) {
                bossOffsetX -= 15;
            } else {
                bossOffsetX = 0;
                ((Timer)e.getSource()).stop();
                onComplete.run();
            }

            repaint();
        });

        t.start();
    }

    private void playGoblinProjectile(Runnable onComplete) {
        showGoblinProjectile = true;
        goblinProjectileX = 700;

        Timer t = new Timer(15, e -> {
            if (goblinProjectileX > 220) {
                goblinProjectileX -= 10;
            } else {
                showGoblinProjectile = false;
                ((Timer)e.getSource()).stop();
                onComplete.run();
            }

            repaint();
        });

        t.start();
    }

    private void playDeathAnimation() {
        Timer t = new Timer(50, e -> {
            bossOpacity -= 0.05f;

            if (bossOpacity <= 0) {
                bossOpacity = 0;
                showVictoryScreen = true;
                ((Timer)e.getSource()).stop();
            }

            repaint();
        });

        t.start();
    }

    private void resetGame() {
        stopEnemyAutoAttack();

        if (defendTimer != null) {
            defendTimer.stop();
        }

        stage = 1;
        currentEnemy = new DragonBoss();

        player.setHp(player.getMaxHp());
        player.setPotionCount(4);
        player.resetFireballCharges();
        player.stopDefending();

        round = 1;
        playerOffsetX = 0;
        bossOffsetX = 0;
        fireballX = -100;
        goblinProjectileX = 700;
        bossOpacity = 1.0f;

        showFireball = false;
        showGoblinProjectile = false;
        canAction = true;
        actionLocked = false;
        isGameOver = false;
        showVictoryScreen = false;
        playerOverlay = new Color(0, 0, 0, 0);

        battleLogs.clear();

        loadStageAssets();
        beginBattle();

        repaint();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    private void addLog(String message) {
        battleLogs.addFirst(message);

        if (battleLogs.size() > 5) {
            battleLogs.removeLast();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (bg != null) {
            g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        }

        BufferedImage currentPlayerImage = playerImg;

        if (showVictoryScreen) currentPlayerImage = arinWinImg;
        else if (isGameOver) currentPlayerImage = arinLoseImg;

        if (currentPlayerImage != null) {
            g2d.drawImage(
                    currentPlayerImage,
                    150 + playerOffsetX,
                    250,
                    200,
                    250,
                    null
            );

            if (playerOverlay.getAlpha() > 0) {
                g2d.setColor(playerOverlay);
                g2d.fillOval(150, 250, 180, 220);
            }
        }

        if (showFireball) {
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(fireballX, 300, 40, 40);
        }

        if (showGoblinProjectile) {
            g2d.setColor(new Color(0, 255, 80));
            g2d.fillOval(goblinProjectileX, 290, 55, 55);

            g2d.setColor(new Color(180, 255, 180, 120));
            g2d.fillOval(goblinProjectileX - 8, 282, 70, 70);
        }

        if (bossImg != null) {
            g2d.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            bossOpacity
                    )
            );

            g2d.drawImage(
                    bossImg,
                    550 + bossOffsetX,
                    120,
                    350,
                    350,
                    null
            );

            g2d.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            1.0f
                    )
            );
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
            g2d.drawImage(actionPanelImg, 350, 480, 350, 100, null);
        }

        g2d.setColor(Color.WHITE);
        g2d.drawString("Potion: " + player.getPotionCount(), 370, 590);
        g2d.drawString("Fireball: " + player.getFireballCharges() + "/3", 470, 590);
        g2d.drawString("Stage: " + stage, 620, 590);
    }

    private void drawBattleLog(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(40, 460, 290, 130, 12, 12);

        g2d.setColor(Color.WHITE);

        int y = 485;

        for (String line : battleLogs) {
            g2d.drawString(line, 50, y);
            y += 22;
        }
    }

    private void drawStatusBars(Graphics2D g2d) {
        drawBar(
                g2d,
                50,
                30,
                player.getName(),
                player.getHp(),
                player.getMaxHp(),
                Color.GREEN,
                "LV " + player.getLevel()
        );

        drawBar(
                g2d,
                680,
                30,
                currentEnemy.getName(),
                currentEnemy.getHp(),
                currentEnemy.getMaxHp(),
                Color.ORANGE,
                "ENEMY"
        );
    }

    private void drawBar(
            Graphics2D g2d,
            int x,
            int y,
            String name,
            int hp,
            int maxHp,
            Color color,
            String label
    ) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(x, y, 260, 78);

        g2d.setColor(Color.WHITE);
        g2d.drawString(name + " (" + label + ")", x + 10, y + 25);

        g2d.setColor(Color.RED);
        g2d.fillRect(x + 10, y + 48, 210, 12);

        g2d.setColor(color);

        int width = (int)(210 * ((double)Math.max(0, hp) / maxHp));
        g2d.fillRect(x + 10, y + 48, width, 12);
    }

    private void drawEndOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        BufferedImage img = isGameOver ? gameOverImg : victoryImg;

        if (img != null) {
            g2d.drawImage(img, 200, 150, 600, 350, null);
        }
    }
}