package view.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

// Move guide shown on the right of the bottom section: the targeted enemy's name, then one row per move (icon + move name).
// Moves the player has not seen yet are shown as "?"
public class MoveGuidePanel extends JPanel {
    // One row of the guide
    public static final class Entry {
        private final Image icon;
        private final String text;
        private Image scaled;
        private int scaledSize = -1;

        public Entry(Image icon, String text) {
            this.icon = icon;
            this.text = text;
        }

        private Image iconAt(int size) {
            if (icon == null || size <= 0) return null;
            if (scaled == null || scaledSize != size) {
                scaled = SpriteScaler.fit(icon, size, size).getImage();
                scaledSize = size;
            }
            return scaled;
        }
    }

    private String title = "";
    private List<Entry> entries = new ArrayList<>();

    public MoveGuidePanel() {
        setOpaque(false);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    }

    public void setGuide(String title, List<Entry> entries) {
        this.title = title == null ? "" : title;
        this.entries = new ArrayList<>(entries);
        repaint();
    }

    public void clearGuide() {
        setGuide("", new ArrayList<>());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int pad = Math.max(6, h / 30);

        // Dark blue backdrop
        g2.setColor(new Color(14, 24, 64, 220));
        g2.fillRoundRect(0, 0, w, h, 14, 14);

        // Enemy name
        int y = pad;
        if (!title.isEmpty()) {
            int titleH = Math.max(22, (int) (h * 0.13));
            g2.setColor(new Color(255, 226, 140));
            drawFitted(g2, title, Font.BOLD, titleH * 0.6f, pad, y, w - 2 * pad, titleH);
            y += titleH;
            g2.setColor(new Color(255, 226, 140, 120));
            g2.drawLine(pad, y, w - pad, y);
            y += pad;
        }

        // One row per move
        int gap = Math.max(4, pad / 2);
        int rows = Math.max(1, entries.size());
        int rowH = Math.min((int) (h * 0.2), (h - y - pad - gap * (rows - 1)) / rows);
        for (Entry e : entries) {
            g2.setColor(new Color(48, 82, 160, 210));
            g2.fillRoundRect(pad, y, w - 2 * pad, rowH, 12, 12);
            g2.setColor(new Color(150, 185, 255, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(pad, y, w - 2 * pad, rowH, 12, 12);

            int iconSize = rowH - 8;
            Image icon = e.iconAt(iconSize);
            if (icon != null) g2.drawImage(icon, pad + 6, y + 4, null);

            int textX = pad + 6 + iconSize + pad;
            g2.setColor(Color.WHITE);
            drawFitted(g2, e.text, Font.BOLD, rowH * 0.4f, textX, y, w - pad - 6 - textX, rowH);
            y += rowH + gap;
        }
        g2.dispose();
    }

    // Draws text centred in the given box, shrinking the font until it fits the width
    private void drawFitted(Graphics2D g2, String text, int style, float startSize, int x, int y, int boxW, int boxH) {
        float size = Math.max(8f, startSize);
        Font f = getFont().deriveFont(style, size);
        FontMetrics fm = g2.getFontMetrics(f);
        while (size > 8f && fm.stringWidth(text) > boxW) {
            size -= 1f;
            f = f.deriveFont(size);
            fm = g2.getFontMetrics(f);
        }
        g2.setFont(f);
        int tx = x + (boxW - fm.stringWidth(text)) / 2;
        int ty = y + (boxH - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);
    }
}
