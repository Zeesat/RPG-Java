package fantasyrpg.services;

import fantasyrpg.entities.DragonBoss;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Goblin;
import fantasyrpg.entities.Player;
import fantasyrpg.interfaces.SkillUser;

import java.util.Scanner;

public class BattleService {

    public enum PlayerAction { ATTACK, SKILL, DEFEND, POTION }

    public static class ActionResult {
        private final String message;
        private final int damage;

        public ActionResult(String message, int damage) {
            this.message = message;
            this.damage  = damage;
        }
        public String getMessage() { return message; }
        public int    getDamage()  { return damage; }
    }

    private final RandomEventService randomEventService;

    public BattleService(RandomEventService randomEventService) {
        this.randomEventService = randomEventService;
    }

    
    
    
    public boolean startBattle(Player player, Enemy enemy, Scanner scanner) {
        while (player.isAlive() && enemy.isAlive()) {
            System.out.println(beginRound(player, enemy));

            String input = scanner.nextLine().trim();
            PlayerAction action = switch (input) {
                case "2" -> PlayerAction.SKILL;
                case "3" -> PlayerAction.DEFEND;
                case "4" -> PlayerAction.POTION;
                default  -> PlayerAction.ATTACK;
            };

            ActionResult playerResult = executePlayerAction(player, enemy, action);
            System.out.println(playerResult.getMessage());

            if (!enemy.isAlive()) { applyVictoryRewards(player, enemy); break; }

            ActionResult enemyResult = executeEnemyTurn(player, enemy);
            System.out.println(enemyResult.getMessage());
        }
        return player.isAlive();
    }

    
    
    
    public String beginRound(Player player, Enemy enemy) {
        player.restoreTurnModifiers();
        enemy.restoreTurnModifiers();

        
        player.tickCooldowns();

        return randomEventService.triggerRoundEvent(player, enemy);
    }

    
    
    
    public ActionResult executePlayerAction(Player player, Enemy enemy, PlayerAction action) {
        return switch (action) {

            case ATTACK -> {
                int dmg = player.attack(enemy);
                yield new ActionResult(
                        player.getName() + " menyerang! ⚔  −" + dmg + " HP",
                        dmg
                );
            }

            case SKILL -> {
                
                if (player.getSkillCooldown() > 0) {
                    yield new ActionResult(
                            "⏳ Fireball cooldown " + player.getSkillCooldown() + " turn lagi!", 0
                    );
                }
                if (!player.canUseFireball()) {
                    yield new ActionResult("🔥 Fireball sudah habis! Tidak bisa digunakan.", 0);
                }
                int dmg = player.useSkill(enemy);
                yield new ActionResult(
                        player.getName() + " melepaskan FIREBALL! 🔥  −" + dmg
                                + " HP  [Cooldown: " + player.getSkillCooldown() + " turn]",
                        dmg
                );
            }

            case DEFEND -> {
                player.defend();
                yield new ActionResult(
                        player.getName() + " mengambil posisi bertahan! 🛡  Damage masuk −55%.",
                        0
                );
            }

            case POTION -> {
                
                if (player.getPotionCooldown() > 0) {
                    yield new ActionResult(
                            "⏳ Potion cooldown " + player.getPotionCooldown() + " turn lagi!", 0
                    );
                }
                if (player.getPotionCount() <= 0) {
                    yield new ActionResult("🧪 Tidak ada potion tersisa!", 0);
                }
                int hpBefore = player.getHp();
                boolean used = player.usePotion();
                if (!used) {
                    yield new ActionResult("🧪 Potion tidak bisa digunakan saat ini.", 0);
                }
                int healed = player.getHp() - hpBefore;
                yield new ActionResult(
                        player.getName() + " meminum potion! 💊  +" + healed
                                + " HP  [Tersisa: " + player.getPotionCount()
                                + "  |  Cooldown: " + player.getPotionCooldown() + " turn]",
                        0
                );
            }

            default -> new ActionResult("Aksi tidak dikenal.", 0);
        };
    }

    
    
    
    public ActionResult executeEnemyTurn(Player player, Enemy enemy) {
        int damage;

        
        double skillThreshold = (enemy instanceof Goblin) ? 0.60 : 0.50;
        boolean shouldUseSkill =
                enemy instanceof SkillUser
                        && enemy.getHp() <= (int)(enemy.getMaxHp() * skillThreshold);

        if (shouldUseSkill) {
            damage = ((SkillUser) enemy).useSkill(player);

            String skillName = "Special Attack";
            if (enemy instanceof DragonBoss) skillName = "💥 DRAGON BREATH";
            else if (enemy instanceof Goblin) skillName = "⚡ GOBLIN RAMPAGE";

            return new ActionResult(
                    enemy.getName() + " menggunakan " + skillName + "!  −" + damage + " HP",
                    damage
            );
        }

        damage = enemy.attack(player);
        return new ActionResult(
                enemy.getName() + " menyerang!  −" + damage + " HP",
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
                "🏆 VICTORY!  EXP +" + enemy.getRewardExperience()
                        + "  |  Score +" + (enemy.getRewardScore() + bonus),
                0
        );
    }
}