package view.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JLabel;
import model.entity.Enemy;

// Visual representation of one enemy on the battle screen: its picture and HP bar, clickable so the player can target it
public class EnemySprite extends JLabel {
    private final Enemy enemy;
    private final HPBar hpBar;
    private final Image source;
    private final int slot;
    private final int groupSize;
    private int scaledW = -1, scaledH = -1;
    private boolean targeted = false;
    private boolean defeated = false;

    // Icon floating above the enemy that shows what it is going to do next
    private final JLabel intentLabel = new JLabel();
    private Image intentSource;
    private int intentSize = -1;

    public EnemySprite(Enemy enemy, Image source, HPBar hpBar, int slot, int groupSize, Consumer<Enemy> onClick) {
        this.enemy = enemy;
        this.source = source;
        this.hpBar = hpBar;
        this.slot = slot;
        this.groupSize = groupSize;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!defeated) onClick.accept(enemy);
            }
        });
    }

    // Scales the picture to fit the box without distorting it, returns the size it ended up with
    public Dimension fitTo(int maxW, int maxH) {
        Dimension d = SpriteScaler.fitSize(source, maxW, maxH);
        if (d.width != scaledW || d.height != scaledH) {
            setIcon(SpriteScaler.fit(source, maxW, maxH));
            scaledW = d.width;
            scaledH = d.height;
        }
        return d;
    }

    // Sets the intent icon, GameUI calls fitIntent to size it
    public void setIntent(Image icon) {
        intentSource = icon;
        intentSize = -1;
        intentLabel.setVisible(icon != null && !defeated);
    }

    // Scales the intent icon to a square of the given size
    public void fitIntent(int size) {
        if (intentSource == null || size <= 0) return;
        if (size != intentSize) {
            intentLabel.setIcon(SpriteScaler.fit(intentSource, size, size));
            intentSize = size;
        }
    }

    // Only worth showing as clickable when there is more than one enemy to choose from
    public void setTargetable(boolean targetable) {
        setCursor(targetable ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        setToolTipText(targetable ? "Click to target " + enemy.getName() : null);
    }

    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
        repaint();
    }

    public void markDefeated() {
        defeated = true;
        intentLabel.setVisible(false);
        setTargeted(false);
        setTargetable(false);
    }

    // Highlights the targeted enemy
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (targeted) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            g2.dispose();
        }
    }

    // Getters
    public Enemy getEnemy() { return enemy; }
    public HPBar getHPBar() { return hpBar; }
    public JLabel getIntentLabel() { return intentLabel; }
    public int getSlot() { return slot; }
    public int getGroupSize() { return groupSize; }
    public boolean isTargeted() { return targeted; }
    public boolean isDefeated() { return defeated; }
}
