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
    private boolean defending = false;

    protected Character(String name, int maxHp, int attackPower, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attackPower = attackPower;
        this.defense = defense;
        this.attackMultiplier = 1.0;
        this.defenseMultiplier = 1.0;
    }

    public String getName()       { return name; }
    public int    getMaxHp()      { return maxHp; }
    public int    getHp()         { return hp; }
    public int    getAttackPower(){ return attackPower; }
    public int    getDefense()    { return defense; }
    public double getAttackMultiplier()  { return attackMultiplier; }
    public double getDefenseMultiplier() { return defenseMultiplier; }
    public boolean isDefending()  { return defending; }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(maxHp, hp));
    }
    protected void setAttackPower(int v) { this.attackPower = Math.max(1, v); }
    protected void setDefense(int v)     { this.defense = Math.max(0, v); }

    public void setAttackMultiplier(double m)  { this.attackMultiplier  = Math.max(0.5, m); }
    public void setDefenseMultiplier(double m) { this.defenseMultiplier = Math.max(0.5, m); }

    public void heal(int amount) { setHp(hp + Math.max(0, amount)); }

    public void restoreTurnModifiers() {
        attackMultiplier  = 1.0;
        defenseMultiplier = 1.0;
        defending = false;
    }

    

    public void defend() {
        defending = true;
        defenseMultiplier = Math.max(defenseMultiplier, 1.5);
    }

    public void stopDefending() {
        defending = false;
        defenseMultiplier = 1.0;
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
        int reducedDamage = calculateFinalDamage(damage);
        if (defending) {
            reducedDamage = Math.max(1, (int) (reducedDamage * 0.55));
        }
        setHp(hp - reducedDamage);
    }

    @Override
    public boolean isAlive() { return hp > 0; }

    public abstract int attack(Character target);

    public int attack(Character target, int bonusDamage) {
        int dealt = attack(target) + Math.max(0, bonusDamage);
        target.receiveDamage(bonusDamage);
        return dealt;
    }

    public int attack(Character target, String skillName) {
        int base = attack(target);
        if ("critical".equalsIgnoreCase(skillName)) {
            int bonus = Math.max(2, base / 2);
            target.receiveDamage(bonus);
            return base + bonus;
        }
        return base;
    }
}