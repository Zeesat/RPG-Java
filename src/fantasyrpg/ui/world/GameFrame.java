package fantasyrpg.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class GameFrame extends JFrame {
    public GameFrame() {
        super("Fantasy RPG");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);

        fantasyrpg.ui.world.GamePanel gamePanel =
                new fantasyrpg.ui.world.GamePanel();

        setContentPane(gamePanel);
        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);

        gamePanel.requestFocusInWindow();
        gamePanel.startGameThread();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
