package fantasyrpg.ui.battle;

import java.awt.Color;
import java.awt.Graphics2D;

public final class HealthBar {
    private HealthBar() {
    }

    public static void draw(Graphics2D g, int x, int y, int width, int height, int value, int max, Color color) {
        int safeMax = Math.max(1, max);
        int safeValue = Math.max(0, Math.min(value, safeMax));
        int fillWidth = (int) Math.round((width - 6) * (safeValue / (double) safeMax));

        g.setColor(new Color(9, 11, 12));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(82, 91, 88));
        g.drawRect(x, y, width, height);

        g.setColor(color.darker().darker());
        g.fillRect(x + 3, y + 3, width - 6, height - 6);
        g.setColor(color);
        g.fillRect(x + 3, y + 3, fillWidth, height - 6);
        g.setColor(new Color(255, 255, 255, 75));
        g.fillRect(x + 4, y + 4, Math.max(0, fillWidth - 2), Math.max(1, height / 4));
    }
}
