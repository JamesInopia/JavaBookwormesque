package controller;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import model.battle.ApplyDamage;
import model.entity.Enemy;
import model.entity.EnemyList.*;
import model.entity.Player;
import view.GameUI;

public class TurnHandler {
    private final GameUI ui;

    private Timer playerTimer;
    private int playerSecondsRemaining = 60;
    private boolean playerActed;
    private boolean waitingForPlayer;
    private Enemy currentEnemy;
    private Player player;

    private volatile boolean overlayActive = false;
    private boolean hadPlayerTimerRunningBeforeOverlay = false;

    // Constructor
    public TurnHandler(GameUI ui, List<String> enemies, Player player) {
        this.ui = ui;
        this.player = player;
        this.waitingForPlayer = false;
    }

    // Starts the battle between player and enemy
    public void startBattle() {
        currentEnemy = new Slime();
        ui.showEnemy(currentEnemy, 1000, 200);
        ui.showPlayer(player, 150, 200);

        // Ensures proper loading of UI
        SwingUtilities.invokeLater(() -> {
            ui.repositionUI();
            nextPlayerTurn();
        });
    }

    // Called when player presses enter, handles word submission, damage application, victory check, and passing of turn.
    public void notifyPlayerSubmitted(String committedWord) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> notifyPlayerSubmitted(committedWord));
            return;
        }

        if (overlayActive) {
            playerActed = true;
            waitingForPlayer = false;
            if (playerTimer != null && playerTimer.isRunning()) playerTimer.stop();
            ui.setCounterVisible(false);
            System.out.println("notifyPlayerSubmitted: recorded while overlay active; deferring endPlayerTurn.");
            return;
        }

        // Applies damage to enemy by calling ApplyDamage
        int damage = (int) ApplyDamage.calculateDamage(committedWord);
        System.out.println("Player dealt " + damage + " damage!");
        currentEnemy.takeDamage(damage);
        ui.updateEnemyHP(currentEnemy.getCurrentHP());
        
        // Determines victory if enemy is alive or not
        if (!currentEnemy.isAlive()) {
            System.out.println(currentEnemy.getName() + " has been defeated!");
            if (playerTimer != null && playerTimer.isRunning()) {
                playerTimer.stop();
            }
            waitingForPlayer = false;
            ui.showDefeatOverlay(currentEnemy.getName());
            return;
        }

        // Passes turn to enemy if either player has acted or ran out of time
        if (waitingForPlayer && !playerActed) {
            playerActed = true;
            if (playerTimer != null && playerTimer.isRunning()) playerTimer.stop();
            ui.setCounterVisible(false);
            System.out.println("notifyPlayerSubmitted: ending player turn immediately.");
            endPlayerTurn();
        }
    }

    // Shows an overlay which signals beginning of player/enemy turn
    protected void showTurnOverlay(String text, Runnable afterFade) {
        if (overlayActive) return;
        overlayActive = true;

        ui.setCounterVisible(false);

        hadPlayerTimerRunningBeforeOverlay = playerTimer != null && playerTimer.isRunning();
        if (hadPlayerTimerRunningBeforeOverlay && playerTimer != null) {
            playerTimer.stop();
        }

        // The view animates the overlay, then this resumes the turn flow once it has faded out
        ui.showTurnOverlay(text, () -> {
            overlayActive = false;

            try { afterFade.run(); } catch (Exception ex) { System.err.println("afterFade threw: " + ex); }

            if (playerActed) {
                playerActed = false;
                waitingForPlayer = false;
                if (playerTimer != null && playerTimer.isRunning()) playerTimer.stop();
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Overlay finished: player had acted during overlay — ending turn now.");
                    endPlayerTurn();
                });
                return;
            }

            if (waitingForPlayer) {
                ui.setCounterVisible(true);
                startPlayerTimer();
            }
        });
    }

    // Starts player's turn
    protected void nextPlayerTurn() {
        showTurnOverlay("Your Turn", () -> {
            System.out.println("Player's turn! You have 60 seconds...");
            playerActed = false;
            waitingForPlayer = true;
        });
    }

    // Runs countdown for time player has to act before turn is forcibly ended
    private void startPlayerTimer() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::startPlayerTimer);
            return;
        }

        if (playerTimer != null && playerTimer.isRunning()) playerTimer.stop();

        playerSecondsRemaining = 60;

        ui.setCounterText(String.valueOf(playerSecondsRemaining));
        ui.setCounterVisible(true);

        playerTimer = new Timer(1000, (ActionEvent e) -> {
            playerSecondsRemaining--;
            ui.setCounterText(String.valueOf(playerSecondsRemaining));

            // Skips player turn if time remaining is 0
            if (playerSecondsRemaining <= 0) {
                playerTimer.stop();
                System.out.println("Time's up! Player skipped turn.");
                waitingForPlayer = false;
                ui.setCounterVisible(false);
                endPlayerTurn();
            }
        });
        playerTimer.setRepeats(true);
        playerTimer.start();
    }

    // Ends player turn and passes turn to enemy
    private void endPlayerTurn() {
        waitingForPlayer = false;
        ui.setCounterVisible(false);
        enemyTurn();
    }

    // Handles action done by enemy during their turn and checks if player is dead during their turn signaling player defeat
    protected void enemyTurn() {
        showTurnOverlay("Opponent's Turn", () -> {
            if (currentEnemy != null && currentEnemy.isAlive()) {
                currentEnemy.performMove(player);

                ui.updatePlayerHP(player.getCurrentHP());

                if (!player.isAlive()) {
                    System.out.println(player.getName() + " has been defeated!");
                    ui.showDefeatOverlay(player.getName());
                    return;
                }
            }
            nextPlayerTurn();
        });
    }

    // Getter for ui
    protected GameUI getUI() {
        return ui;
    }
}
