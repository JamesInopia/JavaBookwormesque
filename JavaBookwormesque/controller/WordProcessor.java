package controller;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import model.battle.ApplyDamage;
import model.word.LetterBoard;
import model.word.Tile;
import view.GameUI;
import view.components.LetterGridView;

// Controller for the word-building input: connects tile clicks and the Enter button to the LetterBoard model and keeps the view in sync
public class WordProcessor {
    private final LetterBoard board;
    private final GameUI ui;
    private final LetterGridView view;
    private TurnHandler turnHandler;
    private volatile boolean busy = false;

    public WordProcessor(LetterBoard board, GameUI ui) {
        this.board = board;
        this.ui = ui;
        this.view = ui.getLetterGridView();

        view.setTileClickHandler(this::onTileClicked);

        ui.getEnterButton().addActionListener(e -> {
            if (board.isValidWord()) {
                String committedWord = commitSelectedAndRefill();
                ui.updateEnterButtonBorder(false);
                if (turnHandler != null) {
                    turnHandler.notifyPlayerSubmitted(committedWord);
                }
            } else {
                System.out.println("Invalid word!");
            }
        });

        spawnUntilFull();
    }

    // Tells the controller which TurnHandler to notify once a word is submitted
    public void setTurnHandler(TurnHandler th) { this.turnHandler = th; }

    // Defines behaviour of tile when clicked: adds them to answerRow if in grid, and returns them to grid if in answerRow
    private void onTileClicked(Tile t) {
        if (busy) return;
        busy = true;
        SwingUtilities.invokeLater(() -> {
            try {
                // Adds Tile from grid to answerRow
                if (!t.isSelected()) {
                    board.addToHolding(t);
                    view.rebuildHoldingPanel(board.getHoldingRow());
                    view.refreshGrid(board);
                    ui.updateEnterButtonBorder(board.isValidWord());
                }
                else {
                    // Returns tile from answerRow to grid
                    if (board.returnToGrid(t)) {
                        view.rebuildHoldingPanel(board.getHoldingRow());
                        view.refreshGrid(board);
                        ui.updateEnterButtonBorder(board.isValidWord());
                    }
                }
            } finally {
                // Small delay to ensure no issues occur during a double click
                new Timer(40, ev -> { busy = false; ((Timer)ev.getSource()).stop(); }).start();
            }
        });
    }

    // Fills every empty slot of the grid on the Swing thread, then redraws it
    private void spawnUntilFull() {
        SwingUtilities.invokeLater(() -> {
            board.spawnUntilFull();
            view.refreshGrid(board);
        });
    }

    // Submits the player's built word
    private String commitSelectedAndRefill() {
        String committed = board.getHeldWord();

        // Console tracking
        System.out.println("[WordProcessor] Committed word: \"" + committed + "\" (partition: " + board.getLastPartitionLabel() + ")");

        int damage = (int) ApplyDamage.calculateDamage(committed);
        System.out.println("[Damage] " + committed + " dealt " + damage + " damage!");

        // Remove selected tiles
        board.removeSelectedTiles();

        // clear holding UI
        board.clearHoldingRow();
        view.clearAnswerRow();

        // gravity + refill
        board.applyGravityAndUpdateHomes();
        spawnUntilFull();

        return committed;
    }
}
