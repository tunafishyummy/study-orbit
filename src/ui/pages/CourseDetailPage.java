package ui.pages;

import data.AppData;
import data.NoteManager;
import data.ScheduleManager;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import model.Course;
import model.Deadline;
import model.ScheduleClass;
import ui.Theme;
import ui.components.RoundedPanel;
import ui.components.RoundedTextField;
import ui.dialogs.ClassDialog;
import ui.dialogs.DeadlineDialog;
import ui.dialogs.FormDialog;

// one course with its notes, deadlines and class meetings
public class CourseDetailPage extends JPanel {
    private static final String[] DAY_ORDER = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final NoteManager notes = AppData.get();
    private final ScheduleManager schedule = AppData.schedule();
    private final Runnable onBack;
    private final NotesPage notesPage;
    private final JLabel courseCode = new JLabel(" ");
    private final JLabel courseTitle = new JLabel(" ");
    private final JLabel meta = new JLabel(" ");
    private final Dot dot = new Dot(Theme.PRIMARY);
    private final CardLayout views = new CardLayout();
    private final JPanel viewPanel = new JPanel(views);
    private final JButton notesPill = pill("Notes");
    private final JButton deadlinesPill = pill("Deadlines");
    private final JButton schedulePill = pill("Schedule");
    private final JPanel deadlinesPanel = new ScrollablePanel();
    private final JPanel schedulePanel = new ScrollablePanel();
    private Course course;
    private String activeTab = "Notes";

