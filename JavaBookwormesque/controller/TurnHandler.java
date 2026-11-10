package controller;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import model.battle.ApplyDamage;
import model.entity.Enemy;
import model.entity.Move;
import model.entity.Player;
import view.GameUI;

public class TurnHandler {
    private final GameUI ui;
    private final WaveHandler waves;
    private final Runnable onBattleEnd;

    private Timer playerTimer;
    private static final int PLAYER_TURN_SECONDS = 60;
    private int playerSecondsRemaining = PLAYER_TURN_SECONDS;
    private boolean playerActed;
    private boolean waitingForPlayer;
    private boolean battleOver = false;
    private Enemy target;
    private Player player;

    private volatile boolean overlayActive = false;
    private boolean hadPlayerTimerRunningBeforeOverlay = false;

    // Pauses so the player can follow what happens when enemies act one after another and when a wave ends
    private static final int ENEMY_ACTION_DELAY_MS = 600;
    private static final int WAVE_CLEAR_DELAY_MS = 800;

    // Constructor
    public TurnHandler(GameUI ui, WaveHandler waves, Player player, Runnable onBattleEnd) {
        this.ui = ui;
        this.waves = waves;
        this.player = player;
        this.onBattleEnd = onBattleEnd;
        this.waitingForPlayer = false;
        ui.setEnemyClickHandler(this::selectTarget);
        ui.setMoveKnowledge(waves.getMoveKnowledge());
    }

    // Starts the battle between player and enemy
    public void startBattle() {
        loadNextWave();
        ui.showPlayer(player);

        // Ensures proper loading of UI
        SwingUtilities.invokeLater(() -> {
            ui.repositionUI();
            nextPlayerTurn(waveTurnText());
        });
    }

    // Puts the enemies of the next wave on screen and targets the first one
    private void loadNextWave() {
        List<Enemy> enemies = waves.startNextWave();
        ui.showEnemies(enemies);
        target = enemies.get(0);
        ui.setTarget(target);
    }

    // Overlay text for the start of a wave, every wave begins with the player's turn
    private String waveTurnText() {
        return "Wave " + waves.getCurrentWave() + "/" + waves.getMaxWave() + ": Your Turn";
    }

    // Called when the player clicks an enemy, decides which enemy the next submitted word will hit
    private void selectTarget(Enemy enemy) {
        if (battleOver || enemy == null || !enemy.isAlive() || !waves.getEnemies().contains(enemy)) return;
        target = enemy;
        ui.setTarget(target);
    }

    // Moves the target to a living enemy once the current target has been defeated
    private void retarget() {
        for (Enemy e : waves.getEnemies()) {
            if (e.isAlive()) {
                target = e;
                ui.setTarget(target);
                return;
            }
        }
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
        target.takeDamage(damage);
        ui.updateEnemyHP(target, target.getCurrentHP());

        // Determines victory if enemy is alive or not
        if (!target.isAlive()) {
            System.out.println(target.getName() + " has been defeated!");
            ui.removeEnemy(target);
            retarget();
        }

        // The wave ends once every enemy in it is defeated
        if (waves.isWaveCleared()) {
            if (playerTimer != null && playerTimer.isRunning()) {
                playerTimer.stop();
            }
            waitingForPlayer = false;
            ui.setCounterVisible(false);
            onWaveCleared();
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

    // Heals the player when a wave ends, then either finishes the level or begins the next wave (which always starts with the player's turn)
    private void onWaveCleared() {
        int healed = waves.healPlayer(player);
        System.out.println("Wave " + waves.getCurrentWave() + " cleared! Player healed for " + healed + " HP.");
        ui.updatePlayerHP(player.getCurrentHP());

        if (waves.isLastWave()) {
            battleOver = true;
            runAfter(WAVE_CLEAR_DELAY_MS, () -> {
                ui.showBattleResult("Level " + waves.getLevel().getNumber() + " cleared!");
                finishBattle();
            });
        } else {
            runAfter(WAVE_CLEAR_DELAY_MS, () -> {
                loadNextWave();
                SwingUtilities.invokeLater(() -> {
                    ui.repositionUI();
                    nextPlayerTurn(waveTurnText());
                });
            });
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
        nextPlayerTurn("Your Turn");
    }

    // Starts player's turn with the given overlay text (used to announce a new wave)
    protected void nextPlayerTurn(String overlayText) {
        showTurnOverlay(overlayText, () -> {
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

        playerSecondsRemaining = PLAYER_TURN_SECONDS;

        ui.setCounter(playerSecondsRemaining, PLAYER_TURN_SECONDS);
        ui.setCounterVisible(true);

        playerTimer = new Timer(1000, (ActionEvent e) -> {
            playerSecondsRemaining--;
            ui.setCounter(playerSecondsRemaining, PLAYER_TURN_SECONDS);

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
        playerActed = false;
        ui.setCounterVisible(false);
        enemyTurn();
    }

    // Handles action done by enemy during their turn and checks if player is dead during their turn signaling player defeat
    protected void enemyTurn() {
        showTurnOverlay("Opponent's Turn", () -> {
            // Every living enemy takes an action before the turn passes back to the player
            enemyActions(waves.getLivingEnemies(), 0);
        });
    }

    // Lets the enemies act one after another, the turn only returns to the player after the last one has acted
    private void enemyActions(List<Enemy> acting, int index) {
        if (battleOver) return;
        if (index >= acting.size()) {
            nextPlayerTurn();
            return;
        }

        if (!performEnemyAction(acting.get(index))) return;

        if (index + 1 >= acting.size()) {
            nextPlayerTurn();
        } else {
            runAfter(ENEMY_ACTION_DELAY_MS, () -> enemyActions(acting, index + 1));
        }
    }

    // Handles action enemy will perform and checks if player has been defeated
    private boolean performEnemyAction(Enemy enemy) {
        if (!enemy.isAlive()) return true;

        // Using a move reveals it: intent icons and the move guide update right away
        Move used = enemy.performMove(player);
        waves.getMoveKnowledge().reveal(enemy, used);
        ui.refreshMoveInfo();

        ui.updatePlayerHP(player.getCurrentHP());

        if (!player.isAlive()) {
            System.out.println(player.getName() + " has been defeated!");
            battleOver = true;
            ui.showBattleResult(player.getName() + " has been defeated!");
            finishBattle();
            return false;
        }
        return true;
    }

    // Ends the battle and hands control back (to the level selector)
    private void finishBattle() {
        battleOver = true;
        waitingForPlayer = false;
        if (playerTimer != null && playerTimer.isRunning()) playerTimer.stop();
        ui.setCounterVisible(false);
        if (onBattleEnd != null) onBattleEnd.run();
    }

    // Runs something once after a short delay
    private void runAfter(int delayMs, Runnable action) {
        Timer t = new Timer(delayMs, e -> action.run());
        t.setRepeats(false);
        t.start();
    }

    // Input from the word builder is only accepted while it's the player's turn
    public boolean isWaitingForPlayer() {
        return waitingForPlayer && !battleOver;
    }

    // Getter for ui
    protected GameUI getUI() {
        return ui;
    }
}
