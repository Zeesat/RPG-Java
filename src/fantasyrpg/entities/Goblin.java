package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;










public class Goblin extends Enemy implements SkillUser {

    public Goblin() {
        super("Goblin King", 70, 13, 3, 120, 600);
    }

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