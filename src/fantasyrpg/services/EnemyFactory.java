package fantasyrpg.services;

import java.util.Random;

import fantasyrpg.entities.DragonBoss;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Goblin;
import fantasyrpg.entities.OrcWarrior;

public class EnemyFactory {
    public Enemy createStageEnemy(int stage, Random random) {
        if (stage >= 3) {
            return new DragonBoss();
        }
        if (stage == 2) {
            return random.nextBoolean() ? new OrcWarrior() : new Goblin();
        }
        return new Goblin();
    }
}

