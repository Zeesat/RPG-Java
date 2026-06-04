package fantasyrpg.entities;










public class OrcWarrior extends Enemy {

    public OrcWarrior() {
        super("Orc Warrior", 220, 13, 8, 55, 250);
    }

    @Override
    public int attack(Character target) {
        int damage = calculateBaseAttack() + 4;
        target.receiveDamage(damage);
        return damage;
    }
}