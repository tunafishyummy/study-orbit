package ui.pages;

import ui.Theme;
import ui.components.Avatar;
import ui.components.CardIcons;

import javax.swing.*;
import java.awt.*;

// Screen 11, settings and accessibility
public class SettingsPage extends JPanel {

    public SettingsPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(Theme.header("Settings"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.add(profileCard());
        add(grid, BorderLayout.CENTER);
    }

    private JPanel profileCard() {
        JPanel card = card("Profile", Theme.HEADER_GREEN, CardIcons.person());

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel who = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        who.setOpaque(false);
        who.add(new Avatar());

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("User1");
        name.setFont(Theme.HEADING);
        name.setForeground(Theme.TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = Theme.muted("BSICS - 2nd Year");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        names.add(name);
        names.add(sub);
        who.add(names);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonRow.setOpaque(false);
        buttonRow.add(Theme.primaryButton("Switch Profile"));

        body.add(who, BorderLayout.CENTER);
        body.add(buttonRow, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel card(String title, Color headerColor, Icon icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.SURFACE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        header.setBackground(headerColor);
        JLabel iconLabel = new JLabel(icon);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.HEADING);
        titleLabel.setForeground(Color.WHITE);
        header.add(iconLabel);
        header.add(titleLabel);

        card.add(header, BorderLayout.NORTH);
        return card;
    }
}
