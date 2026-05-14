package fantasyrpg.services;

import java.util.Random;

import fantasyrpg.entities.DragonBoss;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Goblin;
import fantasyrpg.entities.OrcWarrior;

public class EnemyFactory {

    public Enemy createStageEnemy(int stage, Random random) {
        if (stage <= 1) {
            return new Goblin();
        }

        if (stage == 2) {
            return new OrcWarrior();
        }

        return new DragonBoss();
    }
}