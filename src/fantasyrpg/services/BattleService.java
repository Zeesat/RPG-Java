package fantasyrpg.services;

import java.util.Scanner;

import fantasyrpg.entities.Character;
import fantasyrpg.entities.DragonBoss;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Player;
import fantasyrpg.interfaces.SkillUser;
import fantasyrpg.util.ConsoleFormatter;

public class BattleService {
    public enum PlayerAction {
        ATTACK,
        SKILL,
        DEFEND,
        POTION
    }

    public static class ActionResult {
        private final String message;
        private final int damage;

        public ActionResult(String message, int damage) {
            this.message = message;
            this.damage = damage;
        }

        public String getMessage() {
            return message;
        }

        public int getDamage() {
            return damage;
        }
    }

    private final RandomEventService randomEventService;

    public BattleService(RandomEventService randomEventService) {
        this.randomEventService = randomEventService;
    }

    public boolean startBattle(Player player, Enemy enemy, Scanner scanner) {
        int round = 1;

        ConsoleFormatter.printSection("Battle Start");
        System.out.println("Musuh muncul: " + enemy.getName());

        while (player.isAlive() && enemy.isAlive()) {
            String roundEvent = beginRound(player, enemy);

            ConsoleFormatter.printRound(round);
            System.out.println(roundEvent);
            showStatus(player, enemy);

            handlePlayerTurn(player, enemy, scanner);
            if (!enemy.isAlive()) {
                break;
            }

            handleEnemyTurn(player, enemy);
            round++;
        }

        if (player.isAlive()) {
            ActionResult rewardResult = applyVictoryRewards(player, enemy);
            ConsoleFormatter.printSection("Victory");
            System.out.println(rewardResult.getMessage());
            return true;
        }

        ConsoleFormatter.printSection("Defeat");
        System.out.println("Petualanganmu berakhir di tangan " + enemy.getName() + ".");
        return false;
    }

    public String beginRound(Player player, Enemy enemy) {
        player.restoreTurnModifiers();
        enemy.restoreTurnModifiers();
        return randomEventService.triggerRoundEvent(player, enemy);
    }

    public ActionResult executePlayerAction(Player player, Enemy enemy, PlayerAction action) {
        int damage;

        switch (action) {
            case SKILL:
                damage = player.useSkill(enemy);
                return new ActionResult(
                        player.getName() + " menggunakan skill dan memberi " + damage + " damage.",
                        damage
                );
            case DEFEND:
                player.defend();
                return new ActionResult(player.getName() + " bersiap bertahan.", 0);
            case POTION:
                boolean usedPotion = player.usePotion();
                if (usedPotion) {
                    return new ActionResult(
                            player.getName() + " meminum potion. HP sekarang: " + player.getHp(),
                            0
                    );
                }
                damage = player.attack(enemy);
                return new ActionResult(
                        "Potion habis. Serangan normal dilakukan sebagai gantinya. "
                                + player.getName() + " menyerang dan memberi " + damage + " damage.",
                        damage
                );
            case ATTACK:
            default:
                damage = player.attack(enemy);
                return new ActionResult(
                        player.getName() + " menyerang dan memberi " + damage + " damage.",
                        damage
                );
        }
    }

    public ActionResult executeEnemyTurn(Player player, Enemy enemy) {
        int damage;

        if (enemy instanceof SkillUser skillUser && enemy.getHp() < (enemy.getMaxHp() / 2)) {
            damage = skillUser.useSkill(player);
            return new ActionResult(
                    enemy.getName() + " menggunakan skill dan memberi " + damage + " damage.",
                    damage
            );
        }

        damage = enemy.attack(player);
        return new ActionResult(
                enemy.getName() + " menyerang dan memberi " + damage + " damage.",
                damage
        );
    }

    public ActionResult applyVictoryRewards(Player player, Enemy enemy) {
        player.gainExperience(enemy.getRewardExperience());
        player.addScore(enemy.getRewardScore());
        int bonusScore = 0;
        if (enemy instanceof DragonBoss) {
            bonusScore = 1000;
            player.addScore(bonusScore);
        }

        String message = "Kamu mengalahkan " + enemy.getName() + "!"
                + " EXP +" + enemy.getRewardExperience()
                + ", Score +" + enemy.getRewardScore();
        if (bonusScore > 0) {
            message += ", Bonus Boss +" + bonusScore;
        }

        return new ActionResult(message, 0);
    }

    private void handlePlayerTurn(Player player, Enemy enemy, Scanner scanner) {
        System.out.println("Aksi:");
        System.out.println("1. Attack");
        System.out.println("2. Skill");
        System.out.println("3. Defend");
        System.out.println("4. Potion");
        System.out.print("Pilih aksi: ");

        String input = scanner.nextLine().trim();
        ActionResult result;

        switch (input) {
            case "2":
                result = executePlayerAction(player, enemy, PlayerAction.SKILL);
                break;
            case "3":
                result = executePlayerAction(player, enemy, PlayerAction.DEFEND);
                break;
            case "4":
                result = executePlayerAction(player, enemy, PlayerAction.POTION);
                break;
            case "1":
            default:
                result = executePlayerAction(player, enemy, PlayerAction.ATTACK);
                break;
        }

        System.out.println(result.getMessage());
    }

    private void handleEnemyTurn(Player player, Enemy enemy) {
        ActionResult result = executeEnemyTurn(player, enemy);
        System.out.println(result.getMessage());
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

