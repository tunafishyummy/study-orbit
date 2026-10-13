package ui;

import java.awt.*;
import javax.swing.*;

public final class Theme {
    // palette values, dark is the figma look and light keeps the original colors
    public static Color BG;
    public static Color SURFACE;
    public static Color TEXT;
    public static Color MUTED;
    public static Color BORDER;
    public static Color SELECT;
    public static final Color NAVBAR = new Color(0x27235C);
    public static final Color NAVBAR_SELECTED = new Color(0x3B3580);
    public static final Color NAVBAR_TEXT = Color.WHITE;
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
            TEXT = new Color(0xFFFFFF);
            MUTED = new Color(0xA1A1AA);
            BORDER = new Color(0x3F3F46);
            SELECT = new Color(0x3B3580);
        } else {
            BG = new Color(0xF5F7FB);
            SURFACE = new Color(0xFFFFFF);
            TEXT = new Color(0x1F2937);
            MUTED = new Color(0x6B7280);
            BORDER = new Color(0xE5E7EB);
            SELECT = new Color(0xE0E7FF);
        }
    }

    // sets the shared defaults so every new control follows the current palette
    public static void apply() {
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.caretForeground", TEXT);
        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("List.background", SURFACE);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("Viewport.background", SURFACE);
        UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("ToggleButton.select", SELECT);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    public static JLabel header(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(MUTED);
        return label;
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
        b.setBackground(SURFACE);
        b.setForeground(TEXT);
        b.setFont(BODY);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 15, 7, 15)));
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
