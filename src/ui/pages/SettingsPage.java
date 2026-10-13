package ui.pages;

import ui.Theme;
import ui.components.Avatar;
import ui.components.CardIcons;
import ui.components.ToggleSwitch;

import javax.swing.*;
import java.awt.*;

// Screen 11, settings and accessibility
public class SettingsPage extends JPanel {
    private final Runnable onThemeChange;

    public SettingsPage() {
        this(() -> {});
    }

    public SettingsPage(Runnable onThemeChange) {
        this.onThemeChange = onThemeChange;
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(Theme.header("Settings"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.42;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 16);
        grid.add(profileCard(), c);

        c.gridx = 1;
        c.weightx = 0.58;
        c.insets = new Insets(0, 0, 0, 0);
        grid.add(appearanceCard(), c);

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

    private JPanel appearanceCard() {
        JPanel card = card("Appearance", Theme.HEADER_BLUE, CardIcons.palette());

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel mode = new JLabel("Mode");
        mode.setFont(Theme.HEADING);
        mode.setForeground(Theme.TEXT);
        mode.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton darkButton = new JButton("Dark");
        JButton lightButton = new JButton("Light");
        if (Theme.isDark()) {
            stylePill(darkButton, true);
            stylePill(lightButton, false);
        } else {
            stylePill(darkButton, false);
            stylePill(lightButton, true);
        }
        darkButton.addActionListener(e -> {
            if (!Theme.isDark()) {
                Theme.setDark(true);
                onThemeChange.run();
            }
        });
        lightButton.addActionListener(e -> {
            if (Theme.isDark()) {
                Theme.setDark(false);
                onThemeChange.run();
            }
        });

        JPanel segments = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        segments.setOpaque(false);
        segments.setAlignmentX(Component.LEFT_ALIGNMENT);
        segments.add(darkButton);
        segments.add(lightButton);

        JPanel largeTextRow = row("Large Text", null);
        largeTextRow.add(switchHolder(new ToggleSwitch(true)), BorderLayout.EAST);

        body.add(mode);
        body.add(Box.createVerticalStrut(10));
        body.add(segments);
        body.add(Box.createVerticalStrut(14));
        body.add(line());
        body.add(Box.createVerticalStrut(14));
        body.add(largeTextRow);

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

    private JPanel row(String title, String caption) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(title);
        name.setFont(Theme.HEADING);
        name.setForeground(Theme.TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        texts.add(name);
        if (caption != null) {
            JLabel desc = Theme.muted(caption);
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            texts.add(desc);
        }
        row.add(texts, BorderLayout.CENTER);
        return row;
    }

    private JPanel switchHolder(ToggleSwitch toggle) {
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        holder.setOpaque(false);
        holder.add(toggle);
        return holder;
    }

    private JPanel line() {
        JPanel line = new JPanel();
        line.setBackground(Theme.BORDER);
        line.setPreferredSize(new Dimension(1, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return line;
    }

    private void stylePill(JButton button, boolean active) {
        button.setFont(Theme.BODY);
        button.setFocusPainted(false);
        button.setOpaque(true);
        if (active) {
            button.setBackground(Theme.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(7, 20, 7, 20));
        } else {
            button.setBackground(Theme.BG);
            button.setForeground(Theme.MUTED);
            button.setBorderPainted(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    BorderFactory.createEmptyBorder(6, 19, 6, 19)));
        }
    }
}
