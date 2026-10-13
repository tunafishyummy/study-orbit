package ui.pages;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

public class DashboardPage extends JPanel {

    private final JLabel dateLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();

    public DashboardPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(dateLabel);
        add(timeLabel);

        updateClock();
        new Timer(1000, e -> updateClock()).start();  // refresh every second
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("h:mm a")));
    }
}