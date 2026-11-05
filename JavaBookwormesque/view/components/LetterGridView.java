package view.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.word.LetterBoard;
import model.word.Tile;

// View for the letter grid and answerRow: draws whatever the LetterBoard model currently holds and reports tile clicks
public class LetterGridView {
    private JPanel gridPanel, answerRow;
    private final Map<Tile, TileButton> buttons = new HashMap<>();
    private Consumer<Tile> tileClickHandler;

    // UI creation
    public JPanel createAnswerRow() {
        answerRow = new JPanel(new AnswerRowLayout());
        answerRow.setOpaque(false);
        return answerRow;
    }

    public JPanel createLetterGrid() {
        gridPanel = new JPanel(new GridLayout(LetterBoard.ROWS, LetterBoard.COLS, 4, 4));
        return gridPanel;
    }

    // Controller registers what should happen when a tile is clicked
    public void setTileClickHandler(Consumer<Tile> handler) {
        this.tileClickHandler = handler;
    }

    // Gets the button that displays a tile
    private TileButton buttonFor(Tile t) {
        TileButton btn = buttons.get(t);
        if (btn == null) {
            btn = new TileButton(t);
            resetListeners(btn, e -> {
                if (tileClickHandler != null) tileClickHandler.accept(t);
            });
            buttons.put(t, btn);
        }
        return btn;
    }

    // Removes all existing ActionListeners from a button and attaches the specified one
    private static void resetListeners(AbstractButton b, ActionListener l) {
        for (ActionListener a : b.getActionListeners()) b.removeActionListener(a);
        b.addActionListener(l);
    }

    // Ensures that only tiles that were called to removed are removed from answerRow
    public void rebuildHoldingPanel(List<Tile> holdingRow) {
        if (answerRow == null) return;
        answerRow.removeAll();
        for (Tile t : holdingRow) answerRow.add(buttonFor(t));
        answerRow.revalidate(); answerRow.repaint();
    }

    // Clears the answerRow
    public void clearAnswerRow() {
        if (answerRow != null) { answerRow.removeAll(); answerRow.revalidate(); answerRow.repaint(); }
    }

    // Renders grid
    public void refreshGrid(LetterBoard board) {
        if (gridPanel == null) return;
        gridPanel.removeAll();
        Set<Tile> live = new HashSet<>();
        for (int r = 0; r < LetterBoard.ROWS; r++) {
            for (int c = 0; c < LetterBoard.COLS; c++) {
                Tile t = board.getTile(c, r);
                if (t == null) gridPanel.add(new JLabel(""));
                else {
                    live.add(t);
                    gridPanel.add(t.isSelected() ? new JLabel("") : buttonFor(t));
                }
            }
        }
        // Forgets buttons of tiles that have been removed from the board
        buttons.keySet().retainAll(live);
        gridPanel.revalidate(); gridPanel.repaint();
    }

    // Lays the answerRow tiles out in one centred line
    private static class AnswerRowLayout implements LayoutManager {
        private static final int MAX_TILE = 50, GAP = 4;

        @Override public void addLayoutComponent(String name, Component comp) { }
        @Override public void removeLayoutComponent(Component comp) { }
        @Override public Dimension preferredLayoutSize(Container parent) { return new Dimension(0, MAX_TILE); }
        @Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(0, 0); }

        @Override
        public void layoutContainer(Container parent) {
            int n = parent.getComponentCount();
            if (n == 0) return;

            Insets in = parent.getInsets();
            int availW = parent.getWidth() - in.left - in.right;
            int availH = parent.getHeight() - in.top - in.bottom;
            int size = Math.max(1, Math.min(MAX_TILE, Math.min(availH, (availW - GAP * (n - 1)) / n)));

            int x = in.left + (availW - (size * n + GAP * (n - 1))) / 2;
            int y = in.top + (availH - size) / 2;
            for (Component c : parent.getComponents()) {
                c.setBounds(x, y, size, size);
                x += size + GAP;
            }
        }
    }
}
