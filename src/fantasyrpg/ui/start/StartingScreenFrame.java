package fantasyrpg.ui.start;

import fantasyrpg.ui.world.WorldFrame;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class StartingScreenFrame extends JFrame {
    private boolean movingToWorld;

    public StartingScreenFrame() {
        super("Fantasy RPG");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setContentPane(new StartingScreenPanel(this::moveToWorld));
        fantasyrpg.sound.SoundManager.playBGM("START");
    }

    private void moveToWorld() {
        if (movingToWorld) return;
        movingToWorld = true;
        dispose();
        fantasyrpg.sound.SoundManager.stopBGM();
        fantasyrpg.GameState.reset();
        SwingUtilities.invokeLater(WorldFrame::new);
    }

    public static void showScreen() {
        SwingUtilities.invokeLater(() -> {
            StartingScreenFrame f = new StartingScreenFrame();
            f.setVisible(true);
        });
    }

    
    private static final class StartingScreenPanel extends JPanel {
        private final Runnable onContinue;
        private final Image backgroundImage;

        
        private float fadeAlpha  = 1.0f;   
        private boolean fadeIn   = true;

        
        private final float[] px   = new float[40];
        private final float[] py   = new float[40];
        private final float[] pvy  = new float[40];
        private final float[] palpha = new float[40];

        private final Timer animTimer;

        StartingScreenPanel(Runnable onContinue) {
            this.onContinue = onContinue;
            setBackground(new Color(8, 10, 16));
            setFocusable(true);
            this.backgroundImage = loadBackgroundImage();

            
            java.util.Random rng = new java.util.Random();
            for (int i = 0; i < px.length; i++) {
                px[i]     = rng.nextFloat() * 900f;
                py[i]     = rng.nextFloat() * 500f;
                pvy[i]    = -(0.2f + rng.nextFloat() * 0.5f);
                palpha[i] = 0.1f + rng.nextFloat() * 0.4f;
            }

            animTimer = new Timer(30, e -> tick());
            animTimer.start();

            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    onContinue.run();
                }
            });
        }

        private void tick() {
            
            if (fadeIn) {
                fadeAlpha -= 0.04f;
                if (fadeAlpha <= 0f) { fadeAlpha = 0f; fadeIn = false; }
            }

            

            
            for (int i = 0; i < px.length; i++) {
                py[i] += pvy[i];
                if (py[i] < 0) {
                    py[i] = 510f;
                    px[i] = (float)(Math.random() * 900);
                }
            }
            repaint();
        }

        private Image loadBackgroundImage() {
            try { return ImageIO.read(new File("assets/start/start_screen.png")); }
            catch (IOException e) { return null; }
        }

        @Override public void addNotify() {
            super.addNotify();
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();

            
            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, W, H, this);
            } else {
                
                GradientPaint grad = new GradientPaint(0, 0, new Color(8, 10, 20), 0, H, new Color(20, 8, 8));
                g2.setPaint(grad);
                g2.fillRect(0, 0, W, H);

                drawFallbackContent(g2, W, H);
            }

            
            for (int i = 0; i < px.length; i++) {
                g2.setColor(new Color(200, 180, 120, (int)(palpha[i] * 180)));
                g2.fillOval((int) px[i], (int) py[i], 3, 3);
            }

            

            
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2.setColor(new Color(120, 110, 100, 160));
            g2.drawString("v1.0  RPG-Java", 12, H - 12);

            
            if (fadeAlpha > 0f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, W, H);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            g2.dispose();
        }

        private void drawFallbackContent(Graphics2D g2, int W, int H) {
            
            g2.setFont(new Font("Serif", Font.BOLD, 56));
            String title = "DUNGEON BATTLE";
            FontMetrics fm = g2.getFontMetrics();
            int tx = (W - fm.stringWidth(title)) / 2;

            
            for (int glow = 12; glow > 0; glow -= 3) {
                float a = (float) glow / 32f;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
                g2.setColor(new Color(200, 120, 40));
                g2.drawString(title, tx - glow/2, H/2 - 60 + glow/2);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(title, tx + 3, H/2 - 57);
            
            g2.setColor(new Color(255, 220, 140));
            g2.drawString(title, tx, H/2 - 60);

            
            g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
            String sub = "Rise of Hero";
            fm = g2.getFontMetrics();
            g2.setColor(new Color(180, 160, 120));
            g2.drawString(sub, (W - fm.stringWidth(sub)) / 2, H/2 - 20);

            
            g2.setColor(new Color(160, 100, 40, 180));
            g2.fillRect(W/2 - 120, H/2 - 4, 240, 2);
        }
    }
}