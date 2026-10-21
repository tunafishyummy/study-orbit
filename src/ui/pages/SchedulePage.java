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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// Screen 8, the weekly schedule and the deadline calendar
public class SchedulePage extends JPanel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
    private static final String[] DEADLINE_DAYS = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

    private final ScheduleManager schedule = AppData.schedule();
    private final NoteManager notes = AppData.get();

    private final CardLayout views = new CardLayout();
    private final JPanel viewPanel = new JPanel(views);
    private final JLabel title = new JLabel();
    private final JButton schedulePill = pill("Schedule");
    private final JButton deadlinesPill = pill("Deadlines");
    private final JButton addButton = Theme.primaryButton("Add class");
    private final WeekGrid weekGrid = new WeekGrid();
    private final JLabel weekLabel = new JLabel();
    private final JPanel weekColumns = new JPanel();
    private boolean scheduleMode = true;
    private LocalDate weekStart = startOfWeek(LocalDate.now());

    public SchedulePage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        add(header(), BorderLayout.NORTH);

        viewPanel.setOpaque(false);
        viewPanel.add(scheduleCard(), "Schedule");
        viewPanel.add(deadlinesCard(), "Deadlines");
        add(viewPanel, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomBar.setOpaque(false);
        bottomBar.add(addButton);
        add(bottomBar, BorderLayout.SOUTH);

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
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel weekHeader = new JPanel(new BorderLayout());
        weekHeader.setOpaque(false);

        JButton previous = arrowButton("<");
        previous.addActionListener(e -> navigateWeek(-7));
        JButton next = arrowButton(">");
        next.addActionListener(e -> navigateWeek(7));

        weekLabel.setFont(Theme.font(Font.BOLD, 22));
        weekLabel.setForeground(Theme.TEXT);
        weekLabel.setHorizontalAlignment(SwingConstants.CENTER);

        weekHeader.add(previous, BorderLayout.WEST);
        weekHeader.add(weekLabel, BorderLayout.CENTER);
        weekHeader.add(next, BorderLayout.EAST);
        card.add(weekHeader, BorderLayout.NORTH);

        JPanel white = new RoundedPanel(Color.WHITE, Theme.CORNER_RADIUS);
        white.setLayout(new BorderLayout());
        white.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        weekColumns.setOpaque(false);
        weekColumns.setLayout(new GridLayout(1, 7, 0, 0));
        white.add(weekColumns, BorderLayout.CENTER);
        card.add(white, BorderLayout.CENTER);
        return card;
    }

    private JButton arrowButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Theme.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(Theme.font(Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void navigateWeek(int days) {
        weekStart = weekStart.plusDays(days);
        refreshDeadlines();
    }

    private void refresh() {
        weekGrid.repaint();
        refreshDeadlines();
    }

    private void refreshDeadlines() {
        LocalDate weekEnd = weekStart.plusDays(6);
        String label;
        if (weekStart.getMonth() == weekEnd.getMonth()) {
            label = weekStart.format(DateTimeFormatter.ofPattern("MMMM d"))
                    + " - " + weekEnd.format(DateTimeFormatter.ofPattern("d, yyyy"));
        } else {
            label = weekStart.format(DateTimeFormatter.ofPattern("MMMM d"))
                    + " - " + weekEnd.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        }
        weekLabel.setText(label);

        weekColumns.removeAll();
        List<Deadline> deadlines = schedule.getDeadlines();
        deadlines.sort(Comparator.comparing(Deadline::getDueTime));

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            LocalDate date = weekStart.plusDays(dayIndex);
            JPanel column = dayColumn(date, deadlines);
            column.putClientProperty("date", date);
            if (dayIndex < 6) {
                column.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xE0E0E0)));
            }
            weekColumns.add(column);
        }

        weekColumns.revalidate();
        weekColumns.repaint();
    }

    private JPanel dayColumn(LocalDate date, List<Deadline> deadlines) {
        JPanel column = new JPanel(new BorderLayout(0, 6));
        column.setOpaque(false);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));

        JLabel dayName = new JLabel(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        dayName.setFont(Theme.font(Font.BOLD, 15));
        dayName.setForeground(new Color(0x111111));
        dayName.setAlignmentX(Component.CENTER_ALIGNMENT);
        head.add(dayName);

        JLabel dayDate = new JLabel(date.format(DateTimeFormatter.ofPattern("MMM d")));
        dayDate.setFont(Theme.font(Font.PLAIN, 12));
        dayDate.setForeground(new Color(0x666666));
        dayDate.setAlignmentX(Component.CENTER_ALIGNMENT);
        head.add(dayDate);
        column.add(head, BorderLayout.NORTH);

        JPanel cards = new JPanel();
        cards.setOpaque(false);
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));

        int count = 0;
        for (int index = 0; index < deadlines.size(); index++) {
            Deadline deadline = deadlines.get(index);
            if (deadline.getDueDate().equals(date)) {
                DeadlineCard card = new DeadlineCard(deadline);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                cards.add(card);
                cards.add(Box.createVerticalStrut(6));
                count = count + 1;
            }
        }
        if (count == 0) {
            JLabel empty = new JLabel(" ");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            cards.add(empty);
        }

        JScrollPane scroll = new JScrollPane(cards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        column.add(scroll, BorderLayout.CENTER);
        return column;
    }

    private void saveDeadline(Deadline deadline) {
        try {
            schedule.updateDeadline(deadline);
        } catch (Exception exception) {
            System.out.println("Could not update the deadline: " + exception.getMessage());
        }
        refreshDeadlines();
    }

    private class DeadlineCard extends JPanel {
        private static final int HEADER_HEIGHT = 40;
        private final Deadline deadline;
        private final Color color;
        private boolean hovered;

        DeadlineCard(Deadline deadline) {
            this.deadline = deadline;

            Color base = Theme.PRIMARY;
            Course course = notes.findCourse(deadline.getCourseCode());
            if (course != null) {
                base = Theme.parse(course.getColorHex());
            }
            if (deadline.isDone()) {
                base = new Color(0x4B4B4B);
            }
            this.color = base;

            setOpaque(false);
            setLayout(new BorderLayout());
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
            setPreferredSize(new Dimension(150, 74));
            setToolTipText("<html>" + deadline.getCourseCode() + "<br>" + deadline.getTitle()
                    + "<br>Due " + deadline.getDueDate() + " " + formatTime(deadline.getDueTime()) + "</html>");

            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setPreferredSize(new Dimension(0, HEADER_HEIGHT));
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setBorder(new EmptyBorder(6, 9, 4, 9));

            Color headerText = readableOn(base);
            JLabel code = new JLabel(deadline.getCourseCode());
            code.setFont(Theme.font(Font.BOLD, 12));
            code.setForeground(headerText);
            code.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(code);

            JLabel due = new JLabel("Due " + formatTime(deadline.getDueTime()));
            due.setFont(Theme.font(Font.PLAIN, 10));
            due.setForeground(headerText);
            due.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(due);

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.setBorder(new EmptyBorder(4, 9, 6, 9));

            JLabel title = new JLabel(deadline.getTitle());
            title.setFont(Theme.font(Font.BOLD, 10));
            title.setForeground(Color.WHITE);
            if (deadline.isDone()) {
                title.setText("<html><strike>" + deadline.getTitle() + "</strike></html>");
            }
            footer.add(title, BorderLayout.CENTER);

            add(header, BorderLayout.NORTH);
            add(footer, BorderLayout.CENTER);

            JPopupMenu menu = new JPopupMenu();
            JMenuItem toggle = new JMenuItem(deadline.isDone() ? "Mark as not done" : "Mark as done");
            toggle.addActionListener(e -> {
                deadline.setDone(deadline.isDone() == false);
                saveDeadline(deadline);
            });
            JMenuItem delete = new JMenuItem("Delete deadline");
            delete.addActionListener(e -> {
                try {
                    schedule.deleteDeadline(deadline);
                } catch (Exception exception) {
                    System.out.println("Could not delete the deadline: " + exception.getMessage());
                }
                refreshDeadlines();
            });
            menu.add(toggle);
            menu.add(delete);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    showMenu(event);
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    showMenu(event);
                }

                private void showMenu(MouseEvent event) {
                    if (event.isPopupTrigger()) {
                        menu.show(DeadlineCard.this, event.getX(), event.getY());
                    }
                }
            });
        }

        private Color readableOn(Color color) {
            double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255.0;
            if (luminance > 0.55) {
                return new Color(0x14121F);
            }
            return Color.WHITE;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            Shape clip = new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, 10, 10);
            g2.clip(clip);

            if (hovered) {
                g2.setColor(color.brighter());
            } else {
                g2.setColor(color);
            }
            g2.fillRect(0, 0, width, HEADER_HEIGHT);

            g2.setColor(new Color(0x211E3F));
            g2.fillRect(0, HEADER_HEIGHT, width, height - HEADER_HEIGHT);

            g2.setClip(null);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
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
        JTextField timeField = new RoundedTextField();
        dateField.setText(LocalDate.now().plusDays(7).toString());
        timeField.setText("23:59");

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
        form.add(new JLabel("Due time (23:59)"));
        form.add(timeField);
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
            LocalDate dueDate = LocalDate.parse(dateField.getText().trim());
            LocalTime dueTime = LocalTime.parse(timeField.getText().trim(), TIME_FORMAT);
            schedule.addDeadline((String) courseBox.getSelectedItem(), titleField.getText().trim(),
                    dueDate, dueTime);
            weekStart = startOfWeek(dueDate);
            refreshDeadlines();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Use a date like 2026-10-30 and a time like 23:59.");
        }
    }

    private static LocalDate startOfWeek(LocalDate date) {
        int offset = date.getDayOfWeek().getValue() % 7;
        return date.minusDays(offset);
    }

    private String formatTime(LocalTime time) {
        return Theme.formatTime(time);
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
