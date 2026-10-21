package ui.pages;

import data.AppData;
import model.Course;
import model.Profile;
import ui.Theme;
import ui.components.RoundedComboBox;
import ui.components.RoundedTextField;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// the one time setup shown for a brand new profile
public class SetupWizard extends JDialog {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private boolean finished;

    private final CardLayout steps = new CardLayout();
    private final JPanel stepPanel = new JPanel(steps);

    private final JTextField nameField = new RoundedTextField();
    private final JTextField codeField = new RoundedTextField();
    private final JTextField titleField = new RoundedTextField();
    private final DefaultListModel<String> courseModel = new DefaultListModel<String>();
    private final JComboBox<String> courseBox = new RoundedComboBox<>();
    private final JComboBox<String> dayBox = new RoundedComboBox<>(
            new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" });
    private final JTextField startField = new RoundedTextField();
    private final JTextField endField = new RoundedTextField();
    private final JTextField roomField = new RoundedTextField();
    private final DefaultListModel<String> classModel = new DefaultListModel<String>();
    private final JTextField deadlineTitle = new RoundedTextField();
    private final JTextField deadlineDate = new RoundedTextField();
    private final JTextField deadlineTime = new RoundedTextField();
    private final DefaultListModel<String> deadlineModel = new DefaultListModel<String>();

    public SetupWizard() {
        setTitle("Set up Study Orbit");
        setModal(true);
        setSize(560, 640);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);

        stepPanel.setBackground(Theme.BG);
        stepPanel.add(buildNameStep(), "name");
        stepPanel.add(buildCourseStep(), "courses");
        stepPanel.add(buildScheduleStep(), "schedule");
        add(stepPanel);
        steps.show(stepPanel, "name");
    }

    public boolean isFinished() {
        return finished;
    }

    private JPanel page(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.TITLE);
        heading.setForeground(Theme.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(heading);
        panel.add(Box.createVerticalStrut(6));

        JLabel hint = Theme.muted(subtitle);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(hint);
        panel.add(Box.createVerticalStrut(16));
        return panel;
    }

    private JPanel buildNameStep() {
        JPanel panel = page("Welcome", "What should we call you?");

        panel.add(label("Your name"));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(20));

