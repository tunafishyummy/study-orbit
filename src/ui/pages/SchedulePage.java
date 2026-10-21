package ui.pages;

import data.AppData;
import data.NoteManager;
import data.ScheduleManager;
import model.Course;
import model.Deadline;
import model.ScheduleClass;
import ui.Theme;
import ui.components.RoundedComboBox;
import ui.components.RoundedPanel;
import ui.components.RoundedTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// Screen 8, the weekly schedule and the deadline list
public class SchedulePage extends JPanel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private final ScheduleManager schedule = AppData.schedule();
    private final NoteManager notes = AppData.get();

    private final CardLayout views = new CardLayout();
    private final JPanel viewPanel = new JPanel(views);
    private final JLabel title = new JLabel();
    private final JButton schedulePill = pill("Schedule");
    private final JButton deadlinesPill = pill("Deadlines");
    private final JButton addButton = Theme.primaryButton("Add class");
    private final WeekGrid weekGrid = new WeekGrid();
    private final JPanel deadlineList = new JPanel();
    private boolean scheduleMode = true;

    public SchedulePage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        add(header(), BorderLayout.NORTH);

        viewPanel.setOpaque(false);
        viewPanel.add(scheduleCard(), "Schedule");
        viewPanel.add(deadlinesCard(), "Deadlines");
        add(viewPanel, BorderLayout.CENTER);

        schedulePill.addActionListener(e -> showTab(true));
        deadlinesPill.addActionListener(e -> showTab(false));
        addButton.addActionListener(e -> {
            if (scheduleMode) {
                addClass();
            } else {
                addDeadline();
            }
        });

        showTab(true);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                refresh();
            }
        });
    }

    private JPanel header() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel modeLabel = new JLabel("Mode:");
        modeLabel.setFont(Theme.BODY);
        modeLabel.setForeground(Theme.TEXT);
        right.add(modeLabel);
        right.add(schedulePill);
        right.add(deadlinesPill);
        right.add(addButton);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void showTab(boolean scheduleTab) {
        scheduleMode = scheduleTab;
        if (scheduleTab) {
            views.show(viewPanel, "Schedule");
            title.setText("Schedules");
            addButton.setText("Add class");
        } else {
            views.show(viewPanel, "Deadlines");
            title.setText("Deadlines");
            addButton.setText("Add deadline");
        }
        stylePill(schedulePill, scheduleTab);
        stylePill(deadlinesPill, scheduleTab == false);
        refresh();
    }

    private JButton pill(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.BODY);
        button.setFocusPainted(false);
        button.setOpaque(true);
        return button;
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

    private JPanel scheduleCard() {
        JPanel card = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Semester Class Schedule", SwingConstants.CENTER);
        heading.setFont(Theme.font(Font.BOLD, 24));
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);

        JPanel gridHolder = new RoundedPanel(Color.WHITE, Theme.CORNER_RADIUS);
        gridHolder.setLayout(new BorderLayout());
        gridHolder.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        gridHolder.add(weekGrid, BorderLayout.CENTER);
        card.add(gridHolder, BorderLayout.CENTER);
        return card;
    }

    private JPanel deadlinesCard() {
        JPanel card = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Deadlines", SwingConstants.CENTER);
        heading.setFont(Theme.font(Font.BOLD, 24));
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);

        deadlineList.setOpaque(false);
        deadlineList.setLayout(new BoxLayout(deadlineList, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(deadlineList);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void refresh() {
        weekGrid.repaint();

        deadlineList.removeAll();
        List<Deadline> deadlines = schedule.getDeadlines();
        deadlines.sort(Comparator.comparing(Deadline::getDueDate));

        if (deadlines.isEmpty()) {
            deadlineList.add(Theme.muted("No deadlines yet. Use the add deadline button."));
        }
        for (int index = 0; index < deadlines.size(); index++) {
            deadlineList.add(deadlineRow(deadlines.get(index)));
            deadlineList.add(Box.createVerticalStrut(6));
        }
        deadlineList.revalidate();
        deadlineList.repaint();
    }

    private JPanel deadlineRow(Deadline deadline) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JCheckBox done = new JCheckBox();
        done.setOpaque(false);
        done.setSelected(deadline.isDone());
        done.addActionListener(e -> {
            deadline.setDone(done.isSelected());
            try {
                schedule.updateDeadline(deadline);
            } catch (Exception exception) {
                System.out.println("Could not update the deadline: " + exception.getMessage());
            }
            refresh();
        });
        row.add(done, BorderLayout.WEST);

        String text = deadline.getTitle() + "  -  " + deadline.getDueDate();
        if (deadline.getCourseCode().isEmpty() == false) {
            text = deadline.getCourseCode() + "  -  " + text;
        }
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        if (deadline.isDone()) {
            label.setForeground(Theme.MUTED);
        } else {
            label.setForeground(Theme.TEXT);
        }
        row.add(label, BorderLayout.CENTER);

        JButton delete = Theme.button("Delete");
        delete.addActionListener(e -> {
            try {
                schedule.deleteDeadline(deadline);
            } catch (Exception exception) {
                System.out.println("Could not delete the deadline: " + exception.getMessage());
            }
            refresh();
        });
        row.add(delete, BorderLayout.EAST);
        return row;
    }

    private void addClass() {
        JComboBox<String> courseBox = new RoundedComboBox<>();
        List<Course> courses = notes.getCourses();
        for (int index = 0; index < courses.size(); index++) {
            courseBox.addItem(courses.get(index).getCode());
        }
        if (courseBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Add a course first, then add its class time.");
            return;
        }

        JComboBox<String> dayBox = new RoundedComboBox<>(
                new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" });
        JTextField startField = new RoundedTextField();
        JTextField endField = new RoundedTextField();
        JTextField roomField = new RoundedTextField();
        startField.setText("9:00");
        endField.setText("10:30");

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.add(new JLabel("Course"));
        form.add(courseBox);
        form.add(new JLabel("Day"));
        form.add(dayBox);
        form.add(new JLabel("Start time"));
        form.add(startField);
        form.add(new JLabel("End time"));
        form.add(endField);
        form.add(new JLabel("Room"));
        form.add(roomField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add class", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            LocalTime startTime = LocalTime.parse(startField.getText().trim(), TIME_FORMAT);
            LocalTime endTime = LocalTime.parse(endField.getText().trim(), TIME_FORMAT);
            schedule.addClass((String) courseBox.getSelectedItem(), (String) dayBox.getSelectedItem(),
                    startTime, endTime, roomField.getText().trim());
            refresh();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Use a time like 9:00 or 10:30.");
        }
    }

    private void addDeadline() {
        JTextField titleField = new RoundedTextField();
        JTextField dateField = new RoundedTextField();
        dateField.setText(LocalDate.now().plusDays(7).toString());

        JComboBox<String> courseBox = new RoundedComboBox<>();
        courseBox.addItem("");
        List<Course> courses = notes.getCourses();
        for (int index = 0; index < courses.size(); index++) {
            courseBox.addItem(courses.get(index).getCode());
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.add(new JLabel("Title"));
        form.add(titleField);
        form.add(new JLabel("Due date (2026-10-30)"));
        form.add(dateField);
        form.add(new JLabel("Course (optional)"));
        form.add(courseBox);

        int result = JOptionPane.showConfirmDialog(this, form, "Add deadline", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        if (titleField.getText().trim().isEmpty()) {
            return;
        }
        try {
            schedule.addDeadline((String) courseBox.getSelectedItem(), titleField.getText().trim(),
                    LocalDate.parse(dateField.getText().trim()));
            refresh();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Use a date like 2026-10-30.");
        }
    }

    private String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("h:mm"));
    }

    private class WeekGrid extends JPanel {
        private final String[] DAYS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        private final int START_HOUR = 7;
        private final int END_HOUR = 17;

        WeekGrid() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int labelWidth = 64;
            int headerHeight = 28;
            double columnWidth = (getWidth() - labelWidth) / 7.0;
            double rowHeight = (getHeight() - headerHeight) / (double) (END_HOUR - START_HOUR);

            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(new Color(0x111111));
            for (int index = 0; index < DAYS.length; index++) {
                g2.drawString(DAYS[index], (int) (labelWidth + index * columnWidth) + 10, 18);
            }

            g2.setFont(Theme.font(Font.PLAIN, 11));
            g2.setColor(new Color(0x666666));
            for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
                int y = (int) (headerHeight + (hour - START_HOUR) * rowHeight);
                g2.drawString(hour + ":00", 6, y + 14);
            }

            g2.setColor(new Color(0xD9D9D9));
            for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
                int y = (int) (headerHeight + (hour - START_HOUR) * rowHeight);
                g2.drawLine(labelWidth, y, getWidth(), y);
            }
            for (int index = 0; index <= DAYS.length; index++) {
                int x = (int) (labelWidth + index * columnWidth);
                g2.drawLine(x, 0, x, getHeight());
            }

            List<ScheduleClass> classes = schedule.getClasses();
            for (int index = 0; index < classes.size(); index++) {
                drawClass(g2, classes.get(index), labelWidth, headerHeight, columnWidth, rowHeight);
            }
        }

        private void drawClass(Graphics2D g2, ScheduleClass item, int labelWidth, int headerHeight,
                               double columnWidth, double rowHeight) {
            int dayIndex = Arrays.asList(DAYS).indexOf(item.getDay());
            if (dayIndex < 0) {
                return;
            }

            double startMinutes = item.getStart().getHour() * 60.0 + item.getStart().getMinute() - START_HOUR * 60.0;
            double endMinutes = item.getEnd().getHour() * 60.0 + item.getEnd().getMinute() - START_HOUR * 60.0;
            if (endMinutes <= startMinutes) {
                return;
            }

            int x = (int) (labelWidth + dayIndex * columnWidth) + 3;
            int y = (int) (headerHeight + (startMinutes / 60.0) * rowHeight);
            int width = (int) columnWidth - 6;
            int height = (int) (((endMinutes - startMinutes) / 60.0) * rowHeight);
            if (height < 18) {
                height = 18;
            }

            Color color = Theme.PRIMARY;
            Course course = notes.findCourse(item.getCourseCode());
            if (course != null) {
                color = Theme.parse(course.getColorHex());
            }

            g2.setColor(color);
            g2.fillRoundRect(x, y, width, height, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(Font.BOLD, 11));
            g2.drawString(item.getCourseCode(), x + 6, y + 14);

            if (height > 26) {
                g2.setFont(Theme.font(Font.PLAIN, 10));
                g2.drawString(formatTime(item.getStart()) + " - " + formatTime(item.getEnd()), x + 6, y + 27);
            }
            if (height > 38 && item.getRoom().isEmpty() == false) {
                g2.setFont(Theme.font(Font.PLAIN, 10));
                g2.drawString(item.getRoom(), x + 6, y + 39);
            }
        }
    }
}
