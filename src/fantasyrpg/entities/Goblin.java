package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;

public class Goblin extends Enemy implements SkillUser {

    public Goblin() {
        super("Goblin King", 120, 13, 3, 120, 600, 7);
    }

    @Override
    public int getSkillThreshold() { return 60; }

    @Override
    public String getAttackName() { return "Dagger Slash"; }

    @Override
    public String getSkillName() { return "Goblin Rampage"; }

    @Override
    public int attack(Character target) {
        int damage = calculateBaseAttack() + 2;
        target.receiveDamage(damage);
        return damage;
    }

    @Override
    public int useSkill(Character target) {
        int damage = calculateBaseAttack() + 8;
        target.receiveDamage(damage);
        return damage;
    }
}
