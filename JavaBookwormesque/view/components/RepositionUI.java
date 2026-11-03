package view.components;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RepositionUI {
    // References to the components you want to reposition
    private final JLayeredPane layeredPane;
    private final JPanel mainPanel;
    private final JPanel gridSection;
    private final JLabel counterLabel;
    private final JLabel playerLabel;
    private final JLabel enemyLabel;
    private final HPBar playerHPBar;
    private final HPBar enemyHPBar;
    private final BackgroundLoader background;

    // Constructor to inject dependencies
    public RepositionUI(JLayeredPane layeredPane, JPanel mainPanel, JPanel gridSection, JLabel counterLabel, JLabel playerLabel, JLabel enemyLabel, HPBar playerHPBar, HPBar enemyHPBar, BackgroundLoader background) {
        this.layeredPane = layeredPane;
        this.mainPanel = mainPanel;
        this.gridSection = gridSection;
        this.counterLabel = counterLabel;
        this.playerLabel = playerLabel;
        this.enemyLabel = enemyLabel;
        this.playerHPBar = playerHPBar;
        this.enemyHPBar = enemyHPBar;
        this.background = background;
    }

    // Updates the positions of UI elements whenever the window or layered pane changes
    public void reposition() {
        layeredPane.doLayout();
        layeredPane.validate();

        Dimension s = layeredPane.getSize();
        if (s.width == 0 || s.height == 0) return;

        background.setBounds(0, 0, s.width, s.height);
        mainPanel.setBounds(0, 0, s.width, s.height);

        Rectangle gridBounds = SwingUtilities.convertRectangle(
            gridSection.getParent(), gridSection.getBounds(), layeredPane
        );
        int w = counterLabel.getPreferredSize().width;
        int h = counterLabel.getPreferredSize().height;
        int x = (s.width - w) / 2;
        int y = gridBounds.y;
        counterLabel.setBounds(x, y, w, h);

        if (playerLabel != null && playerHPBar != null) {
            playerHPBar.setBounds(
                playerLabel.getX(),
                playerLabel.getY() + playerLabel.getHeight() + 5,
                playerLabel.getWidth(),
                20
            );
        }
        if (enemyLabel != null && enemyHPBar != null) {
            enemyHPBar.setBounds(
                enemyLabel.getX(),
                enemyLabel.getY() + enemyLabel.getHeight() + 5,
                enemyLabel.getWidth(),
                20
            );
        }
    }
}