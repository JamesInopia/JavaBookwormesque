package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import model.entity.Enemy;
import model.entity.Player;
import view.components.BackgroundLoader;
import view.components.HPBar;
import view.components.LetterGridView;
import view.components.OverlayPanel;

public class GameUI extends JPanel implements UI {
    private final LetterGridView letterGrid;
    private final JButton enterBtn;
    private final JLayeredPane layeredPane;
    private final JPanel mainPanel;
    private final BackgroundLoader background;

    private final JLabel counterLabel;
    private final JPanel middleSection;
    private final JPanel gridSection;

    private JLabel playerLabel;
    private JLabel enemyLabel;

    final private int gridHeight = 100;
    final private int gridWidth = 250;
    final private int answerRowWidth = 800;
    final private int answerRowHeight = 50;

    private HPBar playerHPBar;
    private HPBar enemyHPBar;

    private static final double MAIN_RATIO = 0.3;

    // Sets up battle screen
    public GameUI() {
        // Enforce consistent resolution
        setPreferredSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));
        setMinimumSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));
        setMaximumSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));

        letterGrid = new LetterGridView();

        // Answer row + grid
        JPanel answerRow = letterGrid.createAnswerRow();
        answerRow.setPreferredSize(new Dimension(answerRowWidth, answerRowHeight));
        gridSection = letterGrid.createLetterGrid();

        // Lock grid size
        gridSection.setPreferredSize(new Dimension(gridWidth, gridHeight));
        gridSection.setMinimumSize(new Dimension(gridWidth, gridHeight));
        gridSection.setMaximumSize(new Dimension(gridWidth, gridHeight));

        // Enter button
        enterBtn = new JButton("Enter");
        enterBtn.setPreferredSize(new Dimension(200, 40));
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Top section
        JPanel topHalf = new JPanel(new GridBagLayout());
        topHalf.setOpaque(false);
        topHalf.add(answerRow);

        // Middle section
        middleSection = new JPanel(new GridBagLayout());
        middleSection.setOpaque(false);
        GridBagConstraints mGbc = new GridBagConstraints();
        mGbc.gridx = 0; mGbc.gridy = 0;
        mGbc.fill = GridBagConstraints.BOTH;
        mGbc.weightx = 1.0;
        mGbc.weighty = 1.0;
        middleSection.add(gridSection, mGbc);

        mGbc.gridy = 1;
        mGbc.weighty = 0.0;
        middleSection.add(Box.createRigidArea(new Dimension(0, 10)), mGbc);

        mGbc.gridy = 2;
        middleSection.add(enterBtn, mGbc);

        // Bottom strip
        JPanel bottomStrip = new JPanel(new GridLayout(1, 3, 10, 0));
        bottomStrip.setOpaque(false);
        bottomStrip.add(new JPanel());
        bottomStrip.add(middleSection);
        bottomStrip.add(new JPanel());

        // Main panel
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        gbc.weighty = 1.0 - MAIN_RATIO;
        mainPanel.add(topHalf, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.0;
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);

        gbc.gridy = 2;
        gbc.weighty = MAIN_RATIO;
        mainPanel.add(bottomStrip, gbc);

        // Layered pane
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        background = new BackgroundLoader("data/backgroundImages/battle_background.jpg");
        layeredPane.add(background, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(mainPanel, JLayeredPane.PALETTE_LAYER);

        mainPanel.setBounds(0, 0, UIconst.WIDTH, UIconst.HEIGHT);
        background.setBounds(0, 0, UIconst.WIDTH, UIconst.HEIGHT);

        // Counter label
        counterLabel = new JLabel("0", SwingConstants.CENTER);
        counterLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        counterLabel.setForeground(Color.WHITE);
        counterLabel.setPreferredSize(new Dimension(100, 40));
        counterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        counterLabel.setVerticalAlignment(SwingConstants.CENTER);
        counterLabel.setVisible(true);
        layeredPane.add(counterLabel, JLayeredPane.POPUP_LAYER);

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

    // Helper method to reposition overlay, counter, and HP bars
    public void repositionUI() {
        Dimension s = layeredPane.getSize();
        if (s.width == 0 || s.height == 0) return; // avoid invalid size before frame is visible

        // Resize background and main panel
        background.setBounds(0, 0, s.width, s.height);
        mainPanel.setBounds(0, 0, s.width, s.height);

        // Position counter label above the grid
        Rectangle gridBounds = SwingUtilities.convertRectangle(
            gridSection.getParent(), gridSection.getBounds(), layeredPane
        );
        int w = counterLabel.getPreferredSize().width;
        int h = counterLabel.getPreferredSize().height;
        int x = (s.width - w) / 2;
        int y = gridBounds.y - 100;
        counterLabel.setBounds(x, y, w, h);

        // Reposition HP bars under labels
        if (playerLabel != null) {
            playerHPBar.setBounds(
                playerLabel.getX(),
                playerLabel.getY() + playerLabel.getHeight() + 5,
                playerLabel.getWidth(),
                20
            );
        }
        if (enemyLabel != null) {
            enemyHPBar.setBounds(
                enemyLabel.getX(),
                enemyLabel.getY() + enemyLabel.getHeight() + 5,
                enemyLabel.getWidth(),
                20
            );
        }
    }

    // Player/enemy UI
    public void showEnemy(Enemy enemy, int x, int y) {
        ImageIcon icon = new ImageIcon(enemy.getImagePath());
        Image scaled = icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
        enemyLabel = new JLabel(new ImageIcon(scaled));
        enemyLabel.setBounds(x, y, 128, 128);
        layeredPane.add(enemyLabel, JLayeredPane.PALETTE_LAYER);

        if (enemyHPBar == null) {
            enemyHPBar = new HPBar(enemy.getMaxHP());
        }
        enemyHPBar.setHP(enemy.getCurrentHP());
        enemyHPBar.setBounds(x, y + enemyLabel.getHeight() + 5, enemyLabel.getWidth(), 20);
        layeredPane.add(enemyHPBar, JLayeredPane.PALETTE_LAYER);
    }

    public void showPlayer(Player player, int x, int y) {
        ImageIcon icon = new ImageIcon(player.getImagePath());
        Image scaled = icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
        playerLabel = new JLabel(new ImageIcon(scaled));
        playerLabel.setBounds(x, y, 128, 128);
        layeredPane.add(playerLabel, JLayeredPane.PALETTE_LAYER);

        if (playerHPBar == null) {
            playerHPBar = new HPBar(player.getMaxHP());
            layeredPane.add(playerHPBar, JLayeredPane.PALETTE_LAYER);
        }
        playerHPBar.setHP(player.getCurrentHP());
        playerHPBar.setBounds(x, y + playerLabel.getHeight() + 5, playerLabel.getWidth(), 20);
    }

    // Counter API
    public void setCounterText(String text) {
        counterLabel.setText(text);
        counterLabel.repaint();
    }

    // Shows the counter
    public void setCounterVisible(boolean visible) { counterLabel.setVisible(visible); }

    // Update bars when logic changes HP
    public void updateEnemyHP(int hp) {
        SwingUtilities.invokeLater(() -> {
            if (enemyHPBar != null) {
                enemyHPBar.setHPAnimated(hp);
                enemyHPBar.repaint(); 
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
    public JLabel getEnemyLabel() { return enemyLabel; }
    public HPBar getPlayerHPBar() { return playerHPBar; }
    public HPBar getEnemyHPBar() { return enemyHPBar; }

    @Override 
    public void load() { }

    @Override 
    public void unload() { }
}