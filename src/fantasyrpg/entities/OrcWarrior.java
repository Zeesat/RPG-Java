package fantasyrpg.entities;

public class OrcWarrior extends Enemy {
    public OrcWarrior() {
        super("Orc Warrior", 90, 16, 5, 40, 175);
    }

    @Override
    public int attack(Character target) {
        int damage = calculateBaseAttack() + 5;
        target.receiveDamage(damage);
        return damage;
    }
}

