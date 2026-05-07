package fantasyrpg.entities;

public abstract class Enemy extends Character {
    private final int rewardExperience;
    private final int rewardScore;

    protected Enemy(String name, int maxHp, int attackPower, int defense, int rewardExperience, int rewardScore) {
        super(name, maxHp, attackPower, defense);
        this.rewardExperience = rewardExperience;
        this.rewardScore = rewardScore;
    }

    public int getRewardExperience() {
        return rewardExperience;
    }

    public int getRewardScore() {
        return rewardScore;
    }
}

