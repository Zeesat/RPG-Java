package fantasyrpg.ui.start;

import fantasyrpg.ui.world.WorldFrame;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

        private StartingScreenPanel(Runnable onContinue) {
            this.onContinue = onContinue;
            setBackground(new Color(16, 20, 26));
            setFocusable(true);

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    onContinue.run();
                }
            });
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
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(new Color(230, 236, 242));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
            g2.drawString("This is starting screen, GUI to be added", 120, getHeight() / 2 - 10);

            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 26));
            g2.drawString("Press any key to continue", 275, getHeight() / 2 + 45);

            g2.dispose();
        }
    }
}
