package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;

public class Goblin extends Enemy implements SkillUser {

    public Goblin() {
        super(
                "Goblin King",
                260,
                16,
                4,
                150,
                800
        );
    }

    @Override
    public int attack(Character target) {
        int damage = 16;
        target.receiveDamage(damage);
        return damage;
    }

    @Override
    public int useSkill(Character target) {
        int damage = 26;
        target.receiveDamage(damage);
        return damage;
    }
}