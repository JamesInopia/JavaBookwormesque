package view.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import javax.swing.JComponent;
import javax.swing.Timer;

// The turn timer: a half-circle tab hanging from the top edge of the screen with the seconds left written inside.
// Its border is the countdown: it starts fully drawn and is "eaten" from the right end back round to the left
// start (left side -> bottom -> right side), so at half time only the left side and the left half of the bottom remain.
public class TimerTab extends JComponent {
    private static final Color FILL = new Color(0xD9D9D9);
    private static final Color TRACK = new Color(0, 0, 0, 40);   // faint outline showing where the border used to be
    private static final Color BORDER = new Color(0xD32F2F);
    private static final Color TEXT = new Color(0x222222);
    private static final float STROKE = 6f;
    private static final int SECOND_MS = 1000;

    private int seconds = 0;
    private int total = 60;

    // The border glides from one second's fraction to the next one instead of jumping once per tick
    private double fromFraction = 0, toFraction = 0;
    private long tickStartNanos = 0;
    private final Timer animation;

    public TimerTab() {
        setOpaque(false);
        animation = new Timer(16, e -> {
            repaint();
            if (elapsedMs() >= SECOND_MS) ((Timer) e.getSource()).stop();
        });
    }

    // Called once per second with the seconds left; "total" is the length of a full turn
    public void setTime(int secondsLeft, int totalSeconds) {
        this.total = Math.max(1, totalSeconds);
        this.seconds = Math.max(0, secondsLeft);
        this.fromFraction = clamp01((double) seconds / total);
        this.toFraction = clamp01((double) (seconds - 1) / total);
        this.tickStartNanos = System.nanoTime();
        animation.restart();
        repaint();
    }

    private double elapsedMs() { return (System.nanoTime() - tickStartNanos) / 1_000_000.0; }

    private static double clamp01(double v) { return Math.max(0, Math.min(1, v)); }

    // How much of the border is still drawn right now (1 = all of it, 0 = none)
    private double currentFraction() {
        double t = clamp01(elapsedMs() / SECOND_MS);
        return fromFraction + (toFraction - fromFraction) * t;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth(), h = getHeight();
        double inset = STROKE / 2.0;
        double r = (w - STROKE) / 2.0;                    // radius of the rounded bottom
        double straight = Math.max(0, h - inset - r);     // straight part of the sides above the curve

        // Border path, drawn from the top-left, down, round the bottom and up to the top-right
        Path2D.Double path = new Path2D.Double();
        path.moveTo(inset, 0);
        path.lineTo(inset, straight);
        path.append(new Arc2D.Double(inset, straight - r, 2 * r, 2 * r, 180, 180, Arc2D.OPEN), true);
        path.lineTo(w - inset, 0);
        double length = 2 * straight + Math.PI * r;

        // Body
        Path2D.Double body = new Path2D.Double(path);
        body.closePath();
        g2.setColor(FILL);
        g2.fill(body);

        // Faint track + the part of the border that is left
        g2.setStroke(new BasicStroke(STROKE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.setColor(TRACK);
        g2.draw(path);

        double remaining = currentFraction() * length;
        if (remaining > 0.5) {
            // Dash pattern = "draw 'remaining' pixels, then leave a gap longer than the path": shows only the first part of the path
            g2.setStroke(new BasicStroke(STROKE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f,
                    new float[] { (float) remaining, (float) (length * 2) }, 0f));
            g2.setColor(BORDER);
            g2.draw(path);
        }

        // Seconds left
        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, (int) (w * 0.2))));
        FontMetrics fm = g2.getFontMetrics();
        String text = String.valueOf(seconds);
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (int) ((h - STROKE) * 0.5 + fm.getAscent() * 0.5 - 2);
        g2.setColor(TEXT);
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}
