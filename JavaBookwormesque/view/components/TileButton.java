package view.components;

import java.awt.Dimension;
import javax.swing.JButton;
import model.word.Tile;

// Visual representation of a Tile: the button the player clicks to move a letter between the grid and the answerRow
public class TileButton extends JButton {
    final private int tileSize = 45;
    private final Tile tile;

    public TileButton(Tile tile) {
        super(String.valueOf(tile.getLetter()));
        this.tile = tile;
        setFocusable(false);
        setPreferredSize(new Dimension(tileSize, tileSize));
        setMinimumSize(new Dimension(tileSize, tileSize));
        setMaximumSize(new Dimension(tileSize, tileSize));
    }

    // The model tile this button displays
    public Tile getTile() { return tile; }
}
