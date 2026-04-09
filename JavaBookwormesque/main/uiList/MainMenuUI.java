package main.uiList;
import data.classes.ScreenType;
import data.classes.UI;
import data.classes.UIconst;
import java.awt.*;
import javax.swing.*;
import main.gameHandlers.UIHandler;

public class MainMenuUI extends JPanel implements UI {
    private final JButton playBtn;
    private final JButton settingsBtn;
    private final JButton quitBtn;

    private final UIHandler uiHandler;

    // Receives a UIHandler reference so the menu can trigger screen changes.
    public MainMenuUI(UIHandler handler) {
        this.uiHandler = handler;

        // Enforce consistent resolution
        setPreferredSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));
        setMinimumSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));
        setMaximumSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // Create buttons
        playBtn = new JButton("Play Game");
        settingsBtn = new JButton("Settings");
        quitBtn = new JButton("Quit");

        Dimension btnSize = new Dimension(200, 40);
        for (JButton b : new JButton[]{playBtn, settingsBtn, quitBtn}) {
            b.setMaximumSize(btnSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(Box.createRigidArea(new Dimension(0, 10)));
            add(b);
        }

        //Adds functionality to the buttons
        playBtn.addActionListener(e -> uiHandler.setScreen(ScreenType.GAME));
        quitBtn.addActionListener(e -> System.exit(0));
    }

    @Override
    public void load() { }

    @Override
    public void unload() { }
}
