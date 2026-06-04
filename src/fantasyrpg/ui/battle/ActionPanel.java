package fantasyrpg.ui.battle;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JPanel;

public class ActionPanel extends JPanel {
    private static final String[] ACTIONS = {"ATTACK", "SKILL", "DEFEND"};
    private static final String[] LABELS  = {"ATTACK  [J]", "SKILL   [K]", "DEFEND  [L]"};
    private static final String[] SUBLABELS = {"Basic strike", "MP: 20  CD:3", "Reduce dmg  CD:1"};

    
    private static final Color[] ACCENT = {
            new Color(220, 100, 40),   
            new Color(130, 60, 220),   
            new Color(50, 140, 220),   
    };

    private int selectedIndex = 0;
    private int hoverIndex    = -1;
    private boolean actionsEnabled = true;
    private Consumer<String> listener;

    public ActionPanel() {
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                if (!actionsEnabled) return;
                hoverIndex = getActionIndex(e.getX(), e.getY());
                repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                hoverIndex = -1; repaint();
            }
            @Override public void mousePressed(MouseEvent e) {
                if (!actionsEnabled) return;
                int idx = getActionIndex(e.getX(), e.getY());
                if (idx >= 0) {
                    selectedIndex = idx;
                    if (listener != null) listener.accept(ACTIONS[idx]);
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setActionListener(Consumer<String> listener) { this.listener = listener; }

    public void setActionsEnabled(boolean enabled) {
        this.actionsEnabled = enabled;
        if (!enabled) hoverIndex = -1;
        setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    public void selectAction(String action) {
        for (int i = 0; i < ACTIONS.length; i++) {
            if (ACTIONS[i].equals(action)) { selectedIndex = i; repaint(); return; }
        }
    }

    private int getActionIndex(int mx, int my) {
        int margin = 14, gap = 10;
        int bw = (getWidth() - margin * 2 - gap * 2) / 3;
        int bh = getHeight() - margin * 2;
        for (int i = 0; i < ACTIONS.length; i++) {
            int x = margin + i * (bw + gap);
            if (mx >= x && mx <= x + bw && my >= margin && my <= margin + bh) return i;
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GamePanel.drawFrame(g, 0, 0, getWidth() - 1, getHeight() - 1);

        int margin = 14, gap = 10;
        int bw = (getWidth() - margin * 2 - gap * 2) / 3;
        int bh = getHeight() - margin * 2;

        for (int i = 0; i < ACTIONS.length; i++) {
            drawButton(g, i, margin + i * (bw + gap), margin, bw, bh);
        }
        g.dispose();
    }

    private void drawButton(Graphics2D g, int idx, int x, int y, int w, int h) {
        boolean sel   = actionsEnabled && idx == selectedIndex;
        boolean hover = actionsEnabled && idx == hoverIndex;
        boolean dis   = !actionsEnabled;

        Color accent = ACCENT[idx];
        Color darkAccent = accent.darker().darker();

        
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x - 2, y - 2, w + 6, h + 6, 10, 10);

        
        g.setColor(sel ? accent : dis ? new Color(40, 40, 40) : darkAccent);
        g.fillRoundRect(x, y, w + 2, h + 2, 8, 8);

        
        Color bgTop = sel ? new Color(accent.getRed()/3, accent.getGreen()/3, accent.getBlue()/3, 200)
                : dis ? new Color(12, 15, 18)
                : hover ? new Color(20, 28, 36)
                : new Color(12, 18, 26);
        g.setColor(bgTop);
        g.fillRoundRect(x + 2, y + 2, w - 2, h - 2, 6, 6);

        
        if (!dis) {
            GradientPaint shine = new GradientPaint(
                    x, y + 2, new Color(255, 255, 255, sel ? 18 : hover ? 12 : 6),
                    x, y + h / 2, new Color(255, 255, 255, 0));
            g.setPaint(shine);
            g.fillRoundRect(x + 2, y + 2, w - 2, h / 2, 6, 6);
        }

        
        g.setColor(sel ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200)
                : dis ? new Color(30, 35, 38) : new Color(50, 65, 75));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawRoundRect(x + 4, y + 4, w - 6, h - 6, 6, 6);

        
        int mainSize = 18;
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, mainSize));
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (w - fm.stringWidth(LABELS[idx])) / 2;
        int ty = y + h / 2;

        Color textColor = dis ? new Color(60, 65, 70)
                : sel ? new Color(255, 240, 200)
                : hover ? new Color(230, 210, 180)
                : new Color(200, 185, 155);

        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(LABELS[idx], tx + 2, ty + 2);
        g.setColor(textColor);
        g.drawString(LABELS[idx], tx, ty);

        
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        fm = g.getFontMetrics();
        int sx = x + (w - fm.stringWidth(SUBLABELS[idx])) / 2;
        int sy = y + h / 2 + 18;
        g.setColor(dis ? new Color(40, 45, 50) : new Color(140, 130, 110));
        g.drawString(SUBLABELS[idx], sx, sy);

        
        if (sel) {
            g.setColor(accent);
            g.fillRoundRect(x + 8, y + h - 10, w - 14, 5, 3, 3);
        }
    }
}