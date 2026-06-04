package fantasyrpg.ui.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.HashMap;

public final class EnemySpawnConfig {

    private static final HashMap<String, MarkerStyle> MARKER_STYLES =
            new HashMap<>();

    private static BufferedImage slimeIcon;
    private static BufferedImage golemIcon;
    private static BufferedImage goblinIcon;
    private static BufferedImage orcIcon;
    private static BufferedImage dragonIcon;

    static {
        try {
            slimeIcon = ImageIO.read(new File("assets/icons/slime_icon.png"));
        } catch (Exception e) {
            System.err.println("Failed to load assets/icons/slime_icon.png: " + e.getMessage());
        }
        try {
            golemIcon = ImageIO.read(new File("assets/icons/golem_icon.png"));
        } catch (Exception e) {
            System.err.println("Failed to load assets/icons/golem_icon.png: " + e.getMessage());
        }
        try {
            goblinIcon = ImageIO.read(new File("assets/icons/goblin_icon.png"));
        } catch (Exception e) {
            System.err.println("Failed to load assets/icons/goblin_icon.png: " + e.getMessage());
        }
        try {
            orcIcon = ImageIO.read(new File("assets/icons/orcwarrior_icon.png"));
        } catch (Exception e) {
            System.err.println("Failed to load assets/icons/orcwarrior_icon.png: " + e.getMessage());
        }
        try {
            dragonIcon = ImageIO.read(new File("assets/icons/dragonboss_icon.png"));
        } catch (Exception e) {
            System.err.println("Failed to load assets/icons/dragonboss_icon.png: " + e.getMessage());
        }

        
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

        String enemyId = normalizeEnemyId(spawnPoint.getEnemyId());
        BufferedImage img = null;
        if ("golem".equals(enemyId)) {
            img = golemIcon;
        } else if ("slime".equals(enemyId)) {
            img = slimeIcon;
        } else if ("goblin".equals(enemyId)) {
            img = goblinIcon;
        } else if ("orc_warrior".equals(enemyId)) {
            img = orcIcon;
        } else if ("dragonboss".equals(enemyId)) {
            img = dragonIcon;
        }

        if (img != null) {
            int x = spawnPoint.getX();
            int y = spawnPoint.getY();
            if ("golem".equals(enemyId)) {
                g2.drawImage(img, x - 32, y - 32, 64, 64, null);
            } else if ("dragonboss".equals(enemyId)) {
                g2.drawImage(img, x - 48, y - 48, 96, 96, null);
            } else {
                g2.drawImage(img, x - 24, y - 24, 48, 48, null);
            }
        } else {
            MarkerStyle style =
                    resolveStyle(spawnPoint.getEnemyId());

            int x = spawnPoint.getX();
            int y = spawnPoint.getY();

            
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
        }
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
        String norm = normalizeEnemyId(enemyId);
        if (norm.contains("orc")) {
            return MARKER_STYLES.get("orc");
        }
        if (norm.contains("dragon") || norm.contains("boss")) {
            return MARKER_STYLES.get("boss");
        }

        MarkerStyle style =
                MARKER_STYLES.get(norm);

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
