package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;
import java.util.Random;

public class Slime extends Enemy implements SkillUser {
    private final Random random = new Random();

    public Slime() {
        super("Slime", 80, 9, 2, 50, 150);
    }

    @Override
    public int attack(Character target) {
        int damage = getAttackPower() + random.nextInt(6); 
        int finalDmg = (int) Math.round(damage * getAttackMultiplier());
        target.receiveDamage(finalDmg);
        return finalDmg;
    }

    @Override
    public int useSkill(Character target) {
        int damage = 18 + random.nextInt(8); 
        int finalDmg = (int) Math.round(damage * getAttackMultiplier());
        target.receiveDamage(finalDmg);
        return finalDmg;
    }
}
