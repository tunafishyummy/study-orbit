package ui.pages;

import data.ActivityManager;
import data.AppData;
import data.NoteManager;
import data.ScheduleManager;
import model.Deadline;
import model.Note;
import model.ScheduleClass;
import ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// Screen 3, the student dashboard
public class DashboardPage extends JPanel {

    private final JLabel dateLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();

    private final JLabel nextClassValue = new JLabel();
    private final JLabel nextClassDetail = new JLabel();
    private final JLabel cardsDueValue = new JLabel();
    private final JLabel cardsDueDetail = new JLabel();
    private final JLabel streakValue = new JLabel();
    private final JLabel streakDetail = new JLabel();
    private final JPanel deadlinesList = new JPanel();
    private final JPanel notesList = new JPanel();

    public DashboardPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        add(header(), BorderLayout.NORTH);
        add(cards(), BorderLayout.CENTER);

        updateClock();
        new Timer(1000, e -> updateClock()).start();
        refresh();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                refresh();
            }
        });
    }

    private JPanel header() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = Theme.header("Dashboard");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel.setFont(Theme.BODY);
        dateLabel.setForeground(Theme.TEXT);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeLabel.setFont(Theme.BODY);
        timeLabel.setForeground(Theme.MUTED);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(dateLabel);
        header.add(timeLabel);
        return header;
    }

    private JPanel cards() {
        JPanel topRow = new JPanel(new GridLayout(1, 3, 16, 16));
        topRow.setOpaque(false);
        topRow.add(valueCard("Next class", nextClassValue, nextClassDetail));
        topRow.add(valueCard("Cards due today", cardsDueValue, cardsDueDetail));
        topRow.add(valueCard("Current streak", streakValue, streakDetail));

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 16));
        bottomRow.setOpaque(false);
        bottomRow.add(listCard("Upcoming deadlines", deadlinesList));
        bottomRow.add(listCard("Recent notes", notesList));

        JPanel rows = new JPanel(new GridLayout(2, 1, 16, 16));
        rows.setOpaque(false);
        rows.add(topRow);
        rows.add(bottomRow);
        return rows;
    }

    private JPanel valueCard(String title, JLabel value, JLabel detail) {
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

        detail.setFont(Theme.BODY);
        detail.setForeground(Theme.MUTED);
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createVerticalStrut(4));
        body.add(detail);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel listCard(String title, JPanel list) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private void refresh() {
        NoteManager notes = AppData.get();
        ActivityManager activity = AppData.activity();
        ScheduleManager schedule = AppData.schedule();

        String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        ScheduleClass nextClass = null;
        List<ScheduleClass> classes = schedule.getClassesForDay(today);
        for (int index = 0; index < classes.size(); index++) {
            if (classes.get(index).getEnd().isAfter(LocalTime.now())) {
                nextClass = classes.get(index);
                break;
            }
        }
        if (nextClass == null) {
            nextClassValue.setText("No classes today");
            nextClassDetail.setText("Nothing on the timetable");
        } else {
            nextClassValue.setText(nextClass.getCourseCode());
            nextClassDetail.setText(formatTime(nextClass.getStart()) + " - " + formatTime(nextClass.getEnd())
                    + "  " + nextClass.getRoom());
        }

        int totalCards = notes.getFlashcards().size();
        int reviewedToday = activity.getForDate(LocalDate.now()).getCardsReviewed();
        int due = totalCards - reviewedToday;
        if (due < 0) {
            due = 0;
        }
        cardsDueValue.setText(Integer.toString(due));
        if (due == 0) {
            cardsDueDetail.setText("All caught up");
        } else {
            cardsDueDetail.setText("cards to review today");
        }

        streakValue.setText(activity.getCurrentStreak() + " days");
        streakDetail.setText("Longest " + activity.getLongestStreak() + " days");

        deadlinesList.removeAll();
        List<Deadline> upcoming = schedule.getUpcoming(3);
        if (upcoming.isEmpty()) {
            deadlinesList.add(muted("No deadlines yet"));
        }
        for (int index = 0; index < upcoming.size(); index++) {
            Deadline deadline = upcoming.get(index);
            JLabel row = new JLabel(deadline.getTitle() + "  -  " + deadline.getDueDate());
            row.setFont(Theme.BODY);
            row.setForeground(Theme.TEXT);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            deadlinesList.add(row);
            deadlinesList.add(Box.createVerticalStrut(6));
        }
        deadlinesList.revalidate();
        deadlinesList.repaint();

        notesList.removeAll();
        List<Note> recent = new ArrayList<Note>(notes.getAllNotes());
        recent.sort(Comparator.comparing(Note::getEdited).reversed());
        int limit = recent.size();
        if (limit > 4) {
            limit = 4;
        }
        if (limit == 0) {
            notesList.add(muted("No notes yet"));
        }
        for (int index = 0; index < limit; index++) {
            JLabel row = new JLabel(recent.get(index).getTitle());
            row.setFont(Theme.BODY);
            row.setForeground(Theme.TEXT);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            notesList.add(row);
            notesList.add(Box.createVerticalStrut(6));
        }
        notesList.revalidate();
        notesList.repaint();
    }

    private JPanel muted(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.add(Theme.muted(text));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("h:mm a"));
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("h:mm a")));
    }
}
