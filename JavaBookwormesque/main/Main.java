package main;

import data.classes.ScreenType;
import data.classes.UI;
import javax.swing.*;
import main.gameHandlers.MasterDictionary;
import main.gameHandlers.UIHandler;
import main.uiList.GameUI;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MasterDictionary.loadAsync();

            JFrame frame = new JFrame("Spellaria");
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 720);

            // Uses UIHandler to manage screens, starts with the MainMenuUI
            UIHandler uiHandler = new UIHandler(frame);
            uiHandler.setScreen(ScreenType.MENU);

            frame.setVisible(true);

            // Repositions the UI to have consistent resolution across MainMenu and GameUI
            SwingUtilities.invokeLater(() -> {
                UI current = uiHandler.getCurrentUI();
                if (current instanceof GameUI gameUI) {
                    gameUI.repositionUI();
                }
            });
        });
    }
}

/*
PROGRAMMING NOTE:
> Due to time constraints, this application will simply be for demonstration purposes.
Particularly, demonstration of OOP and Multithreading concepts.
> Aim of this application is to demonstrate the game loop of said "bookwormesque" game
and implementation of game loop found in this program will be utilized in future
implementations.
*/