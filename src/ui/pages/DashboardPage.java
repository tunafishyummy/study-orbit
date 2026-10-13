package ui.pages;

import java.awt.*;
import javax.swing.*;

public class DashboardPage extends JPanel {

    public DashboardPage() {
        setLayout(new BorderLayout());

        add(new JLabel("Dashboard", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
