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
 
        // Dashboard, date, and time // top left //
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(new JLabel("Dashboard"));
        header.add(dateLabel);
        header.add(timeLabel);
        add(header, BorderLayout.NORTH);
 
        updateClock();
        new Timer(1000, e -> updateClock()).start();  // refresh every second
        
        // first row // next class, cards due today, current streak //
        JPanel topRow = new JPanel(new GridLayout(1, 3));
        topRow.add(section("Next class"));
        topRow.add(section("Cards due today"));
        topRow.add(section("Current Streak"));
 
        // second row // upcoming deadlines, recent notes //
        JPanel bottomRow = new JPanel(new GridLayout(1, 2));
        bottomRow.add(section("Upcoming deadlines"));
        bottomRow.add(section("Recent notes"));
 
        JPanel sections = new JPanel(new GridLayout(2, 1));
        sections.add(topRow);
        sections.add(bottomRow);
        add(sections, BorderLayout.CENTER);
    }

    private JLabel section(String title) {
        JLabel label = new JLabel(title);
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
    }
 
    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));  // Date
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("h:mm a")));        // Time
    }
}