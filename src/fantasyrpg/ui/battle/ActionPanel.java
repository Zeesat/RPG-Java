package fantasyrpg.ui.battle;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JPanel;

public class ActionPanel extends JPanel {
    private static final String[] ACTIONS = {"ATTACK", "SKILL", "DEFEND"};
    private static final String[] LABELS = {"ATTACK  [J]", "SKILL  [K]", "DEFEND [L]"};
    private int selectedIndex;
    private int hoverIndex = -1;
    private boolean actionsEnabled = true;
    private Consumer<String> listener;

    public ActionPanel() {
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!actionsEnabled) {
                    return;
                }
                hoverIndex = getActionIndex(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverIndex = -1;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!actionsEnabled) {
                    return;
                }
                int index = getActionIndex(e.getX(), e.getY());
                if (index >= 0) {
                    selectedIndex = index;
                    if (listener != null) {
                        listener.accept(ACTIONS[index]);
                    }
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setActionListener(Consumer<String> listener) {
        this.listener = listener;
    }

    public void setActionsEnabled(boolean actionsEnabled) {
        this.actionsEnabled = actionsEnabled;
        if (!actionsEnabled) {
            hoverIndex = -1;
        }
        setCursor(Cursor.getPredefinedCursor(actionsEnabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    public void selectAction(String action) {
        for (int i = 0; i < ACTIONS.length; i++) {
            if (ACTIONS[i].equals(action)) {
                selectedIndex = i;
                repaint();
                return;
            }
        }
    }

    private int getActionIndex(int mouseX, int mouseY) {
        int margin = 18;
        int gap = 12;
        int buttonWidth = (getWidth() - margin * 2 - gap * 2) / 3;
        int buttonHeight = getHeight() - margin * 2;

        for (int i = 0; i < ACTIONS.length; i++) {
            int x = margin + i * (buttonWidth + gap);
            int y = margin;
            if (mouseX >= x && mouseX <= x + buttonWidth && mouseY >= y && mouseY <= y + buttonHeight) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        GamePanel.drawFrame(g, 0, 0, getWidth() - 1, getHeight() - 1);

        int margin = 18;
        int gap = 12;
        int buttonWidth = (getWidth() - margin * 2 - gap * 2) / 3;
        int buttonHeight = getHeight() - margin * 2;

        for (int i = 0; i < ACTIONS.length; i++) {
            int x = margin + i * (buttonWidth + gap);
            int y = margin;
            drawButton(g, i, x, y, buttonWidth, buttonHeight);
        }
        g.dispose();
    }

    private void drawButton(Graphics2D g, int index, int x, int y, int width, int height) {
        boolean selected = actionsEnabled && index == selectedIndex;
        boolean hover = actionsEnabled && index == hoverIndex;

        g.setColor(new Color(3, 5, 8));
        g.fillRect(x - 5, y - 5, width + 10, height + 10);
        g.setColor(new Color(188, 125, 58));
        g.fillRect(x - 2, y - 2, width + 4, height + 4);
        g.setColor(selected ? new Color(22, 58, 82) : actionsEnabled ? new Color(12, 20, 27) : new Color(8, 12, 16));
        g.fillRect(x + 3, y + 3, width - 6, height - 6);
        g.setColor(selected ? new Color(111, 156, 181) : actionsEnabled ? new Color(67, 82, 86) : new Color(35, 42, 45));
        g.drawRect(x + 8, y + 8, width - 16, height - 16);

        if (hover) {
            g.setColor(new Color(255, 255, 255, 26));
            g.fillRect(x + 6, y + 6, width - 12, height - 12);
        }

        if (selected) {
            g.setColor(new Color(255, 187, 45));
            g.drawRect(x + 12, y + 12, width - 24, height - 24);
            g.drawRect(x + 15, y + 15, width - 30, height - 30);
        }

        drawCenteredText(g, LABELS[index], x + width / 2, y + height / 2 + 7, 20);
    }

    private void drawCenteredText(Graphics2D g, String text, int centerX, int baseline, int size) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
        FontMetrics metrics = g.getFontMetrics();
        int x = centerX - metrics.stringWidth(text) / 2;
        int drawY = baseline + metrics.getAscent() / 5;
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(text, x + 3, drawY + 3);
        g.setColor(new Color(255, 245, 218));
        g.drawString(text, x, drawY);
    }
}
