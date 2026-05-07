package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;

public class Player extends Character implements SkillUser {
    private int level;
    private int experience;
    private int score;
    private int potionCount;

    public Player(String name) {
        super(name, 120, 18, 6);
        this.level = 1;
        this.experience = 0;
        this.score = 0;
        this.potionCount = 3;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getScore() {
        return score;
    }

    public int getPotionCount() {
        return potionCount;
    }

    public void addScore(int score) {
        this.score += Math.max(0, score);
    }

    public void gainExperience(int amount) {
        experience += Math.max(0, amount);
        while (experience >= requiredExperience()) {
            experience -= requiredExperience();
            levelUp();
        }
    }

    public boolean usePotion() {
        if (potionCount <= 0) {
            return false;
        }
        potionCount--;
        heal(30 + (level * 5));
        return true;
    }

    public void addPotion(int amount) {
        potionCount += Math.max(0, amount);
    }

    private int requiredExperience() {
        return level * 40;
    }

    private void levelUp() {
        level++;
        setAttackPower(getAttackPower() + 4);
        setDefense(getDefense() + 2);
        heal(25);
    }

    @Override
    public int attack(Character target) {
        int damage = calculateBaseAttack() + (level * 2);
        target.receiveDamage(damage);
        return damage;
    }

    @Override
    public int useSkill(Character target) {
        int damage = calculateBaseAttack() + (level * 4) + 8;
        target.receiveDamage(damage);
        return damage;
    }
}

