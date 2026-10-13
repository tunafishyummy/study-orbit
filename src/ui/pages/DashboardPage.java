package ui.pages;

import ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Screen 3, the student dashboard
public class DashboardPage extends JPanel {

    private final JLabel dateLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();

    public DashboardPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(header(), BorderLayout.NORTH);
        add(cards(), BorderLayout.CENTER);

        updateClock();
        new Timer(1000, e -> updateClock()).start();
    }

    private JPanel header() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = Theme.header("Dashboard");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel.setFont(Theme.BODY);
        dateLabel.setForeground(Theme.TEXT);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeLabel.setFont(Theme.BODY);
        timeLabel.setForeground(Theme.MUTED);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(dateLabel);
        header.add(timeLabel);
        return header;
    }

    private JPanel cards() {
        JPanel topRow = new JPanel(new GridLayout(1, 3, 16, 16));
        topRow.setOpaque(false);
        topRow.add(section("Next class", lines(
                "CS 002 - Object-Oriented Programming",
                "Mon and Wed 9:00 AM - Room 402")));
        topRow.add(section("Cards due today", lines(
                "23 cards due",
                "About 15 minutes")));
        topRow.add(section("Current Streak", lines(
                "6 day streak",
                "Keep it going")));

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 16));
        bottomRow.setOpaque(false);
        bottomRow.add(deadlines());
        bottomRow.add(section("Recent notes", empty()));

        JPanel rows = new JPanel(new GridLayout(2, 1, 16, 16));
        rows.setOpaque(false);
        rows.add(topRow);
        rows.add(bottomRow);
        return rows;
    }

    private JPanel section(String title, JComponent body) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel lines(String... texts) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (String text : texts) {
            JLabel label = new JLabel(text);
            label.setFont(Theme.BODY);
            label.setForeground(Theme.TEXT);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(label);
            panel.add(Box.createVerticalStrut(4));
        }
        return panel;
    }

    private JPanel empty() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private JPanel deadlines() {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(deadlineRow("OOP Concept Proposal", "CS 002", "Due Friday", Theme.WARNING));
        list.add(Box.createVerticalStrut(8));
        list.add(deadlineRow("Math Problem Set 5", "MATH 008A", "Due Monday", Theme.MUTED));
        list.add(Box.createVerticalStrut(8));
        list.add(deadlineRow("Purposive Comm. Essay", "GEC 002", "Due Wednesday", Theme.MUTED));
        return section("Upcoming deadlines", list);
    }

    private JPanel deadlineRow(String title, String course, String due, Color chipColor) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel name = new JLabel(title);
        name.setFont(Theme.BODY);
        name.setForeground(Theme.TEXT);
        row.add(name, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(chip(course, Theme.PRIMARY));
        right.add(chip(due, chipColor));
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JLabel chip(String text, Color color) {
        JLabel chip = new JLabel(" " + text + " ");
        chip.setFont(Theme.BODY);
        chip.setForeground(Color.WHITE);
        chip.setBackground(color);
        chip.setOpaque(true);
        return chip;
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("h:mm a")));
    }
}
