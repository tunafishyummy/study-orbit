package ui.pages;

import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 10, streaks and goals
public class ProgressPage extends JPanel {

    public ProgressPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(Theme.header("Progress and Streaks"), BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 3, 16, 16));
        row.setOpaque(false);
        row.add(streakCard());
        row.add(goalCard());
        row.add(weekCard());
        add(row, BorderLayout.CENTER);
    }

    private JPanel streakCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());

        JLabel heading = new JLabel("Current Streak");
        heading.setFont(Theme.BODY);
        heading.setForeground(Theme.MUTED);
        card.add(heading, BorderLayout.NORTH);
        return card;
    }

    private JPanel goalCard() {
        JPanel card = Theme.card();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 10));

        card.add(new EmptyRing());

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel goal = new JLabel("Weekly goal");
        goal.setFont(Theme.HEADING);
        goal.setForeground(Theme.TEXT);
        goal.setAlignmentX(Component.LEFT_ALIGNMENT);
        labels.add(Box.createVerticalStrut(30));
        labels.add(goal);
        card.add(labels);
        return card;
    }

    private JPanel weekCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel heading = new JLabel("This week");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.MUTED);
        card.add(heading, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(row("Cards Reviewed"));
        list.add(Box.createVerticalStrut(8));
        list.add(row("Quizzes Taken"));
        list.add(Box.createVerticalStrut(8));
        list.add(row("Notes Made"));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel row(String label) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel name = new JLabel(label);
        name.setFont(Theme.BODY);
        name.setForeground(Theme.TEXT);
        row.add(name, BorderLayout.WEST);
        return row;
    }

    // the empty goal ring
    private static class EmptyRing extends JComponent {
        EmptyRing() {
            setPreferredSize(new Dimension(96, 96));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 14;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Theme.BORDER);
            g2.drawArc(x, y, size, size, 0, 360);
        }
    }
}
