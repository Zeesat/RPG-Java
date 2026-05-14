package fantasyrpg.entities;

public class Goblin extends Enemy {

    public Goblin() {
        super(
                "Goblin Raider",
                70,
                8,
                2,
                25,
                100
        );
    }

    @Override
    public int attack(Character target) {
        int damage = 10;
        target.receiveDamage(damage);
        return damage;
    }
}