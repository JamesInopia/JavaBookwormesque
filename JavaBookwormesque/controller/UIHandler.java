package controller;

import javax.swing.*;
import model.entity.Player;
import model.level.Level;
import model.level.LevelCatalog;
import model.word.LetterBoard;
import view.GameUI;
import view.LevelSelectUI;
import view.MainMenuUI;
import view.ScreenType;
import view.UI;
import view.components.TransitionUI;

public class UIHandler {
    private final JFrame frame;
    private UI currentUI;
    private Level selectedLevel;

    // References main window
    public UIHandler(JFrame frame) {
        this.frame = frame;
    }

    // Maanges screen switching
    public void setScreen(ScreenType screen) {
        UI nextUI = null;
        GameUI gameUI = null;
        TurnHandler handler = null;

        // Decision logic that determines which screen should be shown
        switch (screen) {
            case MENU -> nextUI = new MainMenuUI(this);

            case LEVEL_SELECT -> nextUI = new LevelSelectUI(this);

            case GAME -> {
                if (selectedLevel == null) {
                    throw new IllegalStateException("No level selected, use startLevel(levelNumber).");
                }
                gameUI = new GameUI();
                nextUI = gameUI;

                Player player = new Player();
                handler = new TurnHandler(gameUI, new WaveHandler(selectedLevel), player,
                        () -> setScreen(ScreenType.LEVEL_SELECT));

                WordProcessor wordProcessor = new WordProcessor(new LetterBoard(), gameUI);
                wordProcessor.setTurnHandler(handler);
            }

            default -> throw new IllegalArgumentException("Unexpected value: " + screen);
        }

        if (nextUI != null) {
            nextUI.load();

            JPanel oldPanel = (currentUI != null) ? (JPanel) currentUI : new JPanel();
            JPanel newPanel = (JPanel) nextUI;

            TransitionUI transition = new TransitionUI(oldPanel, newPanel);
            frame.setContentPane(transition);
            frame.revalidate();
            frame.repaint();

            Timer timer = new Timer(30, null);
            final TurnHandler finalHandler = handler;
            final ScreenType finalScreen = screen;

            timer.addActionListener(e -> {
                float alpha = transition.getAlpha() + 0.05f;
                transition.setAlpha(alpha);
                if (alpha >= 1f) {
                    timer.stop();
                    transition.setAlpha(1f);

                    if (finalScreen == ScreenType.GAME && finalHandler != null) {
                        if (newPanel instanceof GameUI g) {
                            g.repositionUI();
                        }
                        finalHandler.startBattle();
                    }

                    frame.setContentPane(newPanel);
                    frame.revalidate();
                    frame.repaint();

                    if (newPanel instanceof GameUI g) {
                        SwingUtilities.invokeLater(g::repositionUI);
                    }
                }
            });
            timer.start();
            currentUI = nextUI;
        }
    }

    // Starts the chosen level from its first wave
    public void startLevel(int levelNumber) {
        selectedLevel = LevelCatalog.create(levelNumber);
        setScreen(ScreenType.GAME);
    }

    // Getter which passes the currentUI
    public UI getCurrentUI() {
        return currentUI;
    }

}
