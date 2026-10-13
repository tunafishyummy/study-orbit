package ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

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
}
