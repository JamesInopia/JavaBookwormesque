package main.gameHandlers;
import java.awt.*;
import javax.swing.*;

// JPanel that loads the background image of a given UI
public class BackgroundLoader extends JPanel {
    private Image backgroundImage;

    public BackgroundLoader(String imagePath) {
        backgroundImage = new ImageIcon(imagePath).getImage();
        setLayout(new BorderLayout()); // so you can add child panels on top
    }

    // Draws background iamge when panel is repainted
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
