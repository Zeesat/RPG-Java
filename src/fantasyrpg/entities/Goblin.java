package fantasyrpg.entities;

public class Goblin extends Enemy {
    public Goblin() {
        super("Goblin Raider", 60, 12, 3, 25, 100);
    }

    @Override
    public int attack(Character target) {
        int damage = calculateBaseAttack() + 3;
        target.receiveDamage(damage);
        return damage;
    }
}

