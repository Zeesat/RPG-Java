package fantasyrpg.ui.world;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class WorldFrame extends JFrame {
    public WorldFrame() {
        super("Dungeon Battle: Rise of Hero");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);

        GamePanel gamePanel = new GamePanel();

        setContentPane(gamePanel);
        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);

        gamePanel.requestFocusInWindow();
        gamePanel.startGameThread();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WorldFrame::new);
    }
}