package ui.pages;

import ui.Theme;
import ui.components.Avatar;
import ui.components.CardIcons;
import ui.components.RoundedPanel;
import ui.components.ToggleSwitch;

import javax.swing.*;
import java.awt.*;

// Screen 11, settings and accessibility
public class SettingsPage extends JPanel {
    private final Runnable onThemeChange;

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
        c.weighty = 0.55;
        c.insets = new Insets(0, 0, 16, 16);
        grid.add(profileCard(), c);

        c.gridx = 1;
        c.weightx = 0.58;
        c.insets = new Insets(0, 0, 16, 0);
        grid.add(appearanceCard(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0.45;
        c.gridwidth = 2;
        c.insets = new Insets(0, 0, 0, 0);
        grid.add(notificationsCard(), c);

        add(grid, BorderLayout.CENTER);
    }

    private JPanel profileCard() {
        JPanel card = card("Profile", CardIcons.person());

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel who = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        who.setOpaque(false);
        who.add(new Avatar());

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("User");
        name.setFont(Theme.HEADING);
        name.setForeground(Theme.TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = Theme.muted("College Level");
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
        JPanel card = card("Appearance", CardIcons.palette());

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

    private JPanel notificationsCard() {
        JPanel card = card("Notifications", CardIcons.bell());

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        JPanel deadlineRow = row("Deadline Reminders", "Get notified on upcoming deadlines.");
        deadlineRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        deadlineRow.add(switchHolder(new ToggleSwitch(true)), BorderLayout.EAST);

        JPanel reviewRow = row("Daily Review Reminders", "Get notified on flashcards, notes, and upcoming classes");
        reviewRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        reviewRow.add(switchHolder(new ToggleSwitch(true)), BorderLayout.EAST);

        body.add(deadlineRow);
        body.add(line());
        body.add(reviewRow);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel card(String title, Icon icon) {
        JPanel card = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JPanel header = new RoundedTopPanel(Theme.PRIMARY);
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JLabel iconLabel = new JLabel(icon);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.HEADING);
        titleLabel.setForeground(Color.WHITE);
        header.add(iconLabel);
        header.add(titleLabel);

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private static class RoundedTopPanel extends JPanel {
        private final Color fill;

        RoundedTopPanel(Color fill) {
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            int corner = Theme.CORNER_RADIUS / 2;
            g2.fillRoundRect(0, 0, getWidth(), getHeight() + Theme.CORNER_RADIUS,
                    Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
            g2.fillRect(0, corner, getWidth(), getHeight() - corner);
            g2.dispose();
            super.paintComponent(g);
        }
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
