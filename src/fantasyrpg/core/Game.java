package fantasyrpg.core;

import fantasyrpg.entities.DragonBoss;
import fantasyrpg.entities.Player;
import fantasyrpg.ui.BattleGUI;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Game {

    public void start() {
        SwingUtilities.invokeLater(() -> {
            Player player = new Player("Arin");
            DragonBoss dragonBoss = new DragonBoss();

            JFrame frame = new JFrame("Dungeon Battle: Rise of Hero");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 650);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);

            frame.add(new BattleGUI(player, dragonBoss));

            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        new Game().start();
    }
}