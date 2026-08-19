package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;
import java.util.Random;

public class Golem extends Enemy implements SkillUser {
    private final Random random = new Random();

    public Golem() {
        super("Stone Golem", 130, 14, 12, 100, 400, 5);
    }

    @Override
    public int getSkillThreshold() { return 40; }

    @Override
    public String getAttackName() { return "Stone Fist"; }

    @Override
    public String getSkillName() { return "Boulder Smash"; }

    @Override
    public int attack(Character target) {
        int damage = getAttackPower() + random.nextInt(9); 
        int finalDmg = (int) Math.round(damage * getAttackMultiplier());
        target.receiveDamage(finalDmg);
        return finalDmg;
    }

    @Override
    public int useSkill(Character target) {
        int damage = 25 + random.nextInt(11); 
        int finalDmg = (int) Math.round(damage * getAttackMultiplier());
        target.receiveDamage(finalDmg);
        return finalDmg;
    }
}
