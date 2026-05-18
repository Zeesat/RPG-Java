package fantasyrpg.ui.world;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WorldFrame::new);
    }
}
