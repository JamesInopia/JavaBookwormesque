package view.components;

import java.awt.Dimension;
import java.awt.Image;
import javax.swing.ImageIcon;

// Scales sprites to fit inside a box WITHOUT changing their aspect ratio
public final class SpriteScaler {
    private SpriteScaler() { }

    // Size the image ends up with when it is scaled down/up until it fits inside maxW x maxH
    public static Dimension fitSize(Image src, int maxW, int maxH) {
        int iw = src.getWidth(null);
        int ih = src.getHeight(null);
        if (iw <= 0 || ih <= 0 || maxW <= 0 || maxH <= 0) return new Dimension(0, 0);

        double scale = Math.min((double) maxW / iw, (double) maxH / ih);
        return new Dimension(Math.max(1, (int) Math.round(iw * scale)), Math.max(1, (int) Math.round(ih * scale)));
    }

    // The scaled image, ready to be used as a label icon
    public static ImageIcon fit(Image src, int maxW, int maxH) {
        Dimension d = fitSize(src, maxW, maxH);
        if (d.width == 0) return new ImageIcon();
        return new ImageIcon(src.getScaledInstance(d.width, d.height, Image.SCALE_SMOOTH));
    }
}
