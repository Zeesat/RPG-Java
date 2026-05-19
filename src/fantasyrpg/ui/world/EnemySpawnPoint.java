package fantasyrpg.ui.world;

public class EnemySpawnPoint {

    private final int x;
    private final int y;
    private final String enemyId;

    public EnemySpawnPoint(
            int x,
            int y,
            String enemyId
    ) {

        this.x = x;
        this.y = y;
        this.enemyId =
                (enemyId == null || enemyId.isBlank())
                        ? "default"
                        : enemyId.trim();
    }

    public int getX() {

        return x;
    }

    public int getY() {

        return y;
    }

    public String getEnemyId() {

        return enemyId;
    }
}
