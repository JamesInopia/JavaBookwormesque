package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.entity.Enemy;
import model.entity.Player;
import view.components.BackgroundLoader;
import view.components.EnemySprite;
import view.components.HPBar;
import view.components.LetterGridView;
import view.components.OverlayPanel;
import view.components.SpriteScaler;
import view.components.TimerTab;

public class GameUI extends JPanel implements UI {
    private final LetterGridView letterGrid;
    private final JButton enterBtn;
    private final JLayeredPane layeredPane;
    private final JPanel mainPanel;
    private final BackgroundLoader background;

    // Bottom section
    private final JSeparator divider;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private final JPanel gridBox;
    private final JPanel gridSection;
    private final JPanel answerStrip;
    private final JPanel answerRow;

    // Timer tab hanging from the top of the screen
    private final TimerTab timerTab;

    private JLabel playerLabel;
    private Image playerSource;
    private HPBar playerHPBar;

    // Every enemy of the current wave, the enemy the player is targeting, and who to tell when an enemy is clicked
    private final List<EnemySprite> enemySprites = new ArrayList<>();
    private Enemy target;
    private Consumer<Enemy> enemyClickHandler;

    private static final Integer ENEMY_LAYER = JLayeredPane.PALETTE_LAYER + 1;
    private static final Integer ANSWER_LAYER = JLayeredPane.PALETTE_LAYER + 2;

    // Bottom section
    private static final double DIVIDER_Y = 0.585;                    
    private static final double PAD = 0.008;                         
    private static final double[] COLUMN_WEIGHTS = { 405, 239, 405 };
    private static final double GRID_BOX_SHARE = 0.786;              
    private static final double STRIP_SHARE = 0.169;                 

    // Answer row centred, in the upper middle of the screen. Tiles get smaller as more letters are added so the whole word always fits in this width.
    private static final double ANSWER_CX = 0.5, ANSWER_TOP = 0.318;
    private static final double ANSWER_W = 0.32, ANSWER_H = 0.071;

    // Timer tab
    private static final double TIMER_WIDTH = 0.145;
    public static final int TURN_SECONDS_FALLBACK = 60;

    // Player
    private static final double PLAYER_CX = 0.196, PLAYER_FEET_Y = 0.534;
    private static final double PLAYER_BOX_W = 0.106, PLAYER_BOX_H = 0.255;

    // Enemies
    private static final double ENEMY_BOX_W = 0.123, ENEMY_BOX_H = 0.25;

    // Enemies are kept to the right of the tile box 
    private static final double ENEMY_GROUP_CX = 0.805;
    private static final double ENEMY_MIN_CX = 0.675;
    private static final double ENEMY_FEET_FRONT = 0.54;
    private static final double ENEMY_STEP_Y = 0.04;
    
    // Horizontal gap between neighbouring enemies, indexed by group size (index 0 and 1 unused)
    private static final double[] ENEMY_STEP_X_BY_COUNT = { 0, 0, 0.16, 0.125 };

    private static final int HP_BAR_HEIGHT = 20, HP_BAR_GAP = 6;

