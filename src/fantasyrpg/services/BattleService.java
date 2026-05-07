package fantasyrpg.services;

import java.util.Scanner;

import fantasyrpg.entities.Character;
import fantasyrpg.entities.DragonBoss;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Player;
import fantasyrpg.interfaces.SkillUser;
import fantasyrpg.util.ConsoleFormatter;

public class BattleService {
    private final RandomEventService randomEventService;

    public BattleService(RandomEventService randomEventService) {
        this.randomEventService = randomEventService;
    }

    public boolean startBattle(Player player, Enemy enemy, Scanner scanner) {
        int round = 1;

        ConsoleFormatter.printSection("Battle Start");
        System.out.println("Musuh muncul: " + enemy.getName());

        while (player.isAlive() && enemy.isAlive()) {
            player.restoreTurnModifiers();
            enemy.restoreTurnModifiers();

            ConsoleFormatter.printRound(round);
            System.out.println(randomEventService.triggerRoundEvent(player, enemy));
            showStatus(player, enemy);

            handlePlayerTurn(player, enemy, scanner);
            if (!enemy.isAlive()) {
                break;
            }

            handleEnemyTurn(player, enemy);
            round++;
        }

        if (player.isAlive()) {
            ConsoleFormatter.printSection("Victory");
            System.out.println("Kamu mengalahkan " + enemy.getName() + "!");
            player.gainExperience(enemy.getRewardExperience());
            player.addScore(enemy.getRewardScore());
            if (enemy instanceof DragonBoss) {
                player.addScore(1000);
            }
            return true;
        }

        ConsoleFormatter.printSection("Defeat");
        System.out.println("Petualanganmu berakhir di tangan " + enemy.getName() + ".");
        return false;
    }

    private void handlePlayerTurn(Player player, Enemy enemy, Scanner scanner) {
        System.out.println("Aksi:");
        System.out.println("1. Attack");
        System.out.println("2. Skill");
        System.out.println("3. Defend");
        System.out.println("4. Potion");
        System.out.print("Pilih aksi: ");

        String input = scanner.nextLine().trim();
        int damage;

        switch (input) {
            case "2":
                damage = player.useSkill(enemy);
                System.out.println(player.getName() + " menggunakan skill dan memberi " + damage + " damage.");
                break;
            case "3":
                player.defend();
                System.out.println(player.getName() + " bersiap bertahan.");
                break;
            case "4":
                boolean usedPotion = player.usePotion();
                if (usedPotion) {
                    System.out.println(player.getName() + " meminum potion. HP sekarang: " + player.getHp());
                } else {
                    System.out.println("Potion habis. Serangan normal dilakukan sebagai gantinya.");
                    damage = player.attack(enemy);
                    System.out.println(player.getName() + " menyerang dan memberi " + damage + " damage.");
                }
                break;
            case "1":
            default:
                damage = player.attack(enemy);
                System.out.println(player.getName() + " menyerang dan memberi " + damage + " damage.");
                break;
        }
    }

    private void handleEnemyTurn(Player player, Enemy enemy) {
        int damage;
        if (enemy instanceof SkillUser skillUser && enemy.getHp() < (enemy.getMaxHp() / 2)) {
            damage = skillUser.useSkill(player);
            System.out.println(enemy.getName() + " menggunakan skill dan memberi " + damage + " damage.");
            return;
        }

        damage = enemy.attack(player);
        System.out.println(enemy.getName() + " menyerang dan memberi " + damage + " damage.");
    }

    private void showStatus(Player player, Character enemy) {
        System.out.println(player.getName() + " HP: " + player.getHp() + "/" + player.getMaxHp()
                + " | LV: " + player.getLevel()
                + " | EXP: " + player.getExperience()
                + " | Score: " + player.getScore()
                + " | Potion: " + player.getPotionCount());
        System.out.println(enemy.getName() + " HP: " + enemy.getHp() + "/" + enemy.getMaxHp());
    }
}