    public CourseDetailPage(Runnable onBack) {
        this.onBack = onBack;
        this.notesPage = new NotesPage(true);
        this.notesPage.setChangeListener(this::refreshCounts);
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        showTab("Notes");

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                refreshData();
            }
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);

        JPanel identity = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        identity.setOpaque(false);
        JButton back = Theme.button("<- All courses");
        back.addActionListener(e -> attemptBack());
        identity.add(back);
        identity.add(Box.createHorizontalStrut(12));
        dot.setPreferredSize(new Dimension(16, 16));
        dot.setMaximumSize(new Dimension(16, 16));
        identity.add(dot);
        identity.add(Box.createHorizontalStrut(8));

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        courseCode.setFont(Theme.font(Font.BOLD, 24));
        courseCode.setForeground(Theme.TEXT);
        courseCode.setAlignmentX(Component.LEFT_ALIGNMENT);
        courseTitle.setFont(Theme.BODY);
        courseTitle.setForeground(Theme.MUTED);
        courseTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        names.add(courseCode);
        names.add(Box.createVerticalStrut(2));
        names.add(courseTitle);
        identity.add(names);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton editCourse = Theme.button("Edit course");
        editCourse.addActionListener(e -> editCourse());
        JButton deleteCourse = Theme.button("Delete course");
        deleteCourse.addActionListener(e -> deleteCourse());
        actions.add(editCourse);
        actions.add(deleteCourse);

        top.add(identity, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        meta.setFont(Theme.BODY);
        meta.setForeground(Theme.MUTED);
        meta.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));

        JPanel tabRow = new JPanel(new BorderLayout());
        tabRow.setOpaque(false);
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabs.setOpaque(false);
        tabs.add(notesPill);
        tabs.add(deadlinesPill);
        tabs.add(schedulePill);
        notesPill.addActionListener(e -> showTab("Notes"));
        deadlinesPill.addActionListener(e -> showTab("Deadlines"));
        schedulePill.addActionListener(e -> showTab("Schedule"));

        JPanel quick = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quick.setOpaque(false);
        JButton newNote = Theme.primaryButton("+ New note");
        newNote.addActionListener(e -> {
            if (showTab("Notes")) {
                notesPage.startNewNoteFor(course == null ? null : course.getCode());
            }
        });
        JButton addDeadline = Theme.button("+ Add deadline");
        addDeadline.addActionListener(e -> {
            if (showTab("Deadlines")) {
                addDeadline();
            }
        });
        JButton addClass = Theme.button("+ Add class");
        addClass.addActionListener(e -> {
            if (showTab("Schedule")) {
                addClass();
            }
        });
        quick.add(newNote);
        quick.add(addDeadline);
        quick.add(addClass);
        tabRow.add(tabs, BorderLayout.WEST);
        tabRow.add(quick, BorderLayout.EAST);

        header.add(top);
        header.add(meta);
        header.add(tabRow);
        return header;
    }

    private JPanel buildBody() {
        viewPanel.setOpaque(false);
        viewPanel.add(notesPage, "Notes");
        viewPanel.add(buildDeadlines(), "Deadlines");
        viewPanel.add(buildSchedule(), "Schedule");
        return viewPanel;
    }

    private JScrollPane buildDeadlines() {
        deadlinesPanel.setOpaque(false);
        deadlinesPanel.setLayout(new BoxLayout(deadlinesPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(deadlinesPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JScrollPane buildSchedule() {
        schedulePanel.setOpaque(false);
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(schedulePanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public void showCourse(Course course) {
        applyCourse(course, true);
    }

    public void restoreCourse(Course course) {
        applyCourse(course, false);
    }

    private void applyCourse(Course course, boolean resetTab) {
        this.course = course;
        notesPage.setCourseScope(course);
        if (resetTab) {
            activeTab = "Notes";
        }
        refreshData();
        showTab(activeTab);
    }

    public boolean canLeave() {
        return notesPage.canLeave();
    }

    private boolean showTab(String tab) {
        if (activeTab.equals("Notes") && tab.equals("Notes") == false) {
            if (notesPage.canLeave() == false) {
                styleTabs();
                return false;
            }
        }
        activeTab = tab;
        views.show(viewPanel, tab);
        styleTabs();
        if (tab.equals("Deadlines")) {
            rebuildDeadlines();
        } else if (tab.equals("Schedule")) {
            rebuildSchedule();
        } else {
            notesPage.refresh();
        }
        return true;
    }

    private void attemptBack() {
        if (notesPage.canLeave()) {
            onBack.run();
        }
    }

    private void refreshData() {
        if (course == null) {
            return;
        }
        courseCode.setText(course.getCode());
        courseTitle.setText(course.getTitle());
        dot.setColor(Theme.parse(course.getColorHex()));
        refreshCounts();
        notesPage.refresh();
        rebuildDeadlines();
        rebuildSchedule();
    }

    private void refreshCounts() {
        if (course == null) {
            return;
        }
        int noteCount = notes.getNotesByCourse(course.getCode()).size();
        int deadlineCount = courseDeadlines().size();
        int classCount = courseClasses().size();
        meta.setText(noteCount + " notes   |   " + deadlineCount + " deadlines   |   "
                + classCount + " class meetings");
        notesPill.setText("Notes (" + noteCount + ")");
        deadlinesPill.setText("Deadlines (" + deadlineCount + ")");
        schedulePill.setText("Schedule (" + classCount + ")");
    }

    private List<Deadline> courseDeadlines() {
        List<Deadline> result = new ArrayList<>();
        for (Deadline deadline : schedule.getDeadlines()) {
            if (deadline.getCourseCode().equalsIgnoreCase(course.getCode())) {
                result.add(deadline);
            }
        }
        result.sort((first, second) -> {
            if (first.isDone() != second.isDone()) {
                return first.isDone() ? 1 : -1;
            }
            int date = first.getDueDate().compareTo(second.getDueDate());
            if (date != 0) {
                return date;
            }
            return first.getDueTime().compareTo(second.getDueTime());
        });
        return result;
    }

    private List<ScheduleClass> courseClasses() {
        List<ScheduleClass> result = new ArrayList<>();
        for (ScheduleClass item : schedule.getClasses()) {
            if (item.getCourseCode().equalsIgnoreCase(course.getCode())) {
                result.add(item);
            }
        }
        result.sort((first, second) -> {
            int day = dayIndex(first.getDay()) - dayIndex(second.getDay());
            if (day != 0) {
                return day;
            }
            return first.getStart().compareTo(second.getStart());
        });
        return result;
    }

    private static int dayIndex(String day) {
        for (int index = 0; index < DAY_ORDER.length; index++) {
            if (DAY_ORDER[index].equalsIgnoreCase(day)) {
                return index;
            }
        }
        return DAY_ORDER.length;
    }

    private void rebuildDeadlines() {
        deadlinesPanel.removeAll();
        if (course == null) {
            return;
        }
        List<Deadline> items = courseDeadlines();
        if (items.isEmpty()) {
            deadlinesPanel.add(emptyState("No deadlines for " + course.getCode() + " yet.",
                    "Add deadline", e -> addDeadline()));
        } else {
            for (Deadline deadline : items) {
                deadlinesPanel.add(deadlineRow(deadline));
                deadlinesPanel.add(Box.createVerticalStrut(8));
            }
        }
        deadlinesPanel.revalidate();
        deadlinesPanel.repaint();
    }

    private JPanel deadlineRow(Deadline deadline) {
        RoundedPanel row = new RoundedPanel(Theme.SURFACE, 14, Theme.BORDER);
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(deadline.getTitle());
        title.setFont(Theme.font(Font.BOLD, 14));
        title.setForeground(Theme.TEXT);
        JLabel due = new JLabel(deadline.getDueDate().format(DATE)
                + "   |   " + Theme.formatTime(deadline.getDueTime()));
        due.setFont(Theme.font(Font.PLAIN, 12));
        due.setForeground(Theme.MUTED);
        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(due);
        row.add(text, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JLabel status = new JLabel(statusText(deadline));
        status.setFont(Theme.font(Font.BOLD, 11));
        status.setForeground(statusColor(deadline));
        actions.add(status);
        JButton toggle = Theme.button(deadline.isDone() ? "Undo" : "Mark done");
        toggle.addActionListener(e -> toggleDeadline(deadline));
        JButton edit = Theme.button("Edit");
        edit.addActionListener(e -> editDeadline(deadline));
        JButton delete = Theme.button("Delete");
        delete.addActionListener(e -> deleteDeadline(deadline));
        actions.add(toggle);
        actions.add(edit);
        actions.add(delete);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private String statusText(Deadline deadline) {
        if (deadline.isDone()) {
            return "Done";
        }
        LocalDate today = LocalDate.now();
        if (deadline.getDueDate().isBefore(today)) {
            return "Overdue";
        }
        if (deadline.getDueDate().isAfter(today.plusDays(3)) == false) {
            return "Due soon";
        }
        return "Upcoming";
    }

    private Color statusColor(Deadline deadline) {
        if (deadline.isDone()) {
            return new Color(0x16A34A);
        }
        LocalDate today = LocalDate.now();
        if (deadline.getDueDate().isBefore(today)) {
            return new Color(0xDC2626);
        }
        if (deadline.getDueDate().isAfter(today.plusDays(3)) == false) {
            return new Color(0xD97706);
        }
        return Theme.MUTED;
    }

    private void toggleDeadline(Deadline deadline) {
        deadline.setDone(deadline.isDone() == false);
        try {
            schedule.updateDeadline(deadline);
        } catch (IOException exception) {
            error("Could not update the deadline", exception);
        }
        refreshData();
    }

    private void addDeadline() {
        Deadline created = DeadlineDialog.show(this, course == null ? null : course.getCode(), null);
        if (created == null) {
            return;
        }
        try {
            schedule.addDeadline(created.getCourseCode(), created.getTitle(),
                    created.getDueDate(), created.getDueTime());
        } catch (IOException exception) {
            error("Could not save the deadline", exception);
        }
        refreshData();
    }

    private void editDeadline(Deadline deadline) {
        Deadline edited = DeadlineDialog.show(this, course.getCode(), deadline);
        if (edited == null) {
            return;
        }
        try {
            schedule.updateDeadline(edited);
        } catch (IOException exception) {
            error("Could not save the deadline", exception);
        }
        refreshData();
    }

    private void deleteDeadline(Deadline deadline) {
        int choice = JOptionPane.showConfirmDialog(this, "Delete \"" + deadline.getTitle() + "\"?",
                "Delete deadline", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            schedule.deleteDeadline(deadline);
        } catch (IOException exception) {
            error("Could not delete the deadline", exception);
        }
        refreshData();
    }

    private void rebuildSchedule() {
        schedulePanel.removeAll();
        if (course == null) {
            return;
        }
        List<ScheduleClass> items = courseClasses();
        if (items.isEmpty()) {
            schedulePanel.add(emptyState("No class meetings for " + course.getCode() + " yet.",
                    "Add class", e -> addClass()));
        } else {
            String lastDay = null;
            for (ScheduleClass item : items) {
                if (item.getDay().equalsIgnoreCase(lastDay) == false) {
                    lastDay = item.getDay();
                    JLabel day = new JLabel(lastDay.toUpperCase());
                    day.setFont(Theme.font(Font.BOLD, 12));
                    day.setForeground(Theme.PRIMARY);
                    day.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 0));
                    day.setAlignmentX(Component.LEFT_ALIGNMENT);
                    schedulePanel.add(day);
                }
                schedulePanel.add(classRow(item));
                schedulePanel.add(Box.createVerticalStrut(8));
            }
        }
        schedulePanel.revalidate();
        schedulePanel.repaint();
    }

    private JPanel classRow(ScheduleClass item) {
        RoundedPanel row = new RoundedPanel(Theme.SURFACE, 14, Theme.BORDER);
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel time = new JLabel(Theme.formatTime(item.getStart()) + " - " + Theme.formatTime(item.getEnd()));
        time.setFont(Theme.font(Font.BOLD, 14));
        time.setForeground(Theme.TEXT);
        JLabel room = new JLabel(item.getRoom().isEmpty() ? "Room not set" : item.getRoom());
        room.setFont(Theme.font(Font.PLAIN, 12));
        room.setForeground(Theme.MUTED);
        text.add(time);
        text.add(Box.createVerticalStrut(2));
        text.add(room);
        row.add(text, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton edit = Theme.button("Edit");
        edit.addActionListener(e -> editClass(item));
        JButton delete = Theme.button("Delete");
        delete.addActionListener(e -> deleteClass(item));
        actions.add(edit);
        actions.add(delete);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private void addClass() {
        ScheduleClass created = ClassDialog.show(this, course == null ? null : course.getCode(), null);
        if (created == null) {
            return;
        }
        try {
            schedule.addClass(created.getCourseCode(), created.getDay(), created.getStart(),
                    created.getEnd(), created.getRoom());
        } catch (IOException exception) {
            error("Could not save the class", exception);
        }
        refreshData();
    }

    private void editClass(ScheduleClass item) {
        ScheduleClass edited = ClassDialog.show(this, course.getCode(), item);
        if (edited == null) {
            return;
        }
        try {
            schedule.saveClasses();
        } catch (IOException exception) {
            error("Could not save the class", exception);
        }
        refreshData();
    }

    private void deleteClass(ScheduleClass item) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete " + item.getDay() + " " + Theme.formatTime(item.getStart()) + "?",
                "Delete class", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            schedule.deleteClass(item);
        } catch (IOException exception) {
            error("Could not delete the class", exception);
        }
        refreshData();
    }

    private void editCourse() {
        if (course == null) {
            return;
        }
        RoundedTextField codeField = new RoundedTextField();
        codeField.setPreferredSize(new Dimension(340, 34));
        codeField.setText(course.getCode());
        RoundedTextField titleField = new RoundedTextField();
        titleField.setPreferredSize(new Dimension(340, 34));
        titleField.setText(course.getTitle());

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.add(new JLabel("Course code"));
        form.add(codeField);
        form.add(new JLabel("Course title"));
        form.add(titleField);

        while (true) {
            if (FormDialog.show(this, "Edit course", form) == false) {
                return;
            }
            String newCode = codeField.getText().trim();
            String newTitle = titleField.getText().trim();
            try {
                String oldCode = course.getCode();
                notes.updateCourse(oldCode, newCode, newTitle, course.getColorHex());
                schedule.renameCourse(oldCode, newCode);
                notesPage.setCourseScope(course);
                refreshData();
                showTab(activeTab);
                return;
            } catch (IllegalArgumentException exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(),
                        "Check your input", JOptionPane.WARNING_MESSAGE);
            } catch (IOException exception) {
                error("Could not save the course", exception);
                return;
            }
        }
    }

    private void deleteCourse() {
        if (course == null) {
            return;
        }
        int noteCount = notes.getNotesByCourse(course.getCode()).size();
        int deadlineCount = courseDeadlines().size();
        int classCount = courseClasses().size();
        String message = "Delete " + course.getCode() + "?\n\n"
                + noteCount + " notes will be kept without a course.\n"
                + deadlineCount + " deadlines will be kept without a course.\n"
                + classCount + " class meetings will be removed.";
        int choice = JOptionPane.showConfirmDialog(this, message, "Delete course",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        if (notesPage.canLeave() == false) {
            return;
        }
        try {
            notes.deleteCourse(course.getCode());
            schedule.deleteCourseData(course.getCode());
        } catch (IOException exception) {
            error("Could not delete the course", exception);
            return;
        }
        onBack.run();
    }

    private JPanel emptyState(String text, String buttonText, ActionListener action) {
        RoundedPanel card = new RoundedPanel(Theme.SURFACE, 16, Theme.BORDER);
        card.setLayout(new GridBagLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.MUTED);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton button = Theme.primaryButton(buttonText);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(action);
        box.add(label);
        box.add(Box.createVerticalStrut(10));
        box.add(button);
        card.add(box);
        return card;
    }

    private JButton pill(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.BODY);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleTabs() {
        stylePill(notesPill, activeTab.equals("Notes"));
        stylePill(deadlinesPill, activeTab.equals("Deadlines"));
        stylePill(schedulePill, activeTab.equals("Schedule"));
    }

    private void stylePill(JButton button, boolean active) {
        if (active) {
            button.setBackground(Theme.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(7, 20, 7, 20));
        } else {
            button.setBackground(Theme.BG);
            button.setForeground(Theme.TEXT);
            button.setBorderPainted(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    BorderFactory.createEmptyBorder(6, 19, 6, 19)));
        }
    }

    private void error(String title, IOException exception) {
        JOptionPane.showMessageDialog(this, title + ": " + exception.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 100;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static class Dot extends JComponent {
        private Color color;

        Dot(Color color) {
            this.color = color;
            setOpaque(false);
        }

        void setColor(Color color) {
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int size = Math.min(getWidth(), getHeight());
            g2.fillOval(0, 0, size, size);
            g2.dispose();
        }
    }
}
