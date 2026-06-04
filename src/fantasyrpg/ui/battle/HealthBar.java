package fantasyrpg.ui.battle;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class HealthBar {
    private HealthBar() {}

    





    public static void draw(
            Graphics2D g, int x, int y, int width, int height,
            int value, int max, Color color) {

        int safeMax   = Math.max(1, max);
        int safeValue = Math.max(0, Math.min(value, safeMax));
        int fillW     = (int) Math.round((width - 6) * (safeValue / (double) safeMax));

        
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(x - 1, y - 1, width + 2, height + 2, 6, 6);

        
        g.setColor(new Color(9, 11, 14));
        g.fillRoundRect(x, y, width, height, 5, 5);
        g.setColor(new Color(55, 60, 65));
        g.drawRoundRect(x, y, width, height, 5, 5);

        if (fillW > 0) {
            
            g.setColor(color.darker().darker());
            g.fillRoundRect(x + 3, y + 3, fillW, height - 6, 3, 3);

            
            GradientPaint gp = new GradientPaint(
                    x + 3, y + 3, color,
                    x + 3, y + height - 3, color.darker());
            g.setPaint(gp);
            g.fillRoundRect(x + 3, y + 3, fillW, height - 6, 3, 3);

            
            g.setColor(new Color(255, 255, 255, 55));
            int shineH = Math.max(2, height / 4);
            g.fillRoundRect(x + 4, y + 3, Math.max(0, fillW - 2), shineH, 2, 2);
        }

        
        g.setColor(new Color(0, 0, 0, 90));
        for (int seg = 1; seg < 4; seg++) {
            int tx = x + 3 + (int)((width - 6) * seg / 4.0);
            g.fillRect(tx - 1, y + 2, 2, height - 4);
        }

        
        double ratio = safeValue / (double) safeMax;
        if (ratio < 0.25 && height >= 12) {
            long t = System.currentTimeMillis();
            float pulse = (float)(0.5 + 0.5 * Math.sin(t / 200.0));
            g.setColor(new Color(255, 50, 50, (int)(160 * pulse)));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawRoundRect(x, y, width, height, 5, 5);
        }
    }
}