package ui.pages;

import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 8, the weekly schedule and the deadline list
public class SchedulePage extends JPanel {
    private final CardLayout views = new CardLayout();
    private final JPanel viewPanel = new JPanel(views);

    public SchedulePage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(header(), BorderLayout.NORTH);

        viewPanel.setOpaque(false);
        viewPanel.add(emptyCard(), "Schedule");
        viewPanel.add(emptyCard(), "Deadlines");
        add(viewPanel, BorderLayout.CENTER);
    }

    private JPanel header() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = Theme.header("Schedule");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabs.setOpaque(false);
        tabs.setAlignmentX(Component.LEFT_ALIGNMENT);
        JToggleButton scheduleTab = new JToggleButton("Schedule");
        JToggleButton deadlinesTab = new JToggleButton("Deadlines");
        ButtonGroup group = new ButtonGroup();
        group.add(scheduleTab);
        group.add(deadlinesTab);
        scheduleTab.setSelected(true);
        styleTab(scheduleTab);
        styleTab(deadlinesTab);
        scheduleTab.addActionListener(e -> views.show(viewPanel, "Schedule"));
        deadlinesTab.addActionListener(e -> views.show(viewPanel, "Deadlines"));
        tabs.add(scheduleTab);
        tabs.add(deadlinesTab);

        header.add(title);
        header.add(Box.createVerticalStrut(10));
        header.add(tabs);
        return header;
    }

    private JPanel emptyCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());
        return card;
    }

    private void styleTab(JToggleButton button) {
        button.setFont(Theme.BODY);
        button.setForeground(Theme.TEXT);
        button.setBackground(Theme.SURFACE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
    }
}
