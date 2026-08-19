package fantasyrpg.entities;

import fantasyrpg.interfaces.SkillUser;

public abstract class Enemy extends Character {
    private final int rewardExperience;
    private final int rewardScore;
    private final int level;

    protected Enemy(
            String name,
            int maxHp,
            int attackPower,
            int defense,
            int rewardExperience,
            int rewardScore,
            int level
    ) {
        super(name, maxHp, attackPower, defense);
        this.rewardExperience = rewardExperience;
        this.rewardScore = rewardScore;
        this.level = level;
    }

    public int getRewardExperience() {
        return rewardExperience;
    }

    public int getRewardScore() {
        return rewardScore;
    }

    public int getLevel() {
        return level;
    }

    public abstract int getSkillThreshold();

    public abstract String getAttackName();

    public String getSkillName() {
        return "Special Attack";
    }

    public boolean hasSkill() {
        return this instanceof SkillUser;
    }
}