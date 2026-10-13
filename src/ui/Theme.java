package ui;

import java.awt.*;
import javax.swing.*;

public final class Theme {
    public static final Color BG = new Color(0xF5F7FB);
    public static final Color PRIMARY = new Color(0x4F46E5);
    public static final Color TEXT = new Color(0x1F2937);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font HEADING = new Font("SansSerif", Font.BOLD, 16);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);

    private Theme() {}

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(BODY);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(BODY);
        b.setFocusPainted(false);
        return b;
    }

    public static Color parse(String hex) {
        try { return Color.decode(hex); } catch (Exception e) { return PRIMARY; }
    }

    public static String toHex(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}
