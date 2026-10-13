package ui.pages;

import data.AppData;
import data.NoteManager;
import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 10, streaks stats and a simple chart
public class ProgressPage extends JPanel {

    public ProgressPage() {
        NoteManager manager = AppData.get();
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(Theme.header("Progress"), BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(3, 1, 16, 16));
        rows.setOpaque(false);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 16));
        row1.setOpaque(false);
        row1.add(streakCard());
        row1.add(goalCard());

        JPanel row2 = new JPanel(new GridLayout(1, 3, 16, 16));
        row2.setOpaque(false);
        row2.add(statCard("Cards reviewed", "142"));
        row2.add(statCard("Quizzes taken", "4"));
        row2.add(statCard("Notes created", String.valueOf(manager.getAllNotes().size())));

        JPanel row3 = Theme.card();
        row3.setLayout(new BorderLayout(0, 8));
        JLabel chartTitle = new JLabel("Cards reviewed per day");
        chartTitle.setFont(Theme.HEADING);
        chartTitle.setForeground(Theme.TEXT);
        row3.add(chartTitle, BorderLayout.NORTH);
        row3.add(new BarChart(), BorderLayout.CENTER);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
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

    private JPanel statCard(String title, String value) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 6));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.BODY);
        heading.setForeground(Theme.MUTED);
        JLabel number = new JLabel(value);
        number.setFont(new Font("SansSerif", Font.BOLD, 32));
        number.setForeground(Theme.TEXT);

        card.add(heading, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
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

    // simple bar chart for the week
    private static class BarChart extends JComponent {
        private final int[] values = {8, 14, 6, 18, 12, 4, 10};

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int max = 0;
            for (int value : values) {
                max = Math.max(max, value);
            }
            if (max == 0) {
                return;
            }

            int gap = 12;
            int barWidth = (getWidth() - gap * 6) / 7;
            for (int i = 0; i < values.length; i++) {
                int height = (int) ((getHeight() - 10) * (values[i] / (double) max));
                int x = i * (barWidth + gap);
                int y = getHeight() - height;
                if (i == values.length - 1) {
                    g2.setColor(Theme.PRIMARY);
                } else {
                    g2.setColor(Theme.NAVBAR_SELECTED);
                }
                g2.fillRoundRect(x, y, barWidth, height, 6, 6);
            }
        }
    }
}
