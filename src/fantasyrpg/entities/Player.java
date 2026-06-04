package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;
import java.util.Random;














public class Player extends Character implements SkillUser {

    private final Random random = new Random();

    
    private int maxFireballCharges = 2;
    private static final int SKILL_COOLDOWN_TURNS  = 3;
    private int fireballCharges;
    private int skillCooldown;          

    
    private static final int POTION_COOLDOWN_TURNS = 3;
    private int potionCount;
    private int potionCooldown;         

    
    private int level;
    private int experience;
    private int score;

    
    public Player(String name) {
        super(name, 150, 25, 6);   
        this.level          = 1;
        this.experience     = 0;
        this.score          = 0;
        this.potionCount    = 3;
        this.fireballCharges = 2;
        this.skillCooldown  = 0;
        this.potionCooldown = 0;
    }

    
    public int  getLevel()              { return level; }
    public int  getExperience()         { return experience; }
    public int  getScore()              { return score; }
    public int  getPotionCount()        { return potionCount; }
    public int  getFireballCharges()    { return fireballCharges; }
    public int  getMaxFireballCharges() { return maxFireballCharges; }
    public int  getSkillCooldown()      { return skillCooldown; }
    public int  getPotionCooldown()     { return potionCooldown; }

    public void setFireballCharges(int charges) {
        this.fireballCharges = charges;
    }

    public void setMaxFireballCharges(int max) {
        this.maxFireballCharges = max;
    }

    
    



    public void tickCooldowns() {
        if (skillCooldown  > 0) skillCooldown--;
        if (potionCooldown > 0) potionCooldown--;
    }

    public void resetFireballCharges() {
        fireballCharges = maxFireballCharges;
    }

    
    public boolean canUseFireball() {
        return fireballCharges > 0 && skillCooldown == 0;
    }

    
    public boolean canUsePotion() {
        return potionCount > 0 && potionCooldown == 0;
    }

    
    public void setPotionCount(int count) { potionCount = Math.max(0, count); }
    public void addPotion(int amount)     { potionCount += Math.max(0, amount); }

    
    





    public boolean usePotion() {
        if (!canUsePotion()) return false;
        potionCount--;
        potionCooldown = POTION_COOLDOWN_TURNS;
        heal(22 + (level * 4));
        return true;
    }

    
    public void gainExperience(int amount) {
        experience += Math.max(0, amount);
        while (experience >= requiredExperience()) {
            experience -= requiredExperience();
            levelUp();
        }
    }
    public void addScore(int amount) { score += Math.max(0, amount); }

    private void levelUp() {
        level++;
        setAttackPower(getAttackPower() + 3);
        setDefense(getDefense() + 1);
        heal(20);
    }
    private int requiredExperience() { return level * 50; }

    
    



    @Override
    public int attack(Character target) {
        int variance = random.nextInt(7) - 3;        
        int damage   = Math.max(1, calculateBaseAttack() + (level * 2) + variance);
        target.receiveDamage(damage);
        return damage;
    }

    





    @Override
    public int useSkill(Character target) {
        if (!canUseFireball()) return 0;
        fireballCharges--;
        skillCooldown = SKILL_COOLDOWN_TURNS;
        int damage = calculateBaseAttack() + (level * 5) + 10;
        target.receiveDamage(damage);
        return damage;
    }
}