        JButton next = Theme.primaryButton("Next");
        next.setAlignmentX(Component.LEFT_ALIGNMENT);
        next.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your name.");
                return;
            }
            steps.show(stepPanel, "courses");
        });
        panel.add(next);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildCourseStep() {
        JPanel panel = page("Your courses", "Add the subjects you are taking this semester.");

        panel.add(label("Course code"));
        codeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        codeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(codeField);
        panel.add(Box.createVerticalStrut(8));

        panel.add(label("Course title"));
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleField);
        panel.add(Box.createVerticalStrut(8));

        JButton add = Theme.button("Add course");
        add.setAlignmentX(Component.LEFT_ALIGNMENT);
        add.addActionListener(e -> addCourse());
        panel.add(add);
        panel.add(Box.createVerticalStrut(10));

        panel.add(listScroll(courseModel, 110));
        panel.add(Box.createVerticalStrut(14));

        panel.add(buttons("Back", () -> steps.show(stepPanel, "name"),
                "Next", () -> {
                    refreshCourseBox();
                    steps.show(stepPanel, "schedule");
                }));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildScheduleStep() {
        JPanel panel = page("Schedule", "Add as many classes and deadlines as you need.");

        panel.add(label("Class"));
        JPanel classRow = new JPanel(new GridLayout(0, 2, 8, 8));
        classRow.setOpaque(false);
        classRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        classRow.add(courseBox);
        classRow.add(dayBox);
        startField.setText("9:00");
        endField.setText("10:30");
        classRow.add(startField);
        classRow.add(endField);
        classRow.add(roomField);
        JButton addClassButton = Theme.button("Add class");
        addClassButton.addActionListener(e -> addClassToList());
        classRow.add(addClassButton);
        panel.add(classRow);
        panel.add(Box.createVerticalStrut(6));
        JLabel timeHint = Theme.muted("Times look like 9:00 and 10:30, room is optional");
        timeHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(timeHint);
        panel.add(Box.createVerticalStrut(8));
        panel.add(listScroll(classModel, 90));
        panel.add(Box.createVerticalStrut(14));

        panel.add(label("Deadline"));
        JLabel deadlineHint = Theme.muted("Title, date (2026-10-30) and time (23:59)");
        deadlineHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(deadlineHint);
        JPanel deadlineRow = new JPanel(new GridLayout(0, 2, 8, 8));
        deadlineRow.setOpaque(false);
        deadlineRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        deadlineDate.setText(LocalDate.now().plusDays(7).toString());
        deadlineTime.setText("23:59");
        deadlineRow.add(deadlineTitle);
        deadlineRow.add(deadlineDate);
        deadlineRow.add(deadlineTime);
        JButton addDeadlineButton = Theme.button("Add deadline");
        addDeadlineButton.addActionListener(e -> addDeadlineToList());
        deadlineRow.add(addDeadlineButton);
        panel.add(deadlineRow);
        panel.add(Box.createVerticalStrut(8));
        panel.add(listScroll(deadlineModel, 90));
        panel.add(Box.createVerticalStrut(14));

        panel.add(buttons("Back", () -> steps.show(stepPanel, "courses"), "Finish", this::finish));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JScrollPane listScroll(DefaultListModel<String> model, int height) {
        JList<String> list = new JList<String>(model);
        list.setBackground(Theme.SURFACE);
        list.setForeground(Theme.TEXT);
        list.setFont(Theme.BODY);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(400, height));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    private JPanel buttons(String leftText, Runnable leftAction, String rightText, Runnable rightAction) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton left = Theme.button(leftText);
        left.addActionListener(e -> leftAction.run());
        row.add(left, BorderLayout.WEST);

        JButton right = Theme.primaryButton(rightText);
        right.addActionListener(e -> rightAction.run());
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addCourse() {
        String code = codeField.getText().trim();
        String title = titleField.getText().trim();
        if (code.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the course code and title.");
            return;
        }
        try {
            AppData.get().addCourse(code, title, Theme.toHex(Theme.nextCourseColor(usedColors())));
            courseModel.addElement(code + " - " + title);
            codeField.setText("");
            titleField.setText("");
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void addClassToList() {
        String course = (String) courseBox.getSelectedItem();
        if (course == null || course.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add a course first.");
            return;
        }
        try {
            LocalTime startTime = LocalTime.parse(startField.getText().trim(), TIME_FORMAT);
            LocalTime endTime = LocalTime.parse(endField.getText().trim(), TIME_FORMAT);
            AppData.schedule().addClass(course, (String) dayBox.getSelectedItem(),
                    startTime, endTime, roomField.getText().trim());
            classModel.addElement(course + "  " + dayBox.getSelectedItem() + "  "
                    + startField.getText().trim() + " - " + endField.getText().trim()
                    + "  " + roomField.getText().trim());
            roomField.setText("");
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Use a time like 9:00 or 10:30.");
        }
    }

    private void addDeadlineToList() {
        String title = deadlineTitle.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the deadline title.");
            return;
        }
        String course = (String) courseBox.getSelectedItem();
        if (course == null) {
            course = "";
        }
        try {
            LocalDate dueDate = LocalDate.parse(deadlineDate.getText().trim());
            LocalTime dueTime = LocalTime.parse(deadlineTime.getText().trim(), TIME_FORMAT);
            AppData.schedule().addDeadline(course, title, dueDate, dueTime);
            deadlineModel.addElement(title + "  " + dueDate + "  " + deadlineTime.getText().trim());
            deadlineTitle.setText("");
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Use a date like 2026-10-30 and a time like 23:59.");
        }
    }

    private List<String> usedColors() {
        List<String> used = new ArrayList<String>();
        List<Course> courses = AppData.get().getCourses();
        for (int index = 0; index < courses.size(); index++) {
            used.add(courses.get(index).getColorHex());
        }
        return used;
    }

    private void refreshCourseBox() {
        courseBox.removeAllItems();
        List<Course> courses = AppData.get().getCourses();
        for (int index = 0; index < courses.size(); index++) {
            courseBox.addItem(courses.get(index).getCode());
        }
    }

    private void finish() {
        try {
            Profile profile = AppData.profiles().addProfile(
                    nameField.getText().trim(), Theme.toHex(Theme.nextCourseColor(usedColors())));
            AppData.profiles().setActiveProfile(profile);
            AppData.profiles().finishFirstRun();
            finished = true;
            dispose();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }
}
