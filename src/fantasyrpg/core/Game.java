package fantasyrpg.core;

import java.util.Random;
import java.util.Scanner;

import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Player;
import fantasyrpg.services.BattleService;
import fantasyrpg.services.EnemyFactory;
import fantasyrpg.services.RandomEventService;
import fantasyrpg.util.ConsoleFormatter;

public class Game {
    private final Scanner scanner;
    private final Random random;
    private final EnemyFactory enemyFactory;
    private final BattleService battleService;

    public Game() {
        this.scanner = new Scanner(System.in);
        this.random = new Random();
        this.enemyFactory = new EnemyFactory();
        this.battleService = new BattleService(new RandomEventService(random));
    }

    public void start() {
        ConsoleFormatter.printSection("Fantasy Turn-Based RPG");
        System.out.print("Masukkan nama hero: ");
        String playerName = scanner.nextLine().trim();
        if (playerName.isEmpty()) {
            playerName = "Arin";
        }

        Player player = new Player(playerName);
        explainObjective();
        runAdventure(player);
        scanner.close();
    }

    private void explainObjective() {
        System.out.println("Tujuanmu adalah menembus 3 stage dan mengalahkan boss akhir.");
        System.out.println("Setiap ronde bisa muncul buff, multiplier, atau event acak.");
        System.out.println();
    }

    private void runAdventure(Player player) {
        for (int stage = 1; stage <= 3; stage++) {
            ConsoleFormatter.printSection("Stage " + stage);
            Enemy enemy = enemyFactory.createStageEnemy(stage, random);
            boolean victory = battleService.startBattle(player, enemy, scanner);

            if (!victory) {
                printGameOver(player);
                return;
            }

            grantStageReward(player, stage);
        }

        printEnding(player);
    }

    private void grantStageReward(Player player, int stage) {
        if (stage < 3) {
            player.addPotion(1);
            System.out.println("Hadiah stage: +1 potion.");
        }
        System.out.println("Status akhir stage -> Level: " + player.getLevel() + ", Score: " + player.getScore());
        System.out.println();
    }

    private void printGameOver(Player player) {
        ConsoleFormatter.printSection("Game Over");
        System.out.println("Hero: " + player.getName());
        System.out.println("Level akhir: " + player.getLevel());
        System.out.println("Score akhir: " + player.getScore());
    }

    private void printEnding(Player player) {
        ConsoleFormatter.printSection("Final Victory");
        System.out.println(player.getName() + " berhasil mengalahkan boss akhir dan menyelamatkan kerajaan.");
        System.out.println("Level akhir: " + player.getLevel());
        System.out.println("Score akhir: " + player.getScore());
    }
}

