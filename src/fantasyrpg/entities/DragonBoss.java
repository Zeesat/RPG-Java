package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;

public class DragonBoss extends Enemy implements SkillUser {

    public DragonBoss() {
        super(
                "Azhrax the Ash Dragon",
                320,
                18,
                6,
                100,
                500
        );
    }

    @Override
    public int attack(Character target) {
        int damage = 18;
        target.receiveDamage(damage);
        return damage;
    }

    @Override
    public int useSkill(Character target) {
        int damage = 30;
        target.receiveDamage(damage);
        return damage;
    }
}