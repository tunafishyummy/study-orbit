package ui.pages;

import data.ActivityManager;
import data.AppData;
import ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

// Screen 10, streaks and study days
public class ProgressPage extends JPanel {

    private final JLabel streakValue = new JLabel();
    private final JLabel longestCaption = new JLabel();
    private final JLabel goalCaption = new JLabel();
    private final GoalRing goalRing = new GoalRing();
    private final JLabel cardsValue = new JLabel();
    private final JLabel quizzesValue = new JLabel();
    private final JLabel notesValue = new JLabel();
    private final JLabel monthTitle = new JLabel();
    private final MonthGrid monthGrid = new MonthGrid();

    public ProgressPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        add(Theme.header("Progress and Streaks"), BorderLayout.NORTH);

        JPanel row1 = new JPanel(new GridLayout(1, 3, 16, 16));
        row1.setOpaque(false);
        row1.add(streakCard());
        row1.add(goalCard());
        row1.add(thisWeekCard());

        JPanel rows = new JPanel(new BorderLayout(0, 16));
        rows.setOpaque(false);
        rows.add(row1, BorderLayout.NORTH);
        rows.add(monthCard(), BorderLayout.CENTER);
        add(rows, BorderLayout.CENTER);

        refresh();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                refresh();
            }
        });
    }

    private JPanel streakCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));

        JLabel heading = new JLabel("Current Streak");
        heading.setFont(Theme.BODY);
        heading.setForeground(Theme.MUTED);
        card.add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        streakValue.setFont(Theme.font(Font.BOLD, 30));
        streakValue.setForeground(Theme.TEXT);
        streakValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(streakValue);

        longestCaption.setFont(Theme.BODY);
        longestCaption.setForeground(Theme.MUTED);
        longestCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createVerticalStrut(6));
        body.add(longestCaption);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel goalCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));

        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        body.setOpaque(false);
        body.add(goalRing);

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Weekly goal");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        labels.add(Box.createVerticalStrut(24));
        labels.add(title);

        goalCaption.setFont(Theme.BODY);
        goalCaption.setForeground(Theme.MUTED);
        goalCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        labels.add(Box.createVerticalStrut(4));
        labels.add(goalCaption);

        body.add(labels);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel thisWeekCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel heading = new JLabel("This week");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.MUTED);
        card.add(heading, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(statRow("Cards Reviewed", cardsValue));
        list.add(Box.createVerticalStrut(10));
        list.add(statRow("Quizzes Taken", quizzesValue));
        list.add(Box.createVerticalStrut(10));
        list.add(statRow("Notes Made", notesValue));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel monthCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        monthTitle.setFont(Theme.HEADING);
        monthTitle.setForeground(Theme.TEXT);
        card.add(monthTitle, BorderLayout.NORTH);

        monthGrid.setPreferredSize(new Dimension(0, 150));
        monthGrid.setMinimumSize(new Dimension(0, 150));
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(monthGrid, BorderLayout.NORTH);
        card.add(holder, BorderLayout.CENTER);
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
        if (goal < 1) {
            goal = 1;
        }
        int done = activity.getStudyDaysThisWeek();

        streakValue.setText(activity.getCurrentStreak() + " Days");
        longestCaption.setText("Longest: " + activity.getLongestStreak() + " Days");
        goalRing.setValues(done, goal);
        goalCaption.setText("Study " + done + " of " + goal + " days");
        cardsValue.setText(Integer.toString(activity.getCardsThisWeek()));
        quizzesValue.setText(Integer.toString(activity.getQuizzesThisWeek()));
        notesValue.setText(Integer.toString(activity.getNotesThisWeek()));

        LocalDate today = LocalDate.now();
        monthTitle.setText(today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " study days");

        Set<LocalDate> activeDates = new HashSet<LocalDate>();
        LocalDate cursor = today.withDayOfMonth(1);
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
            setPreferredSize(new Dimension(110, 110));
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

            int size = Math.min(getWidth(), getHeight()) - 16;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Theme.STUDY_CELL);
            g2.drawArc(x, y, size, size, 0, 360);

            int angle = (int) Math.round(360.0 * done / goal);
            if (angle > 360) {
                angle = 360;
            }
            g2.setColor(Theme.PRIMARY);
            g2.drawArc(x, y, size, size, 90, -angle);

            g2.setFont(Theme.font(Font.BOLD, 20));
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

            int gap = 8;
            double cellWidth = (getWidth() - gap * 6) / 7.0;
            int headerHeight = 30;
            double cellHeight = (getHeight() - headerHeight - gap * (rows - 1)) / (double) rows;

            g2.setFont(Theme.font(Font.PLAIN, 14));
            g2.setColor(Theme.MUTED);
            for (int index = 0; index < names.length; index++) {
                int textWidth = g2.getFontMetrics().stringWidth(names[index]);
                int center = (int) (index * (cellWidth + gap) + cellWidth / 2) - textWidth / 2;
                g2.drawString(names[index], center, 18);
            }

            g2.setFont(Theme.font(Font.PLAIN, 13));
            for (int day = 1; day <= days; day++) {
                int slot = offset + day - 1;
                int row = slot / 7;
                int column = slot % 7;
                int x = (int) (column * (cellWidth + gap));
                int y = (int) (headerHeight + row * (cellHeight + gap));
                LocalDate date = first.withDayOfMonth(day);

                boolean active = activeDates.contains(date);
                if (active) {
                    g2.setColor(Theme.PRIMARY);
                } else {
                    g2.setColor(Theme.STUDY_CELL);
                }
                g2.fillRoundRect(x, y, (int) cellWidth, (int) cellHeight, 6, 6);

                String text = Integer.toString(day);
                int textWidth = g2.getFontMetrics().stringWidth(text);
                int center = x + (int) cellWidth / 2 - textWidth / 2;
                int baseline = y + (int) cellHeight / 2 + 5;
                if (active) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(0xCFCFCF));
                }
                g2.drawString(text, center, baseline);
            }
        }
    }
}
