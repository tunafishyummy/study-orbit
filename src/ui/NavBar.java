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
        setPreferredSize(new Dimension(110, 0));
        setBackground(Theme.NAVBAR);

        JLabel logo = new JLabel();
        Image image = Assets.image("Logo Navbar.png");
        if (image != null) {
            logo.setIcon(new ImageIcon(image.getScaledInstance(56, 56, Image.SCALE_SMOOTH)));
        }
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("Study Orbit");
        name.setFont(Theme.BODY);
        name.setForeground(Theme.TEXT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(10));
        add(logo);
        add(Box.createVerticalStrut(4));
        add(name);
        add(Box.createVerticalStrut(12));

        ButtonGroup group = new ButtonGroup();
        for (String page : PAGES) {
            JToggleButton btn = new JToggleButton(page);
            btn.setForeground(Theme.TEXT);
            btn.setBackground(Theme.NAVBAR);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(94, 35));
            btn.addActionListener(e -> onNavigate.accept(page));
            group.add(btn);
            add(btn);
            add(Box.createVerticalStrut(5));

            if (page.equals("Dashboard")) btn.setSelected(true);
        }
    }
}
