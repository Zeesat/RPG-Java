package fantasyrpg.services;

import fantasyrpg.entities.DragonBoss;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Player;
import fantasyrpg.interfaces.SkillUser;

import java.util.Scanner;

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

    // compatibility method for old Game.java
    public boolean startBattle(
            Player player,
            Enemy enemy,
            Scanner scanner
    ) {
        while (player.isAlive() && enemy.isAlive()) {
            System.out.println(beginRound(player, enemy));

            String input = scanner.nextLine().trim();

            PlayerAction action = switch (input) {
                case "2" -> PlayerAction.SKILL;
                case "3" -> PlayerAction.DEFEND;
                case "4" -> PlayerAction.POTION;
                default -> PlayerAction.ATTACK;
            };

            ActionResult playerResult =
                    executePlayerAction(player, enemy, action);

            System.out.println(playerResult.getMessage());

            if (!enemy.isAlive()) {
                applyVictoryRewards(player, enemy);
                break;
            }

            ActionResult enemyResult =
                    executeEnemyTurn(player, enemy);

            System.out.println(enemyResult.getMessage());
        }

        return player.isAlive();
    }

    public String beginRound(Player player, Enemy enemy) {
        return randomEventService.triggerRoundEvent(player, enemy);
    }

    public ActionResult executePlayerAction(
            Player player,
            Enemy enemy,
            PlayerAction action
    ) {
        switch (action) {
            case ATTACK -> {
                int damage = player.attack(enemy);
                return new ActionResult(
                        player.getName() + " menyerang: " + damage + " damage.",
                        damage
                );
            }

            case SKILL -> {
                if (!player.canUseFireball()) {
                    return new ActionResult("Fireball sudah habis!", 0);
                }

                int damage = player.useSkill(enemy);

                return new ActionResult(
                        player.getName() + " melepaskan FIREBALL! " + damage + " damage!",
                        damage
                );
            }

            case DEFEND -> {
                player.defend();

                return new ActionResult(
                        player.getName() + " bertahan!",
                        0
                );
            }

            case POTION -> {
                if (!player.usePotion()) {
                    return new ActionResult("Potion habis!", 0);
                }

                return new ActionResult(
                        player.getName() + " memulihkan 50 HP.",
                        0
                );
            }

            default -> {
                return new ActionResult("Aksi tidak dikenal.", 0);
            }
        }
    }

    public ActionResult executeEnemyTurn(Player player, Enemy enemy) {
        int damage;

        if (enemy instanceof SkillUser skillUser &&
                enemy.getHp() <= enemy.getMaxHp() / 2) {

            damage = skillUser.useSkill(player);

            return new ActionResult(
                    enemy.getName() + " menggunakan Dragon Breath! -" + damage + " HP",
                    damage
            );
        }

        damage = enemy.attack(player);

        return new ActionResult(
                enemy.getName() + " menyerang! -" + damage + " HP",
                damage
        );
    }

    public ActionResult applyVictoryRewards(Player player, Enemy enemy) {
        player.gainExperience(enemy.getRewardExperience());
        player.addScore(enemy.getRewardScore());

        int bonus = 0;

        if (enemy instanceof DragonBoss) {
            bonus = 1000;
            player.addScore(bonus);
        }

        return new ActionResult(
                "VICTORY! EXP +" +
                        enemy.getRewardExperience() +
                        " | Score +" +
                        (enemy.getRewardScore() + bonus),
                0
        );
    }
}