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
        topRow.add(section("Next class"));
        topRow.add(section("Cards due today"));
        topRow.add(section("Current Streak"));

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 16));
        bottomRow.setOpaque(false);
        bottomRow.add(section("Upcoming deadlines"));
        bottomRow.add(section("Recent notes"));

        JPanel rows = new JPanel(new GridLayout(2, 1, 16, 16));
        rows.setOpaque(false);
        rows.add(topRow);
        rows.add(bottomRow);
        return rows;
    }

    private JPanel section(String title) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);
        return card;
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("h:mm a")));
    }
}
