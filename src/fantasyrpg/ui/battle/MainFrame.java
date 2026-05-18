package fantasyrpg.ui.battle;

import javax.swing.SwingUtilities;

public class MainFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
