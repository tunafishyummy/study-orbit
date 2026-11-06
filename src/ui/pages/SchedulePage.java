package ui.pages;

import data.AppData;
import data.NoteManager;
import data.ScheduleManager;
import model.Course;
import model.Deadline;
import model.ScheduleClass;
import ui.Theme;
import ui.components.RoundedPanel;
import ui.dialogs.ClassDialog;
import ui.dialogs.DeadlineDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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

    private static final String[] DEADLINE_DAYS = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

    private final ScheduleManager schedule = AppData.schedule();
    private final NoteManager notes = AppData.get();

    private final CardLayout views = new CardLayout();
    private final JPanel viewPanel = new JPanel(views);
    private final JLabel title = new JLabel();
    private final JButton schedulePill = pill("Schedule");
    private final JButton deadlinesPill = pill("Deadlines");
    private final JButton addButton = Theme.primaryButton("Add class");
    private final JButton saveButton = Theme.primaryButton("Save changes");
    private final WeekGrid weekGrid = new WeekGrid();
    private final JLabel weekLabel = new JLabel();
    private final JPanel weekColumns = new JPanel();
    private boolean scheduleMode = true;
    private boolean classesChanged;
    private boolean deadlinesChanged;
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

        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveChanges());

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomBar.setOpaque(false);
        bottomBar.add(saveButton);
        bottomBar.add(addButton);
        add(bottomBar, BorderLayout.SOUTH);

        schedulePill.addActionListener(e -> showTab(true));
        deadlinesPill.addActionListener(e -> showTab(false));
        addButton.addActionListener(e -> {
            if (scheduleMode) {
                showClassDialog(null);
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
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

    private void markClassesChanged() {
        classesChanged = true;
        saveButton.setEnabled(true);
    }

    private void markDeadlinesChanged() {
        deadlinesChanged = true;
        saveButton.setEnabled(true);
    }

    private void saveChanges() {
        try {
            if (classesChanged) {
                schedule.saveClasses();
                classesChanged = false;
            }
            if (deadlinesChanged) {
                schedule.saveDeadlines();
                deadlinesChanged = false;
            }
            saveButton.setEnabled(classesChanged || deadlinesChanged);
        } catch (Exception exception) {
            System.out.println("Could not save changes: " + exception.getMessage());
        }
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
            setToolTipText("<html>" + escapeHtml(deadline.getCourseCode()) + "<br>"
                    + escapeHtml(deadline.getTitle())
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
                title.setText("<html><strike>" + escapeHtml(deadline.getTitle()) + "</strike></html>");
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
                private Point pressedAt;

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
                    if (event.isPopupTrigger()) {
                        menu.show(DeadlineCard.this, event.getX(), event.getY());
                        return;
                    }
                    pressedAt = event.getPoint();
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (event.isPopupTrigger()) {
                        menu.show(DeadlineCard.this, event.getX(), event.getY());
                        return;
                    }
                    if (pressedAt == null) {
                        return;
                    }
                    boolean moved = Math.abs(event.getX() - pressedAt.x) > 8
                            || Math.abs(event.getY() - pressedAt.y) > 8;
                    pressedAt = null;
                    if (moved) {
                        dropOn(event.getPoint());
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent event) {
                    setCursor(new Cursor(Cursor.MOVE_CURSOR));
                }
            });
        }

        private void dropOn(Point point) {
            Point local = SwingUtilities.convertPoint(DeadlineCard.this, point, weekColumns);
            Component target = weekColumns.getComponentAt(local);
            if (target instanceof JComponent) {
                Object value = ((JComponent) target).getClientProperty("date");
                if (value instanceof LocalDate) {
                    LocalDate date = (LocalDate) value;
                    if (date.equals(deadline.getDueDate()) == false) {
                        deadline.setDueDate(date);
                        markDeadlinesChanged();
                        refreshDeadlines();
                    }
                }
            }
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        private Color readableOn(Color color) {
            double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255.0;
            if (luminance > 0.55) {
                return new Color(0x14121F);
            }
            return Color.WHITE;
        }

        private String escapeHtml(String value) {
            return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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

    private void showClassDialog(ScheduleClass existing) {
        ScheduleClass result = ClassDialog.show(this, null, existing);
        if (result == null) {
            return;
        }
        try {
            if (existing == null) {
                schedule.addClass(result.getCourseCode(), result.getDay(), result.getStart(),
                        result.getEnd(), result.getRoom());
            } else {
                markClassesChanged();
            }
            refresh();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Could not save the class: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteClass(ScheduleClass item) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete " + item.getCourseCode() + " on " + item.getDay() + "?",
                "Delete class", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            schedule.deleteClass(item);
            refresh();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Could not delete the class: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addDeadline() {
        Deadline result = DeadlineDialog.show(this, null, null);
        if (result == null) {
            return;
        }
        try {
            schedule.addDeadline(result.getCourseCode(), result.getTitle(),
                    result.getDueDate(), result.getDueTime());
            weekStart = startOfWeek(result.getDueDate());
            refreshDeadlines();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Could not save the deadline: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static LocalDate startOfWeek(LocalDate date) {
        int offset = date.getDayOfWeek().getValue() % 7;
        return date.minusDays(offset);
    }

    private String formatTime(LocalTime time) {
        return Theme.formatTime12(time);
    }

    private class WeekGrid extends JPanel {
        private final String[] DAYS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        private final int LABEL_WIDTH = 64;
        private final int HEADER_HEIGHT = 28;
        private final int MIN_DURATION = 30;
        private final int SNAP_MINUTES = 5;
        private final int MODE_NONE = 0;
        private final int MODE_MOVE = 1;
        private final int MODE_START = 2;
        private final int MODE_END = 3;

        private ScheduleClass pressedClass;
        private int dragMode = MODE_NONE;
        private boolean dragging;
        private int pressX;
        private int pressY;
        private int clickOffset;
        private LocalTime originalStart;
        private LocalTime originalEnd;
        private String originalDay;
        private LocalTime previewStart;
        private LocalTime previewEnd;
        private String previewDay;

        WeekGrid() {
            setOpaque(false);
            setToolTipText("");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    if (event.isPopupTrigger()) {
                        showClassMenu(event);
                        return;
                    }
                    pressedClass = classAt(event.getPoint());
                    dragging = false;
                    if (pressedClass == null) {
                        return;
                    }
                    pressX = event.getX();
                    pressY = event.getY();
                    originalStart = pressedClass.getStart();
                    originalEnd = pressedClass.getEnd();
                    originalDay = pressedClass.getDay();
                    previewStart = originalStart;
                    previewEnd = originalEnd;
                    previewDay = originalDay;

                    Rectangle bounds = boundsFor(pressedClass);
                    if (event.getY() <= bounds.y + 6) {
                        dragMode = MODE_START;
                    } else if (event.getY() >= bounds.y + bounds.height - 6) {
                        dragMode = MODE_END;
                    } else {
                        dragMode = MODE_MOVE;
                        int pointer = (int) Math.round(minutesAt(event.getY()));
                        int start = originalStart.getHour() * 60 + originalStart.getMinute();
                        clickOffset = pointer - start;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (event.isPopupTrigger()) {
                        showClassMenu(event);
                        reset();
                        return;
                    }
                    if (dragging && pressedClass != null) {
                        pressedClass.setDay(previewDay);
                        pressedClass.setStart(previewStart);
                        pressedClass.setEnd(previewEnd);
                        markClassesChanged();
                    }
                    reset();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    if (dragging == false) {
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent event) {
                    ScheduleClass item = classAt(event.getPoint());
                    if (item == null) {
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        return;
                    }
                    Rectangle bounds = boundsFor(item);
                    if (event.getY() <= bounds.y + 6) {
                        setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                    } else if (event.getY() >= bounds.y + bounds.height - 6) {
                        setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
                    } else {
                        setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    }
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    if (pressedClass == null) {
                        return;
                    }
                    if (dragging == false
                            && Math.abs(event.getX() - pressX) < 3
                            && Math.abs(event.getY() - pressY) < 3) {
                        return;
                    }
                    dragging = true;

                    int snapped = snap((int) Math.round(minutesAt(event.getY())));
                    int startMinutes = originalStart.getHour() * 60 + originalStart.getMinute();
                    int endMinutes = originalEnd.getHour() * 60 + originalEnd.getMinute();
                    int duration = endMinutes - startMinutes;

                    if (dragMode == MODE_MOVE) {
                        int newStart = snapped - clickOffset;
                        newStart = clamp(newStart, gridStartHour() * 60,
                                gridEndHour() * 60 - duration);
                        previewStart = timeAt(newStart);
                        previewEnd = timeAt(newStart + duration);
                        int dayIndex = dayIndexAt(event.getX());
                        if (dayIndex >= 0) {
                            previewDay = DAYS[dayIndex];
                        }
                        setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    } else if (dragMode == MODE_START) {
                        int newStart = clamp(snapped, gridStartHour() * 60,
                                endMinutes - MIN_DURATION);
                        previewStart = timeAt(newStart);
                        previewEnd = originalEnd;
                        previewDay = originalDay;
                        setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                    } else if (dragMode == MODE_END) {
                        int newEnd = clamp(snapped, startMinutes + MIN_DURATION,
                                gridEndHour() * 60);
                        previewEnd = timeAt(newEnd);
                        previewStart = originalStart;
                        previewDay = originalDay;
                        setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
                    }
                    repaint();
                }
            });
        }

        private void reset() {
            pressedClass = null;
            dragMode = MODE_NONE;
            dragging = false;
            originalStart = null;
            originalEnd = null;
            originalDay = null;
            previewStart = null;
            previewEnd = null;
            previewDay = null;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            repaint();
        }

        private void showClassMenu(MouseEvent event) {
            ScheduleClass item = classAt(event.getPoint());
            if (item == null) {
                return;
            }
            JPopupMenu menu = new JPopupMenu();
            JMenuItem edit = new JMenuItem("Edit class");
            edit.addActionListener(e -> showClassDialog(item));
            JMenuItem delete = new JMenuItem("Delete class");
            delete.addActionListener(e -> deleteClass(item));
            menu.add(edit);
            menu.add(delete);
            menu.show(this, event.getX(), event.getY());
        }

        private int gridStartHour() {
            int start = 7;
            List<ScheduleClass> classes = schedule.getClasses();
            for (int index = 0; index < classes.size(); index++) {
                start = Math.min(start, classes.get(index).getStart().getHour());
            }
            return Math.max(0, start - 1);
        }

        private int gridEndHour() {
            int end = 17;
            List<ScheduleClass> classes = schedule.getClasses();
            for (int index = 0; index < classes.size(); index++) {
                ScheduleClass item = classes.get(index);
                int hour = item.getEnd().getHour();
                if (item.getEnd().getMinute() > 0) {
                    hour++;
                }
                end = Math.max(end, hour);
            }
            return Math.min(23, Math.max(17, end + 1));
        }

        private double columnWidth() {
            return (getWidth() - LABEL_WIDTH) / 7.0;
        }

        private double rowHeight() {
            return (getHeight() - HEADER_HEIGHT) / (double) (gridEndHour() - gridStartHour());
        }

        private Rectangle boundsFor(ScheduleClass item) {
            int dayIndex = Arrays.asList(DAYS).indexOf(item.getDay());
            if (dayIndex < 0) {
                return new Rectangle();
            }
            int start = item.getStart().getHour() * 60 + item.getStart().getMinute();
            int end = item.getEnd().getHour() * 60 + item.getEnd().getMinute();
            double rowHeight = rowHeight();
            int x = (int) (LABEL_WIDTH + dayIndex * columnWidth()) + 3;
            int y = (int) (HEADER_HEIGHT + ((start - gridStartHour() * 60) / 60.0) * rowHeight);
            int width = (int) columnWidth() - 6;
            int height = (int) (((end - start) / 60.0) * rowHeight);
            if (height < 18) {
                height = 18;
            }
            return new Rectangle(x, y, width, height);
        }

        private ScheduleClass classAt(Point point) {
            List<ScheduleClass> classes = schedule.getClasses();
            for (int index = classes.size() - 1; index >= 0; index--) {
                ScheduleClass item = classes.get(index);
                if (boundsFor(item).contains(point)) {
                    return item;
                }
            }
            return null;
        }

        private int dayIndexAt(int x) {
            if (x < LABEL_WIDTH) {
                return -1;
            }
            int index = (int) ((x - LABEL_WIDTH) / columnWidth());
            if (index < 0 || index > 6) {
                return -1;
            }
            return index;
        }

        private double minutesAt(int y) {
            return gridStartHour() * 60.0 + ((y - HEADER_HEIGHT) / rowHeight()) * 60.0;
        }

        private int snap(int minutes) {
            return Math.round(minutes / (float) SNAP_MINUTES) * SNAP_MINUTES;
        }

        private LocalTime timeAt(int minutes) {
            int safe = Math.max(0, Math.min(24 * 60 - 1, minutes));
            return LocalTime.of(safe / 60, safe % 60);
        }

        private int clamp(int value, int min, int max) {
            if (value < min) {
                return min;
            }
            if (value > max) {
                return max;
            }
            return value;
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            ScheduleClass item = classAt(event.getPoint());
            if (item == null) {
                return null;
            }
            String room = item.getRoom().isEmpty() ? "" : "<br>" + item.getRoom();
            return "<html>" + item.getCourseCode() + "<br>"
                    + Theme.formatTime12(item.getStart()) + " - " + Theme.formatTime12(item.getEnd())
                    + room + "</html>";
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getWidth() <= LABEL_WIDTH || getHeight() <= HEADER_HEIGHT) {
                return;
            }

            int startHour = gridStartHour();
            int endHour = gridEndHour();
            double columnWidth = columnWidth();
            double rowHeight = rowHeight();

            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(new Color(0x111111));
            for (int index = 0; index < DAYS.length; index++) {
                g2.drawString(DAYS[index], (int) (LABEL_WIDTH + index * columnWidth) + 10, 18);
            }

            g2.setFont(Theme.font(Font.PLAIN, 11));
            g2.setColor(new Color(0x666666));
            for (int hour = startHour; hour < endHour; hour++) {
                int y = (int) (HEADER_HEIGHT + (hour - startHour) * rowHeight);
                g2.drawString(Theme.formatTime12(LocalTime.of(hour % 24, 0)), 6, y + 14);
            }

            g2.setColor(new Color(0xD9D9D9));
            for (int hour = startHour; hour <= endHour; hour++) {
                int y = (int) (HEADER_HEIGHT + (hour - startHour) * rowHeight);
                g2.drawLine(LABEL_WIDTH, y, getWidth(), y);
            }
            for (int index = 0; index <= DAYS.length; index++) {
                int x = (int) (LABEL_WIDTH + index * columnWidth);
                g2.drawLine(x, 0, x, getHeight());
            }

            List<ScheduleClass> classes = schedule.getClasses();
            for (int index = 0; index < classes.size(); index++) {
                drawClass(g2, classes.get(index), startHour, columnWidth, rowHeight);
            }

            if (dragging && previewStart != null) {
                int y = (int) (HEADER_HEIGHT + ((previewStart.getHour() * 60 + previewStart.getMinute()
                        - startHour * 60) / 60.0) * rowHeight);
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[] { 5f, 4f }, 0f));
                g2.drawLine(LABEL_WIDTH, y, getWidth(), y);
            }
        }

        private void drawClass(Graphics2D g2, ScheduleClass item, int startHour,
                               double columnWidth, double rowHeight) {
            String day = item.getDay();
            LocalTime start = item.getStart();
            LocalTime end = item.getEnd();
            boolean preview = dragging && item == pressedClass;
            if (preview) {
                day = previewDay;
                start = previewStart;
                end = previewEnd;
            }

            int dayIndex = Arrays.asList(DAYS).indexOf(day);
            if (dayIndex < 0) {
                return;
            }
            int startMinutes = start.getHour() * 60 + start.getMinute();
            int endMinutes = end.getHour() * 60 + end.getMinute();
            if (endMinutes <= startMinutes) {
                return;
            }

            int x = (int) (LABEL_WIDTH + dayIndex * columnWidth) + 3;
            int y = (int) (HEADER_HEIGHT + ((startMinutes - startHour * 60) / 60.0) * rowHeight);
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

            if (preview) {
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 190));
            } else {
                g2.setColor(color);
            }
            g2.fillRoundRect(x, y, width, height, 8, 8);
            if (preview) {
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(x, y, width - 1, height - 1, 8, 8);
            }

            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(Font.BOLD, 11));
            g2.drawString(item.getCourseCode(), x + 6, y + 14);
            if (height > 26) {
                g2.setFont(Theme.font(Font.PLAIN, 9));
                g2.drawString(Theme.formatTime12(start), x + 6, y + 26);
            }
            if (height > 40) {
                g2.drawString(Theme.formatTime12(end), x + 6, y + 37);
            }
            if (height > 54 && item.getRoom().isEmpty() == false) {
                g2.setFont(Theme.font(Font.PLAIN, 9));
                g2.drawString(item.getRoom(), x + 6, y + 48);
            }
        }
    }
}
