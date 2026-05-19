package fantasyrpg;

import fantasyrpg.ui.battle.GameFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(GameFrame::new);
    }
}

