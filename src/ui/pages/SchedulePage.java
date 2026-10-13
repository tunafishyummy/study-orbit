package ui.pages;

import ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Screen 8, the weekly schedule and the deadline list
public class SchedulePage extends JPanel {
    private final CardLayout views = new CardLayout();
    private final JPanel viewPanel = new JPanel(views);

    public SchedulePage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(header(), BorderLayout.NORTH);

        viewPanel.setOpaque(false);
        viewPanel.add(scheduleCard(), "Schedule");
        viewPanel.add(emptyCard(), "Deadlines");
        add(viewPanel, BorderLayout.CENTER);
    }

    private JPanel header() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = Theme.header("Schedule");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabs.setOpaque(false);
        tabs.setAlignmentX(Component.LEFT_ALIGNMENT);
        JToggleButton scheduleTab = new JToggleButton("Schedule");
        JToggleButton deadlinesTab = new JToggleButton("Deadlines");
        ButtonGroup group = new ButtonGroup();
        group.add(scheduleTab);
        group.add(deadlinesTab);
        scheduleTab.setSelected(true);
        styleTab(scheduleTab);
        styleTab(deadlinesTab);
        scheduleTab.addActionListener(e -> views.show(viewPanel, "Schedule"));
        deadlinesTab.addActionListener(e -> views.show(viewPanel, "Deadlines"));
        tabs.add(scheduleTab);
        tabs.add(deadlinesTab);

        header.add(title);
        header.add(Box.createVerticalStrut(10));
        header.add(tabs);
        return header;
    }

    private JPanel scheduleCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());
        card.add(new WeekGrid(), BorderLayout.CENTER);
        return card;
    }

    private JPanel emptyCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());
        return card;
    }

    private void styleTab(JToggleButton button) {
        button.setFont(Theme.BODY);
        button.setForeground(Theme.TEXT);
        button.setBackground(Theme.SURFACE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
    }

    // the weekly grid painted on one panel so blocks can span hours
    private static class WeekGrid extends JPanel {
        private static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        private final List<Block> blocks = new ArrayList<>();

        WeekGrid() {
            setOpaque(false);
            addBlock("Mon", 9, 10.5, "CS 002", "9:00 - 10:30", new Color(0x2563EB));
            addBlock("Wed", 9, 10.5, "CS 002", "9:00 - 10:30", new Color(0x2563EB));
            addBlock("Tue", 10, 11.5, "MATH 008A", "10:00 - 11:30", new Color(0x0D9488));
            addBlock("Thu", 10, 11.5, "MATH 008A", "10:00 - 11:30", new Color(0x0D9488));
            addBlock("Mon", 13, 14.5, "GEC 002", "1:00 - 2:30", new Color(0x7C3AED));
            addBlock("Wed", 13, 14.5, "GEC 002", "1:00 - 2:30", new Color(0x7C3AED));
            addBlock("Fri", 8, 9, "PE 001", "8:00 - 9:00", new Color(0xD97706));
        }

        private void addBlock(String day, double start, double end, String title, String time, Color color) {
            Block block = new Block();
            block.day = day;
            block.start = start;
            block.end = end;
            block.title = title;
            block.time = time;
            block.color = color;
            blocks.add(block);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int labelWidth = 60;
            int headHeight = 26;
            double dayWidth = (getWidth() - labelWidth) / 6.0;
            double hourHeight = (getHeight() - headHeight) / 11.0;

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(Theme.MUTED);
            for (int d = 0; d < DAYS.length; d++) {
                g2.drawString(DAYS[d], (int) (labelWidth + d * dayWidth) + 8, 16);
            }

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int h = 0; h < 11; h++) {
                int y = (int) (headHeight + h * hourHeight);
                g2.setColor(Theme.MUTED);
                g2.drawString(formatHour(7 + h), 6, y + 14);
                g2.setColor(Theme.BORDER);
                g2.drawLine(labelWidth, y, getWidth(), y);
            }
            for (int d = 0; d <= 6; d++) {
                int x = (int) (labelWidth + d * dayWidth);
                g2.setColor(Theme.BORDER);
                g2.drawLine(x, headHeight, x, getHeight());
            }

            for (Block block : blocks) {
                int x = (int) (labelWidth + indexOf(block.day) * dayWidth) + 4;
                int y = (int) (headHeight + (block.start - 7) * hourHeight) + 2;
                int w = (int) dayWidth - 8;
                int h = (int) ((block.end - block.start) * hourHeight) - 4;
                g2.setColor(block.color);
                g2.fillRoundRect(x, y, w, h, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.drawString(block.title, x + 8, y + 18);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString(block.time, x + 8, y + 34);
            }
        }

        private int indexOf(String day) {
            for (int i = 0; i < DAYS.length; i++) {
                if (DAYS[i].equals(day)) {
                    return i;
                }
            }
            return 0;
        }

        private String formatHour(int hour) {
            if (hour < 12) {
                return hour + ":00";
            }
            if (hour == 12) {
                return "12:00";
            }
            return (hour - 12) + ":00";
        }

        private static class Block {
            String day;
            double start;
            double end;
            String title;
            String time;
            Color color;
        }
    }
}
