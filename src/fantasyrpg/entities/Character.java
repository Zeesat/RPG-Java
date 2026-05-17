package fantasyrpg.entities;

import fantasyrpg.interfaces.Attackable;

public abstract class Character implements Attackable {
    private final String name;
    private final int maxHp;

    private int hp;
    private int attackPower;
    private int defense;

    private boolean defending = false;

    protected Character(String name, int maxHp, int attackPower, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attackPower = attackPower;
        this.defense = defense;
    }

    public String getName() {
        return name;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(maxHp, hp));
    }

    public int getAttackPower() {
        return attackPower;
    }

    protected void setAttackPower(int attackPower) {
        this.attackPower = Math.max(1, attackPower);
    }

    public int getDefense() {
        return defense;
    }

    protected void setDefense(int defense) {
        this.defense = Math.max(0, defense);
    }

    public void heal(int amount) {
        setHp(hp + Math.max(0, amount));
    }

    public void defend() {
        defending = true;
    }

    public void stopDefending() {
        defending = false;
    }

    public boolean isDefending() {
        return defending;
    }

    @Override
    public void receiveDamage(int damage) {
        int reducedDamage = damage - defense;

        if (defending) {
            reducedDamage = reducedDamage / 2;
        }

        reducedDamage = Math.max(1, reducedDamage);

        setHp(hp - reducedDamage);
    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }

    public abstract int attack(Character target);
}