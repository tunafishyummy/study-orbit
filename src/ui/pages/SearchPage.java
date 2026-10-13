package ui.pages;

import java.awt.*;
import javax.swing.*;

public class SearchPage extends JPanel {

    public SearchPage() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Search");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel subtitle = new JLabel("Search what you want like courses, flashcards, notes");

        // search bar
        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchField.getPreferredSize().height));

        // filter buttons
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup group = new ButtonGroup();
        for (String name : new String[]{"All", "Notes", "Flashcards"}) {
            JToggleButton btn = new JToggleButton(name);
            group.add(btn);
            filters.add(btn);
            if (name.equals("All")) btn.setSelected(true);
        }
        filters.add(new JButton("Course: Any"));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{title, subtitle, searchField, filters}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(c);
        }

        add(header, BorderLayout.NORTH);

        JPanel results = new JPanel();
        results.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(results, BorderLayout.CENTER);
    }
}