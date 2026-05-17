package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;
import java.util.Random;

public class Player extends Character implements SkillUser {
    private final Random random = new Random();
    private static final int MAX_FIREBALL_CHARGES = 3;

    private int level;
    private int experience;
    private int score;
    private int potionCount;
    private int fireballCharges;

    public Player(String name) {
        super(name, 150, 15, 5);

        this.level = 1;
        this.experience = 0;
        this.score = 0;
        this.potionCount = 4;
        this.fireballCharges = MAX_FIREBALL_CHARGES;
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

    public int getFireballCharges() {
        return fireballCharges;
    }

    public int getMaxFireballCharges() {
        return MAX_FIREBALL_CHARGES;
    }

    public void resetFireballCharges() {
        fireballCharges = MAX_FIREBALL_CHARGES;
    }

    public void setPotionCount(int count) {
        potionCount = Math.max(0, count);
    }

    // FIX untuk stage reward
    public void addPotion(int amount) {
        potionCount += Math.max(0, amount);
    }

    public boolean usePotion() {
        if (potionCount <= 0) {
            return false;
        }

        potionCount--;
        heal(30 + (level * 5));
        return true;
    }

    public boolean canUseFireball() {
        return fireballCharges > 0;
    }

    public void gainExperience(int amount) {
        experience += Math.max(0, amount);

        while (experience >= requiredExperience()) {
            experience -= requiredExperience();
            levelUp();
        }
    }

    public void addScore(int amount) {
        score += Math.max(0, amount);
    }

    private void levelUp() {
        level++;
        setAttackPower(getAttackPower() + 2);
        setDefense(getDefense() + 1);
        heal(25);
    }

    private int requiredExperience() {
        return level * 50;
    }

    @Override
    public int attack(Character target) {
        int variance = random.nextInt(5) - 2;
        int damage = Math.max(1, calculateBaseAttack() + (level * 2) + variance);
        target.receiveDamage(damage);
        return damage;
    }

    @Override
    public int useSkill(Character target) {
        if (fireballCharges <= 0) {
            return 0;
        }

        fireballCharges--;

        int damage = calculateBaseAttack() + (level * 4) + 8;
        target.receiveDamage(damage);
        return damage;
    }
}
