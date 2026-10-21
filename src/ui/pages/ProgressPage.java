package ui.pages;

import data.ActivityManager;
import data.AppData;
import data.SettingsManager;
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
    private final GoalRing goalRing = new GoalRing();
    private final JLabel goalCaption = new JLabel();
    private final JLabel cardsValue = new JLabel();
    private final JLabel quizzesValue = new JLabel();
    private final JLabel notesValue = new JLabel();
    private final JLabel monthHeading = new JLabel();
    private final StudyGrid studyGrid = new StudyGrid();

    public ProgressPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

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
        card.setLayout(new BorderLayout());

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
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 10));

        card.add(goalRing);

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

        JLabel goal = new JLabel("Weekly goal");
        goal.setFont(Theme.HEADING);
        goal.setForeground(Theme.TEXT);
        goal.setAlignmentX(Component.LEFT_ALIGNMENT);
        labels.add(Box.createVerticalStrut(30));
        labels.add(goal);

        goalCaption.setFont(Theme.BODY);
        goalCaption.setForeground(Theme.MUTED);
        goalCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        labels.add(Box.createVerticalStrut(4));
        labels.add(goalCaption);

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
        list.add(statRow("Cards Reviewed", cardsValue));
        list.add(Box.createVerticalStrut(8));
        list.add(statRow("Quizzes Taken", quizzesValue));
        list.add(Box.createVerticalStrut(8));
        list.add(statRow("Notes Made", notesValue));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel statRow(String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel name = new JLabel(label);
        name.setFont(Theme.BODY);
        name.setForeground(Theme.TEXT);
        row.add(name, BorderLayout.WEST);

        value.setFont(Theme.font(Font.BOLD, 16));
        value.setForeground(Theme.TEXT);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JPanel studyDaysCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));

        monthHeading.setFont(Theme.HEADING);
        monthHeading.setForeground(Theme.TEXT);
        card.add(monthHeading, BorderLayout.NORTH);
        card.add(studyGrid, BorderLayout.CENTER);
        return card;
    }

    private void refresh() {
        ActivityManager activity = AppData.activity();
        int goal = AppData.settings().getInt(SettingsManager.WEEKLY_GOAL, 5);
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
        monthHeading.setText(today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " study days");

        Set<LocalDate> activeDates = new HashSet<LocalDate>();
        LocalDate cursor = today.withDayOfMonth(1);
        while (cursor.isAfter(today) == false) {
            if (activity.isActive(cursor)) {
                activeDates.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        studyGrid.showMonth(today, activeDates);
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

    private static class StudyGrid extends JPanel {
        private static final String[] DAYS = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

        private LocalDate first = LocalDate.now().withDayOfMonth(1);
        private Set<LocalDate> activeDates = new HashSet<LocalDate>();

        StudyGrid() {
            setOpaque(false);
        }

        void showMonth(LocalDate anyDay, Set<LocalDate> activeDates) {
            this.first = anyDay.withDayOfMonth(1);
            this.activeDates = activeDates;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int days = first.lengthOfMonth();
            int offset = first.getDayOfWeek().getValue() % 7;
            int rows = (offset + days + 6) / 7;

            int gap = 8;
            double colWidth = (getWidth() - gap * 6) / 7.0;
            int headerHeight = 30;
            int rowHeight = 40;
            int available = (getHeight() - headerHeight - gap * (rows - 1)) / rows;
            if (available < rowHeight) {
                rowHeight = available;
            }
            if (rowHeight < 24) {
                rowHeight = 24;
            }

            g2.setFont(Theme.font(Font.PLAIN, 13));
            g2.setColor(Theme.MUTED);
            for (int index = 0; index < DAYS.length; index++) {
                int x = (int) (index * (colWidth + gap));
                g2.drawString(DAYS[index], x + 8, 18);
            }

            for (int day = 1; day <= days; day++) {
                int slot = offset + day - 1;
                int row = slot / 7;
                int column = slot % 7;
                int x = (int) (column * (colWidth + gap));
                int y = headerHeight + row * (rowHeight + gap);
                LocalDate date = first.withDayOfMonth(day);

                if (activeDates.contains(date)) {
                    g2.setColor(Theme.PRIMARY);
                } else {
                    g2.setColor(Theme.STUDY_CELL);
                }
                g2.fillRoundRect(x, y, (int) colWidth, rowHeight, 6, 6);

                if (activeDates.contains(date)) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(0xCFCFCF));
                }
                g2.drawString(Integer.toString(day), x + 10, y + 25);
            }
        }
    }
}
