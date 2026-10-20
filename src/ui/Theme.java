package ui;

import java.awt.*;
import javax.swing.*;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.RoundedScrollPane;

public final class Theme {
    public static final int CORNER_RADIUS = 24;

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
    public static final Color PRIMARY = new Color(0x6151D9);
    public static final Color STUDY_CELL = new Color(0x3B3B3B);
    private static final Font KUMBH_SANS = Assets.font("KumbhSans.ttf", Font.PLAIN, 14);
    public static final Font TITLE = font(Font.BOLD, 26);
    public static final Font HEADING = font(Font.BOLD, 16);
    public static final Font BODY = font(Font.PLAIN, 14);

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
        } else {
            BG = new Color(0xF5F7FB);
            SURFACE = new Color(0xFFFFFF);
            TEXT = new Color(0x1F2937);
            MUTED = new Color(0x6B7280);
            BORDER = new Color(0xE5E7EB);
        }
        SELECT = PRIMARY;
    }

    // sets the shared defaults so every new control follows the current palette
    public static void apply() {
        UIManager.put("defaultFont", BODY);
        UIManager.put("Label.font", BODY);
        UIManager.put("Button.font", BODY);
        UIManager.put("TextField.font", BODY);
        UIManager.put("TextArea.font", BODY);
        UIManager.put("TextPane.font", BODY);
        UIManager.put("EditorPane.font", BODY);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("List.font", BODY);
        UIManager.put("Table.font", BODY);
        UIManager.put("TableHeader.font", BODY);
        UIManager.put("CheckBox.font", BODY);
        UIManager.put("RadioButton.font", BODY);
        UIManager.put("Menu.font", BODY);
        UIManager.put("MenuItem.font", BODY);
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

    public static Font font(int style, float size) {
        return KUMBH_SANS.deriveFont(style, size);
    }

    public static JPanel card() {
        JPanel panel = new RoundedPanel(SURFACE, CORNER_RADIUS, BORDER);
        panel.setBorder(BorderFactory.createEmptyBorder(17, 17, 17, 17));
        return panel;
    }

    public static JScrollPane inputScrollPane(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        return scroll;
    }

    public static JScrollPane roundedScrollPane(Component view) {
        return new RoundedScrollPane(view);
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
        JButton b = new RoundedButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(BODY);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    public static JButton button(String text) {
        JButton b = new RoundedButton(text);
        b.setBackground(SURFACE);
        b.setForeground(TEXT);
        b.setFont(BODY);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7, 15, 7, 15));
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
