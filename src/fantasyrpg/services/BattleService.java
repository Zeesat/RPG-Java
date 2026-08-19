package fantasyrpg.services;

import fantasyrpg.GameState;
import fantasyrpg.entities.Enemy;
import fantasyrpg.entities.Player;
import fantasyrpg.interfaces.SkillUser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class BattleService {
    private static final int ATTACK_COOLDOWN = 0;
    private static final int SKILL_COOLDOWN  = 3;
    private static final int DEFEND_COOLDOWN = 1;

    public enum PlayerAction { ATTACK, SKILL, DEFEND, BACK }
    public enum BattlePhase { PLAYER_TURN, ENEMY_TURN, ANIMATING, ENDED }

    public record BattleResult(
        String logMessage,
        int damageDealt,
        boolean battleEnded,
        boolean playerWon,
        List<EffectEvent> effects
    ) {
        public BattleResult {
            if (effects == null) effects = List.of();
        }
    }

    public record EffectEvent(String type, int floatingX, int floatingY, String floatingText, int floatingColorRGB) {}

    private final Random random = new Random();
    private final Queue<String> effectQueue = new ArrayDeque<>();

    private Player player;
    private Enemy enemy;
    private int heroHp;
    private int enemyHp;
    private int enemyMaxHp;
    private int turn;

    private int attackCooldownLeft;
    private int skillCooldownLeft;
    private int defendCooldownLeft;

    private boolean heroDefendingThisTurn;
    private boolean heroDefendedLastTurn;

    private boolean enemyEnraged;
    private BattlePhase phase;
    private boolean battleEnded;
    private boolean playerWon;

    private int maxSkillUses;
    private int skillUsesLeft;

    public BattleService() {}

    public void initBattle() {
        player = new Player("Hero");
        int defeatedCount = 0;
        if (GameState.map1Enemy1Defeated) defeatedCount++;
        if (GameState.map1Enemy2Defeated) defeatedCount++;
        if (GameState.map2Enemy1Defeated) defeatedCount++;
        if (GameState.map2Enemy2Defeated) defeatedCount++;
        for (int i = 0; i < defeatedCount; i++) {
            player.gainExperience(player.getLevel() * 50);
        }
        player.setHp(player.getMaxHp());

        enemy = createEnemyForCurrentMap();
        heroHp = player.getHp();
        enemyHp = enemy.getHp();
        enemyMaxHp = enemy.getMaxHp();

        maxSkillUses = calculateMaxSkillUses();
        player.setMaxFireballCharges(maxSkillUses);
        player.setFireballCharges(maxSkillUses);
        skillUsesLeft = maxSkillUses;

        attackCooldownLeft = 0;
        skillCooldownLeft  = 0;
        defendCooldownLeft = 0;
        heroDefendingThisTurn = false;
        heroDefendedLastTurn  = false;
        enemyEnraged = false;
        turn = 1;
        battleEnded = false;
        playerWon = false;
        phase = BattlePhase.PLAYER_TURN;
        effectQueue.clear();
    }

    public BattleResult executePlayerAction(PlayerAction action) {
        if (battleEnded || phase != BattlePhase.PLAYER_TURN) {
            return new BattleResult("", 0, battleEnded, playerWon, List.of());
        }

        return switch (action) {
            case ATTACK  -> doAttack();
            case SKILL   -> doSkill();
            case DEFEND  -> doDefend();
            case BACK    -> handleBack();
        };
    }

    public BattleResult executeEnemyTurn() {
        if (battleEnded || phase != BattlePhase.ENEMY_TURN) {
            return new BattleResult("", 0, battleEnded, playerWon, List.of());
        }

        double hpRatio = (double) enemyHp / enemyMaxHp;
        int threshold = enemy.getSkillThreshold();

        if (!enemyEnraged && threshold > 0 && hpRatio * 100 <= threshold) {
            enemyEnraged = true;
        }

        boolean useSkill;
        if (threshold == 0) {
            useSkill = false;
        } else {
            useSkill = enemyEnraged
                    ? random.nextInt(100) < 50
                    : (threshold > 0 && hpRatio * 100 <= threshold && random.nextInt(100) < 30);
        }

        if (enemyEnraged) {
            enemy.setAttackMultiplier(1.25);
        } else {
            enemy.setAttackMultiplier(1.0);
        }

        int hpBefore = player.getHp();
        if (useSkill && enemy instanceof SkillUser skillUser) {
            skillUser.useSkill(player);
        } else {
            enemy.attack(player);
        }
        int damage = hpBefore - player.getHp();
        heroHp = player.getHp();

        if (heroDefendingThisTurn) {
            heroDefendedLastTurn = true;
        } else {
            heroDefendedLastTurn = false;
        }
        heroDefendingThisTurn = false;

        String atkLabel = useSkill ? enemy.getSkillName() : enemy.getAttackName();

        List<EffectEvent> effects = new ArrayList<>();
        effects.add(new EffectEvent(useSkill ? "ENEMY_SKILL" : "ENEMY_ATTACK", 0, 0, "", 0));
        effects.add(new EffectEvent("HERO_HIT", 0, 0, "", 0));

        boolean heroDied = heroHp <= 0;
        if (heroDied) {
            battleEnded = true;
            playerWon = false;
            phase = BattlePhase.ENDED;
        }

        player.restoreTurnModifiers();
        enemy.restoreTurnModifiers();
        player.tickCooldowns();
        turn++;

        phase = heroDied ? BattlePhase.ENDED : BattlePhase.PLAYER_TURN;

        String logMsg = atkLabel + "! -" + damage + " HP." + (heroDied ? " Hero defeated. GAME OVER." : "");
        return new BattleResult(logMsg, damage, battleEnded, false, effects);
    }

    private BattleResult doAttack() {
        if (attackCooldownLeft > 0) {
            return new BattleResult("Attack on cooldown! (" + attackCooldownLeft + " turn left)", 0, false, false, List.of());
        }

        int hpBefore = enemy.getHp();
        player.attack(enemy);
        int damage = hpBefore - enemy.getHp();
        enemyHp = enemy.getHp();

        List<EffectEvent> effects = new ArrayList<>();
        effects.add(new EffectEvent("HERO_ATTACK", 0, 0, "", 0));
        effects.add(new EffectEvent("ENEMY_HIT", 0, 0, "", 0));

        heroDefendedLastTurn = false;
        heroDefendingThisTurn = false;
        attackCooldownLeft = ATTACK_COOLDOWN;
        if (skillCooldownLeft  > 0) skillCooldownLeft--;
        if (defendCooldownLeft > 0) defendCooldownLeft--;

        if (enemyHp == 0) {
            return handleEnemyDefeat("Hero attacks: -" + damage + " HP.", effects);
        }

        phase = BattlePhase.ENEMY_TURN;
        return new BattleResult("Hero attacks: -" + damage + " HP.", damage, false, false, effects);
    }

    private BattleResult doSkill() {
        if (skillCooldownLeft > 0) {
            return new BattleResult("Skill on cooldown! (" + skillCooldownLeft + " turns left)", 0, false, false, List.of());
        }
        if (player.getFireballCharges() <= 0) {
            return new BattleResult("Skill sudah habis untuk battle ini!", 0, false, false, List.of());
        }

        int hpBefore = enemy.getHp();
        player.useSkill(enemy);
        int damage = hpBefore - enemy.getHp();
        enemyHp = enemy.getHp();

        List<EffectEvent> effects = new ArrayList<>();
        effects.add(new EffectEvent("HERO_SKILL", 0, 0, "", 0));
        effects.add(new EffectEvent("ENEMY_HIT", 0, 0, "", 0));

        heroDefendedLastTurn = false;
        heroDefendingThisTurn = false;
        skillCooldownLeft = SKILL_COOLDOWN;
        if (attackCooldownLeft > 0) attackCooldownLeft--;
        if (defendCooldownLeft > 0) defendCooldownLeft--;

        if (enemyHp == 0) {
            return handleEnemyDefeat("Fireball! -" + damage + " HP. (Sisa: " + player.getFireballCharges() + ")", effects);
        }

        phase = BattlePhase.ENEMY_TURN;
        return new BattleResult("Fireball! -" + damage + " HP. (Sisa: " + player.getFireballCharges() + ")", damage, false, false, effects);
    }

    private BattleResult doDefend() {
        if (defendCooldownLeft > 0) {
            return new BattleResult("Can't defend twice in a row!", 0, false, false, List.of());
        }

        heroDefendingThisTurn = true;
        heroDefendedLastTurn  = false;
        player.defend();

        List<EffectEvent> effects = new ArrayList<>();
        effects.add(new EffectEvent("HERO_DEFEND", 0, 0, "", 0));

        defendCooldownLeft = DEFEND_COOLDOWN;
        if (attackCooldownLeft > 0) attackCooldownLeft--;
        if (skillCooldownLeft  > 0) skillCooldownLeft--;

        phase = BattlePhase.ENEMY_TURN;
        return new BattleResult("Hero bertahan! Damage akan dikurangi.", 0, false, false, effects);
    }

    private BattleResult handleBack() {
        if (battleEnded) {
            if (enemyHp == 0) {
                applyDefeatState();
            }
        }
        return new BattleResult("", 0, battleEnded, playerWon, List.of());
    }

    private BattleResult handleEnemyDefeat(String prefixLog, List<EffectEvent> effects) {
        battleEnded = true;
        playerWon = true;
        phase = BattlePhase.ENDED;
        applyDefeatState();

        return new BattleResult(
            prefixLog + "\n" + enemy.getName() + " defeated! Victory!",
            0, true, true, effects
        );
    }

    private void applyDefeatState() {
        boolean isMap1 = GameState.currentMapPath.endsWith("maps.tmx");
        if (isMap1) {
            if (GameState.currentEnemyIndex == 0) GameState.map1Enemy1Defeated = true;
            if (GameState.currentEnemyIndex == 1) GameState.map1Enemy2Defeated = true;
        } else {
            if (GameState.currentEnemyIndex == 0) GameState.map2Enemy1Defeated = true;
            if (GameState.currentEnemyIndex == 1) GameState.map2Enemy2Defeated = true;
            if (GameState.currentEnemyIndex == 2) GameState.map2Enemy3Defeated = true;
        }
    }

    private Enemy createEnemyForCurrentMap() {
        boolean isMap1 = GameState.currentMapPath.endsWith("maps.tmx");
        int idx = GameState.currentEnemyIndex;
        if (isMap1) {
            return idx == 1 ? new fantasyrpg.entities.Golem() : new fantasyrpg.entities.Slime();
        }
        return switch (idx) {
            case 0 -> new fantasyrpg.entities.Goblin();
            case 1 -> new fantasyrpg.entities.OrcWarrior();
            case 2 -> new fantasyrpg.entities.DragonBoss();
            default -> new fantasyrpg.entities.Goblin();
        };
    }

    private int calculateMaxSkillUses() {
        String type = getEnemyTypeKey();
        return switch (type) {
            case "GOLEM" -> 2;
            case "GOBLIN" -> 3;
            case "ORC" -> 4;
            case "DRAGON" -> 5;
            default -> 1;
        };
    }

    public String getEnemyTypeKey() {
        boolean isMap1 = GameState.currentMapPath.endsWith("maps.tmx");
        int idx = GameState.currentEnemyIndex;
        if (isMap1) return idx == 1 ? "GOLEM" : "SLIME";
        return switch (idx) { case 0 -> "GOBLIN"; case 1 -> "ORC"; case 2 -> "DRAGON"; default -> "GOBLIN"; };
    }

    public boolean isDungeon() {
        return !GameState.currentMapPath.endsWith("maps.tmx");
    }

    public Player getPlayer() { return player; }
    public Enemy getEnemy() { return enemy; }
    public int getHeroHp() { return heroHp; }
    public int getEnemyHp() { return enemyHp; }
    public int getEnemyMaxHp() { return enemyMaxHp; }
    public int getTurn() { return turn; }
    public boolean isBattleEnded() { return battleEnded; }
    public boolean isPlayerWon() { return playerWon; }
    public boolean isEnemyEnraged() { return enemyEnraged; }
    public boolean isHeroDefendingThisTurn() { return heroDefendingThisTurn; }
    public boolean isHeroDefendedLastTurn() { return heroDefendedLastTurn; }
    public int getAttackCooldownLeft() { return attackCooldownLeft; }
    public int getSkillCooldownLeft() { return skillCooldownLeft; }
    public int getDefendCooldownLeft() { return defendCooldownLeft; }
    public BattlePhase getPhase() { return phase; }
}
