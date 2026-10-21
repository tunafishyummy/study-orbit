package ui.pages;

import data.ActivityManager;
import data.AppData;
import ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

// Screen 10, streaks and study days
public class ProgressPage extends JPanel {

    private final JLabel streakValue = new JLabel();
    private final JLabel longestValue = new JLabel();
    private final JLabel goalCaption = new JLabel();
    private final GoalRing goalRing = new GoalRing();
    private final JLabel cardsValue = new JLabel();
    private final JLabel quizzesValue = new JLabel();
    private final JLabel notesValue = new JLabel();
    private final MonthGrid monthGrid = new MonthGrid();

    public ProgressPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        add(Theme.header("Progress and Streaks"), BorderLayout.NORTH);

        JPanel row1 = new JPanel(new GridLayout(1, 3, 16, 16));
        row1.setOpaque(false);
        row1.add(valueCard("Current streak", streakValue, "days in a row"));
        row1.add(valueCard("Longest streak", longestValue, "personal best"));
        row1.add(goalCard());

        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 16));
        row2.setOpaque(false);
        row2.add(thisWeekCard());
        row2.add(monthCard());

        JPanel rows = new JPanel(new GridLayout(2, 1, 16, 16));
        rows.setOpaque(false);
        rows.add(row1);
        rows.add(row2);
        add(rows, BorderLayout.CENTER);

        refresh();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                refresh();
            }
        });
    }

    private JPanel valueCard(String title, JLabel value, String caption) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.BODY);
        heading.setForeground(Theme.MUTED);
        card.add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        value.setFont(Theme.font(Font.BOLD, 22));
        value.setForeground(Theme.TEXT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(value);

        JLabel captionLabel = Theme.muted(caption);
        captionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createVerticalStrut(4));
        body.add(captionLabel);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel goalCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(10, 8));

        JLabel heading = new JLabel("Weekly goal");
        heading.setFont(Theme.BODY);
        heading.setForeground(Theme.MUTED);
        card.add(heading, BorderLayout.NORTH);

        card.add(goalRing, BorderLayout.CENTER);

        goalCaption.setFont(Theme.BODY);
        goalCaption.setForeground(Theme.TEXT);
        card.add(goalCaption, BorderLayout.SOUTH);
        return card;
    }

    private JPanel thisWeekCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel heading = new JLabel("This week");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(statRow("Cards reviewed", cardsValue));
        list.add(Box.createVerticalStrut(10));
        list.add(statRow("Quizzes taken", quizzesValue));
        list.add(Box.createVerticalStrut(10));
        list.add(statRow("Notes made", notesValue));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel monthCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel heading = new JLabel("Study days this month");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);
        card.add(monthGrid, BorderLayout.CENTER);
        return card;
    }

    private JPanel statRow(String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel name = new JLabel(label);
        name.setFont(Theme.BODY);
        name.setForeground(Theme.MUTED);
        row.add(name, BorderLayout.WEST);

        value.setFont(Theme.font(Font.BOLD, 16));
        value.setForeground(Theme.TEXT);
        row.add(value, BorderLayout.EAST);

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        return row;
    }

    private void refresh() {
        ActivityManager activity = AppData.activity();
        int goal = AppData.settings().getInt(data.SettingsManager.WEEKLY_GOAL, 5);
        int done = activity.getStudyDaysThisWeek();
        if (goal < 1) {
            goal = 1;
        }

        streakValue.setText(activity.getCurrentStreak() + " days");
        longestValue.setText(activity.getLongestStreak() + " days");
        goalRing.setValues(done, goal);
        goalCaption.setText(done + " of " + goal + " days this week");
        cardsValue.setText(Integer.toString(activity.getCardsThisWeek()));
        quizzesValue.setText(Integer.toString(activity.getQuizzesThisWeek()));
        notesValue.setText(Integer.toString(activity.getNotesThisWeek()));

        Set<LocalDate> activeDates = new HashSet<LocalDate>();
        LocalDate today = LocalDate.now();
        LocalDate first = today.withDayOfMonth(1);
        LocalDate cursor = first;
        while (cursor.isAfter(today) == false) {
            if (activity.isActive(cursor)) {
                activeDates.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        monthGrid.setActiveDates(activeDates);
    }

    private static class GoalRing extends JComponent {
        private int done;
        private int goal = 1;

        GoalRing() {
            setPreferredSize(new Dimension(96, 96));
        }

        void setValues(int done, int goal) {
            this.done = done;
            this.goal = goal;
            repaint();
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

            int angle = (int) Math.round(360.0 * done / goal);
            if (angle > 360) {
                angle = 360;
            }
            g2.setColor(Theme.PRIMARY);
            g2.drawArc(x, y, size, size, 90, -angle);

            g2.setFont(Theme.font(Font.BOLD, 18));
            g2.setColor(Theme.TEXT);
            String text = done + "/" + goal;
            FontMetrics metrics = g2.getFontMetrics();
            int textX = (getWidth() - metrics.stringWidth(text)) / 2;
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(text, textX, textY);
        }
    }

    private static class MonthGrid extends JComponent {
        private Set<LocalDate> activeDates = new HashSet<LocalDate>();

        MonthGrid() {
            setOpaque(false);
        }

        void setActiveDates(Set<LocalDate> activeDates) {
            this.activeDates = activeDates;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            LocalDate today = LocalDate.now();
            LocalDate first = today.withDayOfMonth(1);
            int days = first.lengthOfMonth();
            int offset = first.getDayOfWeek().getValue() % 7;
            int rows = (offset + days + 6) / 7;
            String[] names = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

            int gap = 6;
            double cellWidth = (getWidth() - gap * 6) / 7.0;
            int headerHeight = 24;
            double cellHeight = (getHeight() - headerHeight - gap * (rows - 1)) / (double) rows;

            g2.setFont(Theme.font(Font.BOLD, 12));
            g2.setColor(Theme.MUTED);
            for (int index = 0; index < names.length; index++) {
                g2.drawString(names[index], (int) (index * (cellWidth + gap)) + 6, 15);
            }

            g2.setFont(Theme.font(Font.PLAIN, 12));
            for (int day = 1; day <= days; day++) {
                int slot = offset + day - 1;
                int row = slot / 7;
                int column = slot % 7;
                int x = (int) (column * (cellWidth + gap));
                int y = (int) (headerHeight + row * (cellHeight + gap));
                LocalDate date = first.withDayOfMonth(day);

                if (activeDates.contains(date)) {
                    g2.setColor(Theme.PRIMARY);
                } else {
                    g2.setColor(Theme.STUDY_CELL);
                }
                g2.fillRoundRect(x, y, (int) cellWidth, (int) cellHeight, 6, 6);

                if (activeDates.contains(date)) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(0xCFCFCF));
                }
                g2.drawString(Integer.toString(day), x + 6, y + (int) (cellHeight / 2) + 4);
            }
        }
    }
}
