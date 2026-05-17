package fantasyrpg.services;

import fantasyrpg.entities.Character;
import fantasyrpg.entities.Player;

import java.util.Random;

public class RandomEventService {
    private final Random random;

    public RandomEventService(Random random) {
        this.random = random;
    }

    public String triggerRoundEvent(Player player, Character enemy) {
        int roll = random.nextInt(100);

        if (roll < 20) {
            player.setAttackMultiplier(1.5);
            return "Blessing of Valor: serangan player meningkat 50% di ronde ini.";
        }
        if (roll < 35) {
            enemy.setAttackMultiplier(1.4);
            return "Enemy Fury: serangan musuh meningkat 40% di ronde ini.";
        }
        if (roll < 50) {
            player.heal(10);
            return "Healing Mist: player memulihkan 10 HP.";
        }
        if (roll < 60) {
            enemy.setDefenseMultiplier(0.7);
            return "Armor Break: pertahanan musuh menurun di ronde ini.";
        }
        if (roll < 70) {
            player.setDefenseMultiplier(1.6);
            return "Guardian Aura: pertahanan player meningkat di ronde ini.";
        }
        return "Tidak ada event spesial pada ronde ini.";
    }
}
