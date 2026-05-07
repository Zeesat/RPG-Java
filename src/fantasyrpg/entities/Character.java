package fantasyrpg.entities;

import fantasyrpg.interfaces.Attackable;

public abstract class Character implements Attackable {
    private final String name;
    private final int maxHp;
    private int hp;
    private int attackPower;
    private int defense;
    private double attackMultiplier;
    private double defenseMultiplier;

    protected Character(String name, int maxHp, int attackPower, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attackPower = attackPower;
        this.defense = defense;
        this.attackMultiplier = 1.0;
        this.defenseMultiplier = 1.0;
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

    public double getAttackMultiplier() {
        return attackMultiplier;
    }

    public void setAttackMultiplier(double attackMultiplier) {
        this.attackMultiplier = Math.max(0.5, attackMultiplier);
    }

    public double getDefenseMultiplier() {
        return defenseMultiplier;
    }

    public void setDefenseMultiplier(double defenseMultiplier) {
        this.defenseMultiplier = Math.max(0.5, defenseMultiplier);
    }

    public void heal(int amount) {
        setHp(hp + Math.max(0, amount));
    }

    public void restoreTurnModifiers() {
        attackMultiplier = 1.0;
        defenseMultiplier = 1.0;
    }

    public void defend() {
        defenseMultiplier = 1.5;
    }

    protected int calculateFinalDamage(int rawDamage) {
        int reduced = rawDamage - (int) Math.round(defense * defenseMultiplier);
        return Math.max(1, reduced);
    }

    protected int calculateBaseAttack() {
        return (int) Math.round(attackPower * attackMultiplier);
    }

    @Override
    public void receiveDamage(int damage) {
        int finalDamage = calculateFinalDamage(damage);
        setHp(hp - finalDamage);
    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }

    public abstract int attack(Character target);

    public int attack(Character target, int bonusDamage) {
        int dealtDamage = attack(target) + Math.max(0, bonusDamage);
        target.receiveDamage(dealtDamage);
        return dealtDamage;
    }

    public int attack(Character target, String skillName) {
        int baseDamage = attack(target);
        if ("critical".equalsIgnoreCase(skillName)) {
            int bonusDamage = Math.max(2, baseDamage / 2);
            target.receiveDamage(bonusDamage);
            return baseDamage + bonusDamage;
        }
        return baseDamage;
    }
}

