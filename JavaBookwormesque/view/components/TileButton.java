package view.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;
import model.word.Tile;

// Visual representation of a Tile
public class TileButton extends JButton {
    final private int tileSize = 45;
    private final Tile tile;
    private final Border normalBorder;
    private final Border compactBorder = BorderFactory.createLineBorder(new java.awt.Color(0x7A8A99), 1);

    public TileButton(Tile tile) {
        super(String.valueOf(tile.getLetter()));
        this.tile = tile;
        setFocusable(false);
        setMargin(new Insets(0, 0, 0, 0));
        normalBorder = getBorder();
        setPreferredSize(new Dimension(tileSize, tileSize));
        setMinimumSize(new Dimension(tileSize, tileSize));
        setMaximumSize(new Dimension(tileSize, tileSize));
    }

    // The tile is drawn in the grid
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        int side = Math.min(width, height);

        // Very small tiles
        Border wanted = side < 24 ? compactBorder : normalBorder;
        if (getBorder() != wanted) setBorder(wanted);

        float size = Math.max(8f, Math.min(24f, side * 0.45f));
        Font f = getFont();
        if (f != null && (f.getSize2D() != size || !f.isBold())) setFont(f.deriveFont(Font.BOLD, size));
    }

    // The model tile this button displays
    public Tile getTile() { return tile; }
}
