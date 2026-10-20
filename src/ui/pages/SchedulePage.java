package ui.pages;

import ui.Theme;
import ui.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;

// Screen 8, the weekly schedule and the deadline list
public class SchedulePage extends JPanel {
    private final CardLayout views = new CardLayout();
    private final JPanel viewPanel = new JPanel(views);
    private final JLabel title = new JLabel();
    private final JButton schedulePill = pill("Schedule");
    private final JButton deadlinesPill = pill("Deadlines");

    public SchedulePage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(header(), BorderLayout.NORTH);

        viewPanel.setOpaque(false);
        viewPanel.add(scheduleCard(), "Schedule");
        viewPanel.add(deadlinesCard(), "Deadlines");
        add(viewPanel, BorderLayout.CENTER);

        schedulePill.addActionListener(e -> showTab("Schedule"));
        deadlinesPill.addActionListener(e -> showTab("Deadlines"));
        showTab("Schedule");
    }

    private JPanel header() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);
        header.add(title, BorderLayout.WEST);

        JPanel mode = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        mode.setOpaque(false);
        JLabel modeLabel = new JLabel("Mode:");
        modeLabel.setFont(Theme.BODY);
        modeLabel.setForeground(Theme.TEXT);
        mode.add(modeLabel);
        mode.add(schedulePill);
        mode.add(deadlinesPill);
        header.add(mode, BorderLayout.EAST);
        return header;
    }

    private void showTab(String tab) {
        views.show(viewPanel, tab);
        boolean schedule = tab.equals("Schedule");
        if (schedule) {
            title.setText("Schedules");
        } else {
            title.setText("Deadlines");
        }
        stylePill(schedulePill, schedule);
        stylePill(deadlinesPill, !schedule);
    }

    private JButton pill(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.BODY);
        button.setFocusPainted(false);
        button.setOpaque(true);
        return button;
    }

    private void stylePill(JButton button, boolean active) {
        if (active) {
            button.setBackground(Theme.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(7, 20, 7, 20));
        } else {
            button.setBackground(Theme.BG);
            button.setForeground(Theme.TEXT);
            button.setBorderPainted(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    BorderFactory.createEmptyBorder(6, 19, 6, 19)));
        }
    }

    private JButton arrowButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Theme.ARROW);
        button.setForeground(Color.WHITE);
        button.setFont(Theme.BODY);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return button;
    }

    private JPanel scheduleCard() {
        JPanel card = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Semester Class Schedule", SwingConstants.CENTER);
        heading.setFont(Theme.font(Font.BOLD, 26));
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);

        JPanel gridHolder = new RoundedPanel(Color.WHITE, Theme.CORNER_RADIUS);
        gridHolder.setLayout(new BorderLayout());
        gridHolder.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        gridHolder.add(new WeekGrid(), BorderLayout.CENTER);
        card.add(gridHolder, BorderLayout.CENTER);
        return card;
    }

    private JPanel deadlinesCard() {
        JPanel card = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel weekHeader = new JPanel(new BorderLayout());
        weekHeader.setOpaque(false);
        JButton previous = arrowButton("<");
        JButton next = arrowButton(">");
        JLabel range = new JLabel("October 4 - 10, 2026", SwingConstants.CENTER);
        range.setFont(Theme.font(Font.BOLD, 24));
        range.setForeground(Theme.TEXT);
        weekHeader.add(previous, BorderLayout.WEST);
        weekHeader.add(range, BorderLayout.CENTER);
        weekHeader.add(next, BorderLayout.EAST);
        card.add(weekHeader, BorderLayout.NORTH);

        JPanel gridHolder = new RoundedPanel(Color.WHITE, Theme.CORNER_RADIUS);
        gridHolder.setLayout(new BorderLayout());
        gridHolder.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        gridHolder.add(new DeadlineGrid(), BorderLayout.CENTER);
        card.add(gridHolder, BorderLayout.CENTER);
        return card;
    }

    // the empty week grid, painted so the lines line up
    private static class WeekGrid extends JPanel {
        private static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        private static final String[] TIMES = {"7:30 AM", "8:30 AM", "9:30 AM", "10:30 AM", "11:30 AM",
                "12:30 PM", "1:30 PM", "2:30 PM", "3:30 PM"};

        WeekGrid() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int labelWidth = 72;
            int headerHeight = 32;
            double colWidth = (getWidth() - labelWidth) / 7.0;
            double rowHeight = (getHeight() - headerHeight) / 9.0;

            g2.setFont(Theme.font(Font.BOLD, 14));
            g2.setColor(new Color(0x111111));
            for (int d = 0; d < DAYS.length; d++) {
                g2.drawString(DAYS[d], (int) (labelWidth + d * colWidth) + 12, 21);
            }

            g2.setFont(Theme.font(Font.PLAIN, 12));
            g2.setColor(new Color(0x666666));
            for (int r = 0; r < TIMES.length; r++) {
                int y = (int) (headerHeight + r * rowHeight);
                g2.drawString(TIMES[r], 6, y + 15);
            }

            g2.setColor(new Color(0xD9D9D9));
            for (int r = 0; r <= TIMES.length; r++) {
                int y = (int) (headerHeight + r * rowHeight);
                g2.drawLine(labelWidth, y, getWidth(), y);
            }
            for (int d = 0; d <= DAYS.length; d++) {
                int x = (int) (labelWidth + d * colWidth);
                g2.drawLine(x, 0, x, getHeight());
            }
        }
    }

    // the empty deadline week view
    private static class DeadlineGrid extends JPanel {
        private static final String[] DAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        private static final String[] DATES = {"Oct 4", "Oct 5", "Oct 6", "Oct 7", "Oct 8", "Oct 9", "Oct 10"};

        DeadlineGrid() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double colWidth = getWidth() / 7.0;
            for (int d = 0; d < DAYS.length; d++) {
                int x = (int) (d * colWidth);
                g2.setFont(Theme.font(Font.BOLD, 14));
                g2.setColor(new Color(0x111111));
                g2.drawString(DAYS[d], x + 12, 20);
                g2.setFont(Theme.font(Font.PLAIN, 12));
                g2.setColor(new Color(0x666666));
                g2.drawString(DATES[d], x + 12, 38);
            }

            g2.setColor(new Color(0xD9D9D9));
            for (int d = 0; d <= DAYS.length; d++) {
                int x = (int) (d * colWidth);
                g2.drawLine(x, 0, x, getHeight());
            }
        }
    }
}
