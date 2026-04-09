package data.classes;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JButton;

// Tile class which tracks original position of tile from the grid
public class Tile extends JButton {
    final private int tileSize = 45;

    // Position in the tile grid and if in answerRow
    int homeCol, homeRow;
    boolean inAnswerRow = false;
    public boolean selected = false;

    // Constructor
    public Tile(char c, int col, int row) {
        super(String.valueOf(c));
        this.homeCol = col;
        this.homeRow = row;
        setFocusable(false);
        setPreferredSize(new Dimension(tileSize, tileSize));
        setMinimumSize(new Dimension(tileSize, tileSize));
        setMaximumSize(new Dimension(tileSize, tileSize));
    }

    // Sets its position based on their placement in the grid
    public void setHome(int col, int row) { this.homeCol = col; this.homeRow = row; }

    // Getter
    public Point getHome() { return new Point(homeCol, homeRow); }
}