    // Sets up battle screen
    public GameUI() {
        // Enforce consistent resolution
        setPreferredSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));
        setMinimumSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));
        setMaximumSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));

        letterGrid = new LetterGridView();

        // Grid + box around it
        answerRow = letterGrid.createAnswerRow();
        gridSection = letterGrid.createLetterGrid();

        gridBox = new JPanel(new BorderLayout());
        gridBox.setBackground(Color.WHITE);
        gridBox.setBorder(new EmptyBorder(8, 8, 8, 8));
        gridBox.add(gridSection, BorderLayout.CENTER);

        // Enter button
        enterBtn = new JButton("Enter");

        answerStrip = new JPanel(new BorderLayout());
        answerStrip.setBackground(Color.WHITE);
        answerStrip.setBorder(new EmptyBorder(4, 4, 4, 4));
        answerStrip.add(enterBtn, BorderLayout.CENTER);

        // Empty side panels, reserved for future content
        leftPanel = createSidePanel();
        rightPanel = createSidePanel();

        divider = new JSeparator(SwingConstants.HORIZONTAL);

        // Main panel
        mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);
        mainPanel.add(divider);
        mainPanel.add(leftPanel);
        mainPanel.add(gridBox);
        mainPanel.add(answerStrip);
        mainPanel.add(rightPanel);

        // Layered pane
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        background = new BackgroundLoader("public/backgroundImages/battle_background.jpg");
        layeredPane.add(background, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(mainPanel, JLayeredPane.PALETTE_LAYER);

        mainPanel.setBounds(0, 0, UIconst.WIDTH, UIconst.HEIGHT);
        background.setBounds(0, 0, UIconst.WIDTH, UIconst.HEIGHT);

        // Selected letters sit above the sprites so they can always be clicked
        layeredPane.add(answerRow, ANSWER_LAYER);

        // Timer tab
        timerTab = new TimerTab();
        timerTab.setTime(TURN_SECONDS_FALLBACK, TURN_SECONDS_FALLBACK);
        layeredPane.add(timerTab, JLayeredPane.POPUP_LAYER);

        setLayout(new BorderLayout());
        add(layeredPane, BorderLayout.CENTER);

        // Resize listener
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionUI();
            }
        });

        // Force initial positioning once frame is visible
        SwingUtilities.invokeLater(this::repositionUI);
    }

    // Transparent panel with a thin outline, like the two empty boxes in the mockup
    private JPanel createSidePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        return panel;
    }

    // Helper method to reposition everything that depends on the size of the screen
    public void repositionUI() {
        Dimension s = layeredPane.getSize();
        if (s.width == 0 || s.height == 0) return;
        background.setBounds(0, 0, s.width, s.height);
        mainPanel.setBounds(0, 0, s.width, s.height);

        layoutBottomSection(s.width, s.height);
        layoutTimer(s.width);
        layoutAnswerRow(s.width, s.height);
        layoutEntities(s.width, s.height);

        mainPanel.validate();
        layeredPane.repaint();
    }

    // Bottom section
    private void layoutBottomSection(int w, int h) {
        int splitY = (int) Math.round(h * DIVIDER_Y);
        int pad = (int) Math.round(w * PAD);
        int bottom = h - pad;

        divider.setBounds(0, splitY, w, 2);

        // Split the width between the three columns using their weights
        double weightSum = COLUMN_WEIGHTS[0] + COLUMN_WEIGHTS[1] + COLUMN_WEIGHTS[2];
        double usable = w - 4.0 * pad; // two outer margins + two gaps
        int leftW = (int) Math.round(usable * COLUMN_WEIGHTS[0] / weightSum);
        int centerW = (int) Math.round(usable * COLUMN_WEIGHTS[1] / weightSum);
        int rightW = (int) usable - leftW - centerW;

        int leftX = pad;
        int centerX = leftX + leftW + pad;
        int rightX = centerX + centerW + pad;

        // Side panels sit a little under the divider
        int sideY = splitY + pad;
        leftPanel.setBounds(leftX, sideY, leftW, bottom - sideY);
        rightPanel.setBounds(rightX, sideY, rightW, bottom - sideY);

        // Centre column
        int columnY = splitY + 2;
        int columnH = bottom - columnY;
        int gridH = (int) Math.round(columnH * GRID_BOX_SHARE);
        int stripH = (int) Math.round(columnH * STRIP_SHARE);
        gridBox.setBounds(centerX, columnY, centerW, gridH);
        answerStrip.setBounds(centerX, bottom - stripH, centerW, stripH);
    }

    // Answer row
    private void layoutAnswerRow(int w, int h) {
        int rowW = (int) Math.round(w * ANSWER_W);
        int rowH = (int) Math.round(h * ANSWER_H);
        answerRow.setBounds((int) Math.round(w * ANSWER_CX) - rowW / 2, (int) Math.round(h * ANSWER_TOP), rowW, rowH);
    }

    // Timer tab is centred and glued to the top edge
    private void layoutTimer(int w) {
        int tabW = (int) Math.round(w * TIMER_WIDTH);
        timerTab.setBounds((w - tabW) / 2, 0, tabW, tabW / 2);
    }

    // Player and enemies
    private void layoutEntities(int w, int h) {
        if (playerLabel != null && playerSource != null) {
            Dimension d = SpriteScaler.fitSize(playerSource, (int) (w * PLAYER_BOX_W), (int) (h * PLAYER_BOX_H));
            if (playerLabel.getIcon() == null || playerLabel.getIcon().getIconWidth() != d.width
                    || playerLabel.getIcon().getIconHeight() != d.height) {
                playerLabel.setIcon(SpriteScaler.fit(playerSource, (int) (w * PLAYER_BOX_W), (int) (h * PLAYER_BOX_H)));
            }
            placeByFeet(playerLabel, playerHPBar, PLAYER_CX, PLAYER_FEET_Y, d, w, h);
        }

        for (EnemySprite sprite : enemySprites) {
            Dimension d = sprite.fitTo((int) (w * ENEMY_BOX_W), (int) (h * ENEMY_BOX_H));
            double[] spot = enemySpot(sprite.getSlot(), sprite.getGroupSize());
            placeByFeet(sprite, sprite.getHPBar(), spot[0], spot[1], d, w, h);
        }
    }

    // Puts the picture so its bottom-centre is at of the screen, with its HP bar right underneath
    private void placeByFeet(JComponent picture, HPBar bar, double cx, double feetY, Dimension size, int w, int h) {
        int x = (int) Math.round(cx * w - size.width / 2.0);
        int y = (int) Math.round(feetY * h - size.height);
        picture.setBounds(x, y, size.width, size.height);
        if (bar != null) bar.setBounds(x, y + size.height + HP_BAR_GAP, size.width, HP_BAR_HEIGHT);
    }

    // Spot of an enemy: a staircase going up-left from the front enemy, with the whole group centred on ENEMY_GROUP_CX
    private double[] enemySpot(int slot, int groupSize) {
        int fromFront = groupSize - 1 - slot;   // 0 = front (rightmost) enemy
        double stepX = 0;
        if (groupSize > 1) {
            stepX = groupSize < ENEMY_STEP_X_BY_COUNT.length
                ? ENEMY_STEP_X_BY_COUNT[groupSize]
                : Math.min(ENEMY_STEP_X_BY_COUNT[ENEMY_STEP_X_BY_COUNT.length - 1],
                           2 * (ENEMY_GROUP_CX - ENEMY_MIN_CX) / (groupSize - 1));
        }
        double cx = ENEMY_GROUP_CX + ((groupSize - 1) / 2.0 - fromFront) * stepX;
        double feetY = ENEMY_FEET_FRONT - fromFront * ENEMY_STEP_Y;
        return new double[] { cx, feetY };
    }

    // One enemy of a wave of groupSize enemies
    public void showEnemy(Enemy enemy, int slot, int groupSize) {
        Image source = new ImageIcon(enemy.getImagePath()).getImage();

        HPBar hpBar = new HPBar(enemy.getMaxHP());
        hpBar.setHP(enemy.getCurrentHP());

        EnemySprite sprite = new EnemySprite(enemy, source, hpBar, slot, groupSize, clicked -> {
            if (enemyClickHandler != null) enemyClickHandler.accept(clicked);
        });

        layeredPane.add(sprite, ENEMY_LAYER);
        layeredPane.add(hpBar, JLayeredPane.PALETTE_LAYER);
        enemySprites.add(sprite);
        updateTargetHighlights();
    }

    // Replaces whatever enemies are on screen with the enemies of a wave
    public void showEnemies(List<Enemy> enemies) {
        clearEnemies();
        for (int i = 0; i < enemies.size(); i++) {
            showEnemy(enemies.get(i), i, enemies.size());
        }
        repositionUI();
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    // Takes every enemy off the screen
    private void clearEnemies() {
        for (EnemySprite sprite : enemySprites) {
            layeredPane.remove(sprite);
            layeredPane.remove(sprite.getHPBar());
        }
        enemySprites.clear();
        layeredPane.repaint();
    }

    // Marks which enemy the player is targeting, only shown when there is more than one enemy to choose from
    public void setTarget(Enemy enemy) {
        this.target = enemy;
        updateTargetHighlights();
    }

    // Controller registers what should happen when an enemy is clicked
    public void setEnemyClickHandler(Consumer<Enemy> handler) {
        this.enemyClickHandler = handler;
    }

    // Highlight and click feedback only apply while at least two enemies are standing
    private void updateTargetHighlights() {
        int living = 0;
        for (EnemySprite sprite : enemySprites) {
            if (!sprite.isDefeated()) living++;
        }
        boolean choosable = living > 1;
        for (EnemySprite sprite : enemySprites) {
            boolean alive = !sprite.isDefeated();
            sprite.setTargetable(choosable && alive);
            sprite.setTargeted(choosable && alive && sprite.getEnemy() == target);
        }
    }

    // Takes a defeated enemy off the screen shortly after its HP bar has emptied
    public void removeEnemy(Enemy enemy) {
        for (EnemySprite sprite : enemySprites) {
            if (sprite.getEnemy() == enemy) {
                sprite.markDefeated();
                updateTargetHighlights();

                Timer t = new Timer(500, e -> {
                    layeredPane.remove(sprite);
                    layeredPane.remove(sprite.getHPBar());
                    enemySprites.remove(sprite);
                    layeredPane.repaint();
                });
                t.setRepeats(false);
                t.start();
                return;
            }
        }
    }

    public void showPlayer(Player player) {
        if (playerLabel != null) layeredPane.remove(playerLabel);

        playerSource = new ImageIcon(player.getImagePath()).getImage();
        playerLabel = new JLabel();
        layeredPane.add(playerLabel, JLayeredPane.PALETTE_LAYER);

        if (playerHPBar == null) {
            playerHPBar = new HPBar(player.getMaxHP());
            layeredPane.add(playerHPBar, JLayeredPane.PALETTE_LAYER);
        }
        playerHPBar.setHP(player.getCurrentHP());

        repositionUI();
    }

    // Counter API: seconds left, and how many seconds a full turn lasts (the border of the tab is the countdown)
    public void setCounter(int secondsLeft, int totalSeconds) {
        timerTab.setTime(secondsLeft, totalSeconds);
    }

    // Shows the counter
    public void setCounterVisible(boolean visible) { timerTab.setVisible(visible); }

    // Empty boxes next to the grid, for whatever goes there later
    public JPanel getLeftPanel() { return leftPanel; }
    public JPanel getRightPanel() { return rightPanel; }

    // Update bars when logic changes HP
    public void updateEnemyHP(Enemy enemy, int hp) {
        SwingUtilities.invokeLater(() -> {
            for (EnemySprite sprite : enemySprites) {
                if (sprite.getEnemy() == enemy) {
                    sprite.getHPBar().setHPAnimated(hp);
                    sprite.getHPBar().repaint();
                }
            }
        });
    }

    public void updatePlayerHP(int hp) {
        SwingUtilities.invokeLater(() -> {
            if (playerHPBar != null) {
                System.out.println("Updating player HP bar to: " + hp + " / " + playerHPBar.getMaxHP());
                playerHPBar.setHPAnimated(hp);
                playerHPBar.repaint();
            }
        });
    }

    // Updates Enter Button if user forms a valid word
    public void updateEnterButtonBorder(boolean valid) {
        if (valid) {
            enterBtn.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        } else {
            enterBtn.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("Button.border"));
        }
    }

    // Overlay when enemy dies, exits the game when interacted with
    public void showDefeatOverlay(String enemyName) {
        JOptionPane.showMessageDialog(this, enemyName + " defeated!");
        System.exit(0);
    }

    // Tells the player how the battle ended (level cleared or player defeated), returns once it has been dismissed
    public void showBattleResult(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    // Plays the turn overlay (fade in, hold, fade out) and calls onFinished once the overlay has been removed
    public void showTurnOverlay(String text, Runnable onFinished) {
        final OverlayPanel overlay = new OverlayPanel(text);

        final Runnable addAndAnimate = () -> {
            Dimension size = getLayeredPane().getSize();
            if (size.width <= 0 || size.height <= 0) size = new Dimension(800, 600);
            overlay.setBounds(0, 0, size.width, size.height);
            getLayeredPane().add(overlay, JLayeredPane.MODAL_LAYER);
            getLayeredPane().revalidate();
            getLayeredPane().repaint();

            SwingUtilities.invokeLater(() -> {
                Dimension s2 = getLayeredPane().getSize();
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

                                getLayeredPane().remove(overlay);
                                getLayeredPane().revalidate();
                                getLayeredPane().repaint();

                                onFinished.run();
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

    // Other getters/setters
    public JLayeredPane getLayeredPane() { return layeredPane; }
    public JButton getEnterButton() { return enterBtn; }
    public LetterGridView getLetterGridView() { return letterGrid; }
    public JLabel getPlayerLabel() { return playerLabel; }
    public HPBar getPlayerHPBar() { return playerHPBar; }
    public List<EnemySprite> getEnemySprites() { return enemySprites; }

    @Override 
    public void load() { }

    @Override 
    public void unload() { }
}