package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;














public class DragonBoss extends Enemy implements SkillUser {

    public DragonBoss() {
        super("Azhrax the Ash Dragon", 160, 15, 8, 180, 1200);
    }

    @Override
    public int attack(Character target) {
        int damage = calculateBaseAttack() + 5;
        target.receiveDamage(damage);
        return damage;
    }

    


    @Override
    public int useSkill(Character target) {
        int damage = calculateBaseAttack() + 12;
        target.receiveDamage(damage);
        return damage;
    }
}