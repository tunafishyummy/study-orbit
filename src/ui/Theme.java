package ui;

import java.awt.*;
import javax.swing.*;

public final class Theme {
    // palette values, dark is the figma look and light keeps the original colors
    public static Color BG;
    public static Color SURFACE;
    public static Color NAVBAR;
    public static Color NAVBAR_SELECTED;
    public static Color TEXT;
    public static Color MUTED;
    public static Color BORDER;
    public static Color SELECT;
    public static final Color PRIMARY = new Color(0x4F46E5);
    public static final Color SUCCESS = new Color(0x22C55E);
    public static final Color WARNING = new Color(0xF59E0B);
    public static final Color DANGER = new Color(0xEF4444);
    public static final Color HEADER_GREEN = new Color(0x3CB44A);
    public static final Color HEADER_BLUE = new Color(0x7EC8F0);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font HEADING = new Font("SansSerif", Font.BOLD, 16);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);

    private static boolean dark = true;

    private Theme() {}

    static {
        setDark(true);
    }

    public static boolean isDark() {
        return dark;
    }

    public static void setDark(boolean value) {
        dark = value;
        if (dark) {
            BG = new Color(0x171717);
            SURFACE = new Color(0x2D2D2D);
            NAVBAR = new Color(0x27235C);
            NAVBAR_SELECTED = new Color(0x3B3580);
            TEXT = new Color(0xFFFFFF);
            MUTED = new Color(0xA1A1AA);
            BORDER = new Color(0x3F3F46);
            SELECT = new Color(0x3B3580);
        } else {
            BG = new Color(0xF5F7FB);
            SURFACE = new Color(0xFFFFFF);
            NAVBAR = new Color(0xFFFFFF);
            NAVBAR_SELECTED = new Color(0xE0E7FF);
            TEXT = new Color(0x1F2937);
            MUTED = new Color(0x6B7280);
            BORDER = new Color(0xE5E7EB);
            SELECT = new Color(0xE0E7FF);
        }
    }

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
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return PRIMARY;
        }
    }

    public static String toHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
