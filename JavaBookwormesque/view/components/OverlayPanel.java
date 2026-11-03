package view.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

// Visual component for the turn overaly with fade effects
public class OverlayPanel extends JPanel {
    private final JLabel message;
    private int bgAlpha = 0;
    private int textAlpha = 0;

    public OverlayPanel(String text) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        message = new JLabel(text);
        message.setFont(new Font("SansSerif", Font.BOLD, 48));
        message.setForeground(new Color(255, 255, 255, 0));
        add(message);

        addMouseListener(new java.awt.event.MouseAdapter() {});
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});
        addMouseWheelListener(e -> {});
        setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {});
    }

    public void setAlphas(int bgAlpha, int textAlpha) {
        this.bgAlpha = Math.max(0, Math.min(255, bgAlpha));
        this.textAlpha = Math.max(0, Math.min(255, textAlpha));
        message.setForeground(new Color(255, 255, 255, this.textAlpha));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgAlpha > 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, bgAlpha));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
