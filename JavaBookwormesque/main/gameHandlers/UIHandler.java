package main.gameHandlers;

import data.classes.ScreenType;
import data.classes.UI;
import java.util.List;
import javax.swing.*;
import main.uiList.GameUI;
import main.uiList.MainMenuUI;
import main.uiList.TransitionUI;

public class UIHandler {
    private final JFrame frame;   // The main window
    private UI currentUI;         // Currently active UI

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

            case GAME -> {
                gameUI = new GameUI();
                nextUI = gameUI;

                data.classes.Player player = new data.classes.Player();
                handler = new TurnHandler(gameUI, List.of("Enemy"), player);
                gameUI.setTurnHandler(handler);
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

    // Getter which passes the currentUI
    public UI getCurrentUI() {
        return currentUI;
    }

}
