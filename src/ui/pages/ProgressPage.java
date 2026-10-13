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
        rows.add(streakCard());
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
}
