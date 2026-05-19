package fantasyrpg.ui.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;

public final class EnemySpawnConfig {

    private static final HashMap<String, MarkerStyle> MARKER_STYLES =
            new HashMap<>();

    static {
        // Central place to configure enemy placeholder markers by enemyId.
        registerStyle("default", new MarkerStyle(
                new Color(255, 89, 89),
                new Color(255, 205, 205),
                7,
                11
        ));

        registerStyle("goblin", new MarkerStyle(
                new Color(91, 201, 113),
                new Color(211, 247, 218),
                7,
                11
        ));

        registerStyle("orc", new MarkerStyle(
                new Color(255, 153, 51),
                new Color(255, 224, 184),
                7,
                11
        ));

        registerStyle("boss", new MarkerStyle(
                new Color(246, 80, 100),
                new Color(255, 219, 224),
                8,
                13
        ));
    }

    private EnemySpawnConfig() {
    }

    public static void drawPlaceholderMarker(
            Graphics2D g2,
            EnemySpawnPoint spawnPoint
    ) {

        MarkerStyle style =
                resolveStyle(spawnPoint.getEnemyId());

        int x = spawnPoint.getX();
        int y = spawnPoint.getY();

        // Current placeholder rendering (no asset yet): filled oval + ring.
        g2.setColor(style.fillColor);
        g2.fillOval(
                x - style.fillRadius,
                y - style.fillRadius,
                style.fillRadius * 2,
                style.fillRadius * 2
        );

        g2.setColor(style.ringColor);
        g2.drawOval(
                x - style.ringRadius,
                y - style.ringRadius,
                style.ringRadius * 2,
                style.ringRadius * 2
        );

        /*
         * Example when sprite assets are ready:
         * BufferedImage icon = enemySpawnIcons.get(spawnPoint.getEnemyId());
         * if (icon != null) {
         *     g2.drawImage(icon, x - 16, y - 16, 32, 32, null);
         * }
         */
    }

    public static String normalizeEnemyId(String enemyId) {

        if (enemyId == null || enemyId.isBlank()) {
            return "default";
        }

        return enemyId.trim().toLowerCase();
    }

    private static void registerStyle(
            String enemyId,
            MarkerStyle style
    ) {

        MARKER_STYLES.put(
                normalizeEnemyId(enemyId),
                style
        );
    }

    private static MarkerStyle resolveStyle(String enemyId) {

        MarkerStyle style =
                MARKER_STYLES.get(
                        normalizeEnemyId(enemyId)
                );

        if (style != null) {
            return style;
        }

        return MARKER_STYLES.get("default");
    }

    private static final class MarkerStyle {

        private final Color fillColor;
        private final Color ringColor;
        private final int fillRadius;
        private final int ringRadius;

        private MarkerStyle(
                Color fillColor,
                Color ringColor,
                int fillRadius,
                int ringRadius
        ) {

            this.fillColor = fillColor;
            this.ringColor = ringColor;
            this.fillRadius = fillRadius;
            this.ringRadius = ringRadius;
        }
    }
}
