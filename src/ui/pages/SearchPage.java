package ui.pages;

import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 9, search across notes and flashcards
public class SearchPage extends JPanel {

    public SearchPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(header(), BorderLayout.NORTH);
        add(results(), BorderLayout.CENTER);
    }

    private JPanel header() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = Theme.header("Search");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = Theme.muted("Search what you want like courses, flashcards, notes");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String name : new String[]{"All", "Notes", "Flashcards"}) {
            JToggleButton button = new JToggleButton(name);
            button.setFont(Theme.BODY);
            button.setForeground(Theme.TEXT);
            button.setBackground(Theme.SURFACE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    BorderFactory.createEmptyBorder(5, 14, 5, 14)));
            filters.add(button);
        }
        filters.add(Theme.button("Course: Any"));

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(12));
        header.add(searchField);
        header.add(Box.createVerticalStrut(10));
        header.add(filters);
        return header;
    }

    private JPanel results() {
        JPanel panel = Theme.card();
        panel.setLayout(new BorderLayout());
        panel.add(Theme.muted("Start typing to search your notes."), BorderLayout.NORTH);
        return panel;
    }
}
