package main.uiList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.Timer;

public class HPBar extends JPanel {
    private int currentHP;
    private final int maxHP;
    private int targetHP;

    // Initializes health bar with max HP and sets HP bar size
    public HPBar(int maxHP) {
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.targetHP = maxHP;
        setPreferredSize(new Dimension(200, 30));
    }

    // Updates HP bar to new HP value
    public void setHP(int newHP) {
        currentHP = Math.max(0, Math.min(newHP, maxHP));
        targetHP = currentHP;
        repaint();
    }

    // Updates HP using smooth animation
    public void setHPAnimated(int newHP) {
        targetHP = Math.max(0, Math.min(newHP, maxHP));
        final int step = (currentHP - targetHP) / 20; // 20 steps
        final int actualStep = (step == 0) ? (currentHP > targetHP ? -1 : 1) : step;

        Timer timer = new Timer(15, e -> {
            if (currentHP == targetHP) {
                ((Timer) e.getSource()).stop();
            } else {
                currentHP += actualStep;
                if ((actualStep < 0 && currentHP < targetHP) || (actualStep > 0 && currentHP > targetHP)) {
                    currentHP = targetHP;
                }
                repaint();
            }
        });
        timer.start();
    }

    // Getters
    public int getCurrentHP() { return currentHP; }
    public int getMaxHP() { return maxHP; }

    // Rendering logic for HP bar
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();

        Graphics2D g2 = (Graphics2D) g.create();

        // Rounded background
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(0, 0, width, height, 15, 15);

        // HP bar color based on percentage
        double percent = (double) currentHP / maxHP;
        int barWidth = (int) (width * percent);
        if (percent > 0.6) {
            g2.setColor(Color.GREEN);
        } else if (percent > 0.3) {
            g2.setColor(Color.ORANGE);
        } else {
            g2.setColor(Color.RED);
        }
        g2.fillRoundRect(0, 0, barWidth, height, 15, 15);

        // White text
        g2.setColor(Color.WHITE);
        String text = currentHP + " / " + maxHP;
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2.drawString(text, (width - textWidth) / 2, (height + textHeight) / 2 - 2);

        g2.dispose();
    }
}