package model.word;

import java.awt.Point;

// Tile class which tracks original position of tile from the grid
public class Tile {
    final private char letter;

    // Position in the tile grid and if in answerRow
    private int homeCol, homeRow;
    private boolean inAnswerRow = false;
    private boolean selected = false;

    // Constructor
    public Tile(char c, int col, int row) {
        this.letter = c;
        this.homeCol = col;
        this.homeRow = row;
    }

    // Sets its position based on their placement in the grid
    public void setHome(int col, int row) { this.homeCol = col; this.homeRow = row; }

    // Getter
    public Point getHome() { return new Point(homeCol, homeRow); }
    public char getLetter() { return letter; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
