package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;
import java.util.Random;

public class Slime extends Enemy implements SkillUser {
    private final Random random = new Random();

    public Slime() {
        super("Slime", 80, 9, 2, 50, 150, 3);
    }

    @Override
    public int getSkillThreshold() { return 50; }

    @Override
    public String getAttackName() { return "Slime Lash"; }

    @Override
    public String getSkillName() { return "Acid Surge"; }

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
