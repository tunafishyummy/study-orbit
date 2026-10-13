package ui.pages;

import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 10, streaks stats and a simple chart
public class ProgressPage extends JPanel {

    public ProgressPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(Theme.header("Progress"), BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(1, 1, 16, 16));
        rows.setOpaque(false);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 16));
        row1.setOpaque(false);
        row1.add(streakCard());
        row1.add(goalCard());

        rows.add(row1);
        add(rows, BorderLayout.CENTER);
    }

    private JPanel streakCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));

        JLabel heading = new JLabel("Current streak");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        JLabel value = new JLabel("6 day streak");
        value.setFont(Theme.TITLE);
        value.setForeground(Theme.TEXT);

        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(new DayDots(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel goalCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));

        JLabel heading = new JLabel("Weekly goal");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);
        card.add(new GoalRing(5, 7), BorderLayout.CENTER);
        return card;
    }

    // seven day dots with the first three filled
    private static class DayDots extends JComponent {
        DayDots() {
            setPreferredSize(new Dimension(120, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 7; i++) {
                if (i < 3) {
                    g2.setColor(Theme.PRIMARY);
                } else {
                    g2.setColor(Theme.BORDER);
                }
                g2.fillOval(i * 16, 4, 10, 10);
            }
        }
    }

    // the weekly goal ring
    private static class GoalRing extends JComponent {
        private final int done;
        private final int total;

        GoalRing(int done, int total) {
            this.done = done;
            this.total = total;
            setPreferredSize(new Dimension(120, 120));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 16;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.setColor(Theme.BORDER);
            g2.drawArc(x, y, size, size, 0, 360);

            int angle = (int) Math.round(360.0 * done / total);
            g2.setColor(Theme.PRIMARY);
            g2.drawArc(x, y, size, size, 90, -angle);

            String text = done + " / " + total;
            g2.setFont(Theme.HEADING);
            g2.setColor(Theme.TEXT);
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - metrics.stringWidth(text)) / 2, getHeight() / 2 + 6);
        }
    }
}
