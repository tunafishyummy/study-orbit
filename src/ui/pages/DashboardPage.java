package ui.pages;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

public class DashboardPage extends JPanel {

    private final JLabel dateLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();

    public DashboardPage() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top left: title, date, time
        JLabel title = new JLabel("Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(dateLabel);
        header.add(timeLabel);
        add(header, BorderLayout.NORTH);

        updateClock();
        new Timer(1000, e -> updateClock()).start();  // refresh every second

        // first three rectangles: next class, cards due today, current streak
        JPanel topRow = new JPanel(new GridLayout(1, 3, 10, 10));
        topRow.add(section("Next class"));
        topRow.add(section("Cards due today"));
        topRow.add(section("Current Streak"));

        // Second two rectangles: upcoming deadlines, recent notes
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 10, 10));
        bottomRow.add(section("Upcoming deadlines"));
        bottomRow.add(section("Recent notes"));

        JPanel sections = new JPanel(new GridLayout(2, 1, 10, 10));
        sections.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        sections.add(topRow);
        sections.add(bottomRow);
        add(sections, BorderLayout.CENTER);
    }


    private JPanel section(String title) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        box.add(new JLabel(title), BorderLayout.NORTH);
        return box;
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));  // Tuesday, October 13
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("h:mm a")));        // 10:26 AM
    }
}