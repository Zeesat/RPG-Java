package fantasyrpg.entities;

public class OrcWarrior extends Enemy {

    public OrcWarrior() {
        super("Orc Warrior", 220, 13, 8, 55, 250, 9);
    }

    @Override
    public int getSkillThreshold() { return 0; }

    @Override
    public String getAttackName() { return "Axe Swing"; }

    @Override
    public int attack(Character target) {
        int damage = calculateBaseAttack() + 4;
        target.receiveDamage(damage);
        return damage;
    }
}
