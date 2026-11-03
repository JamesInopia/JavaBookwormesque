package view.components;

import java.awt.*;
import javax.swing.*;

public class TransitionUI extends JPanel {
    private final JPanel oldPanel;
    private final JPanel newPanel;
    private float alpha = 0f;

    public TransitionUI(JPanel oldPanel, JPanel newPanel) {
        this.oldPanel = oldPanel;
        this.newPanel = newPanel;
        setLayout(null);
        add(oldPanel);
        add(newPanel);
        newPanel.setVisible(true);
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
        repaint();
    }

    public float getAlpha() {
        return alpha;
    }

    // Animate the fade transition
    public void startTransition(Runnable onComplete) {
        Timer t = new Timer(40, null); 
        t.addActionListener(e -> {
            setAlpha(getAlpha() + 0.05f); 
            if (getAlpha() >= 1f) {
                t.stop();
                if (onComplete != null) onComplete.run();
            }
        });
        t.start();
    }

    // Handles the fade-out and fade-in transitions
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - alpha));
        oldPanel.paint(g2);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        newPanel.paint(g2);
        g2.dispose();
    }
}
