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
            player.heal(10);
            return "Healing Mist: +10 HP";
        }

        if (roll < 35) {
            player.heal(5);
            return "Blessing: +5 HP";
        }

        if (roll < 50) {
            return "Dragon watches carefully...";
        }

        if (roll < 70) {
            return "The battlefield trembles...";
        }

        return "Tidak ada event spesial.";
    }
}