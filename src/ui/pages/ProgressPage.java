package ui.pages;

import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 10, streaks and study days
public class ProgressPage extends JPanel {

    public ProgressPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(Theme.header("Progress and Streaks"), BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(2, 1, 16, 16));
        rows.setOpaque(false);

        JPanel row = new JPanel(new GridLayout(1, 3, 16, 16));
        row.setOpaque(false);
        row.add(streakCard());
        row.add(goalCard());
        row.add(weekCard());

        rows.add(row);
        rows.add(studyDaysCard());
        add(rows, BorderLayout.CENTER);
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

    private JPanel studyDaysCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));

        JLabel heading = new JLabel("October study days");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);
        card.add(new StudyGrid(), BorderLayout.CENTER);
        return card;
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

    // the calendar grid with two weeks of day cells
    private static class StudyGrid extends JPanel {
        private static final String[] DAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        private static final String[][] CELLS = {
                {"", "", "", "", "1", "2", "3"},
                {"4", "5", "6", "7", "8", "9", "10"}
        };

        StudyGrid() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int gap = 8;
            double colWidth = (getWidth() - gap * 6) / 7.0;
            int headerHeight = 30;
            int rowHeight = 38;

            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.setColor(Theme.MUTED);
            for (int d = 0; d < DAYS.length; d++) {
                int x = (int) (d * (colWidth + gap));
                g2.drawString(DAYS[d], x + 8, 18);
            }

            for (int r = 0; r < CELLS.length; r++) {
                for (int d = 0; d < DAYS.length; d++) {
                    int x = (int) (d * (colWidth + gap));
                    int y = headerHeight + r * (rowHeight + gap);
                    g2.setColor(Theme.BG);
                    g2.fillRoundRect(x, y, (int) colWidth, rowHeight, 8, 8);

                    String value = CELLS[r][d];
                    if (!value.isEmpty()) {
                        g2.setColor(Theme.MUTED);
                        g2.drawString(value, x + 10, y + 24);
                    }
                }
            }
        }
    }
}
