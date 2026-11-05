package view;
import controller.UIHandler;
import java.awt.*;
import javax.swing.*;
import model.level.Level;
import model.level.LevelCatalog;

// Screen where the player picks which level to play or goes back to the main menu
public class LevelSelectUI extends JPanel implements UI {
    private final JButton backBtn;
    private final JButton[] levelButtons = new JButton[LevelCatalog.TOTAL_LEVELS];

    private final UIHandler uiHandler;

    // Receives a UIHandler reference so the level selector can trigger screen changes.
    public LevelSelectUI(UIHandler handler) {
        this.uiHandler = handler;

        // Enforce consistent resolution
        setPreferredSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));
        setMinimumSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));
        setMaximumSize(new Dimension(UIconst.WIDTH, UIconst.HEIGHT));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        JLabel title = new JLabel("Select Level");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Levels 1-10 in two rows of five
        JPanel grid = new JPanel(new GridLayout(2, 5, 15, 15));
        grid.setOpaque(false);
        Dimension gridSize = new Dimension(5 * 130 + 4 * 15, 2 * 80 + 15);
        grid.setMaximumSize(gridSize);
        grid.setPreferredSize(gridSize);
        grid.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (int i = 1; i <= LevelCatalog.TOTAL_LEVELS; i++) {
            final int levelNumber = i;
            Level level = LevelCatalog.create(levelNumber);

            JButton btn = new JButton(buttonText(level));
            // A level without waves has nothing to play yet
            if (level.hasWaves()) {
                btn.addActionListener(e -> uiHandler.startLevel(levelNumber));
            } else {
                btn.setEnabled(false);
                btn.setToolTipText("Coming soon");
            }
            levelButtons[i - 1] = btn;
            grid.add(btn);
        }

        backBtn = new JButton("Back");
        backBtn.setMaximumSize(new Dimension(200, 40));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> uiHandler.setScreen(ScreenType.MENU));

        add(Box.createVerticalGlue());
        add(title);
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(grid);
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(backBtn);
        add(Box.createVerticalGlue());
    }

    // Label on a level button: its number and how many waves it has
    private static String buttonText(Level level) {
        String detail = level.hasWaves() ? level.getMaxWave() + " waves" : "Empty";
        return "<html><center>Level " + level.getNumber() + "<br><small>" + detail + "</small></center></html>";
    }

    // Getters
    public JButton getBackButton() { return backBtn; }
    public JButton getLevelButton(int levelNumber) { return levelButtons[levelNumber - 1]; }

    @Override
    public void load() { }

    @Override
    public void unload() { }
}
