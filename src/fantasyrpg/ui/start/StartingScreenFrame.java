package fantasyrpg.ui.start;

import fantasyrpg.ui.world.WorldFrame;
import java.awt.Color;
import java.awt.Font;
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
    }

    private void moveToWorld() {
        if (movingToWorld) {
            return;
        }

        movingToWorld = true;
        dispose();
        SwingUtilities.invokeLater(WorldFrame::new);
    }

    public static void showScreen() {
        SwingUtilities.invokeLater(() -> {
            StartingScreenFrame frame = new StartingScreenFrame();
            frame.setVisible(true);
        });
    }

    private static final class StartingScreenPanel extends JPanel {
        private final Runnable onContinue;
        private final Image backgroundImage;

        private StartingScreenPanel(Runnable onContinue) {
            this.onContinue = onContinue;
            setBackground(new Color(16, 20, 26));
            setFocusable(true);
            this.backgroundImage = loadBackgroundImage();

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    onContinue.run();
                }
            });
        }

        private Image loadBackgroundImage() {
            try {
                return ImageIO.read(new File("assets/start/start_screen.png"));
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(230, 236, 242));
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
                g2.drawString("Failed to load: assets/start/start_screen.png", 120, getHeight() / 2);
            }

            g2.dispose();
        }
    }
}
