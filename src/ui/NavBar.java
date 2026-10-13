package ui;

import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

public class NavBar extends JPanel {

    // Same order as the sidebar in the design
    public static final String[] PAGES = {
        "Dashboard", "Courses", "Notes", "Flashcards", "Quizzes",
        "Schedule", "Search", "Progress", "Settings"
    };

    public NavBar(Consumer<String> onNavigate) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(100, 0));

        JLabel logo = new JLabel("Study Orbit");
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(10));
        add(logo);
        add(Box.createVerticalStrut(15));

        ButtonGroup group = new ButtonGroup();
        for (String page : PAGES) {
            JToggleButton btn = new JToggleButton(page);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(90, 35));
            btn.addActionListener(e -> onNavigate.accept(page));
            group.add(btn);
            add(btn);
            add(Box.createVerticalStrut(5));

            if (page.equals("Dashboard")) btn.setSelected(true);
        }
    }
}
