package ui;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.RoundedScrollPane;

public final class Theme {
    public static final int CORNER_RADIUS = 24;

    // Fixed dark palette.
    public static final Color BG = new Color(0x171717);
    public static final Color SURFACE = new Color(0x2D2D2D);
    public static final Color TEXT = new Color(0xFFFFFF);
    public static final Color MUTED = new Color(0xA1A1AA);
    public static final Color BORDER = new Color(0x3F3F46);
    public static final Color NAVBAR = new Color(0x27235C);
    public static final Color NAVBAR_TEXT = Color.WHITE;
    public static final Color PRIMARY = new Color(0x6151D9);
    public static final Color SELECT = PRIMARY;
    public static final Color STUDY_CELL = new Color(0x3B3B3B);
    public static final Color HEADER_BLUE = new Color(0x7EC8F0);
    public static final Color[] COURSE_PALETTE = {
            new Color(0x6151D9),
            new Color(0x0D9488),
            new Color(0xD97706),
            new Color(0xDC2626),
            new Color(0xDB2777),
            new Color(0x16A34A),
            new Color(0x2563EB),
            new Color(0x0891B2),
            new Color(0x65A30D),
            new Color(0xEA580C),
            new Color(0x7C3AED),
            new Color(0x64748B)
    };
    private static final Font KUMBH_SANS = Assets.font("KumbhSans.ttf", Font.PLAIN, 14);
    private static boolean largeText;
    private static boolean time24;
    public static Font TITLE = font(Font.BOLD, 26);
    public static Font HEADING = font(Font.BOLD, 16);
    public static Font BODY = font(Font.PLAIN, 14);

    private Theme() {}

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
        float actual = size;
        if (largeText) {
            actual = size * 1.2f;
        }
        return KUMBH_SANS.deriveFont(style, actual);
    }

    public static void setLargeText(boolean value) {
        largeText = value;
        TITLE = font(Font.BOLD, 26);
        HEADING = font(Font.BOLD, 16);
        BODY = font(Font.PLAIN, 14);
    }

    public static boolean isLargeText() {
        return largeText;
    }

    public static void setTime24(boolean value) {
        time24 = value;
    }

    public static String formatTime(LocalTime time) {
        if (time24) {
            return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return time.format(DateTimeFormatter.ofPattern("h:mm a"));
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

    public static Color nextCourseColor(List<String> usedHex) {
        boolean[] used = new boolean[COURSE_PALETTE.length];
        for (int index = 0; index < usedHex.size(); index++) {
            Color color = parse(usedHex.get(index));
            for (int paletteIndex = 0; paletteIndex < COURSE_PALETTE.length; paletteIndex++) {
                if (COURSE_PALETTE[paletteIndex].equals(color)) {
                    used[paletteIndex] = true;
                }
            }
        }
        for (int index = 0; index < used.length; index++) {
            if (used[index] == false) {
                return COURSE_PALETTE[index];
            }
        }
        return COURSE_PALETTE[usedHex.size() % COURSE_PALETTE.length];
    }

    public static String toHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
