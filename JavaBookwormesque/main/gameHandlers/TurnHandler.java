package main.gameHandlers;
import data.classes.Enemy;
import data.classes.Player;
import data.entityList.Slime;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.*;
import main.uiList.GameUI;

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

        final OverlayPanel overlay = new OverlayPanel(text);

        final Runnable addAndAnimate = () -> {
            Dimension size = ui.getLayeredPane().getSize();
            if (size.width <= 0 || size.height <= 0) size = new Dimension(800, 600);
            overlay.setBounds(0, 0, size.width, size.height);
            ui.getLayeredPane().add(overlay, JLayeredPane.MODAL_LAYER);
            ui.getLayeredPane().revalidate();
            ui.getLayeredPane().repaint();

            SwingUtilities.invokeLater(() -> {
                Dimension s2 = ui.getLayeredPane().getSize();
                if (s2.width > 0 && s2.height > 0) overlay.setBounds(0, 0, s2.width, s2.height);
                overlay.requestFocusInWindow();
            });

            final int fadeInMs = 500;
            final int holdMs = 2000;
            final int fadeOutMs = 500;
            final int tick = 40;
            final int inSteps = Math.max(1, fadeInMs / tick);
            final int outSteps = Math.max(1, fadeOutMs / tick);

            Timer fadeIn = new Timer(tick, null);
            final int[] step = {0};
            fadeIn.addActionListener((ActionEvent e) -> {
                step[0]++;
                float t = Math.min(1f, (float) step[0] / inSteps);
                overlay.setAlphas(Math.round(150 * t), Math.round(255 * t));
                overlay.repaint();
                if (step[0] >= inSteps) {
                    fadeIn.stop();
                    Timer hold = new Timer(holdMs, null);
                    hold.setRepeats(false);
                    hold.addActionListener(ae -> {
                        Timer fadeOut = new Timer(tick, null);
                        final int[] outStep = {0};
                        fadeOut.addActionListener(ev -> {
                            outStep[0]++;
                            float tt = Math.min(1f, (float) outStep[0] / outSteps);
                            overlay.setAlphas(Math.round(150 * (1f - tt)), Math.round(255 * (1f - tt)));
                            overlay.repaint();
                            if (outStep[0] >= outSteps) {
                                fadeOut.stop();

                                ui.getLayeredPane().remove(overlay);
                                ui.getLayeredPane().revalidate();
                                ui.getLayeredPane().repaint();

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
                            }
                        });
                        fadeOut.setInitialDelay(0);
                        fadeOut.start();
                    });
                    hold.start();
                }
            });
            fadeIn.setInitialDelay(0);
            fadeIn.start();
        };

        if (SwingUtilities.isEventDispatchThread()) addAndAnimate.run(); else SwingUtilities.invokeLater(addAndAnimate);
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

    // Visual component for the turn overaly with fade effects
    private static class OverlayPanel extends JPanel {
        private final JLabel message;
        private int bgAlpha = 0;
        private int textAlpha = 0;

        public OverlayPanel(String text) {
            setOpaque(false);
            setLayout(new GridBagLayout());
            message = new JLabel(text);
            message.setFont(new Font("SansSerif", Font.BOLD, 48));
            message.setForeground(new Color(255, 255, 255, 0));
            add(message);

            addMouseListener(new java.awt.event.MouseAdapter() {});
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});
            addMouseWheelListener(e -> {});
            setFocusable(true);
            addKeyListener(new java.awt.event.KeyAdapter() {});
        }

        public void setAlphas(int bgAlpha, int textAlpha) {
            this.bgAlpha = Math.max(0, Math.min(255, bgAlpha));
            this.textAlpha = Math.max(0, Math.min(255, textAlpha));
            message.setForeground(new Color(255, 255, 255, this.textAlpha));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgAlpha > 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, bgAlpha));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }

    // Getter for ui
    protected GameUI getUI() {
            return ui;
        }
}
