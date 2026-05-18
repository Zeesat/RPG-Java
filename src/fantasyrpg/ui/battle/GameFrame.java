package fantasyrpg.ui.battle;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class GameFrame extends JFrame {
    public GameFrame() {
        super("Fantasy RPG");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);

        GamePanel gamePanel = new GamePanel();

        setContentPane(gamePanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
