package ui;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.swing.*;

// loads images from the assets folder that compile copies next to the classes
public final class Assets {
    private Assets() {}

    public static Image image(String name) {
        URL url = Assets.class.getResource("/assets/" + name);
        if (url == null) {
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    public static Font font(String name, int style, float size) {
        URL url = Assets.class.getResource("/assets/fonts/" + name);
        if (url == null) {
            return new Font("SansSerif", style, Math.round(size));
        }
        try (InputStream stream = url.openStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(style, size);
        } catch (FontFormatException | IOException e) {
            return new Font("SansSerif", style, Math.round(size));
        }
    }
}
