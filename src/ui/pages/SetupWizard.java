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
    private final JTextField deadlineTitle = new RoundedTextField();
    private final JTextField deadlineDate = new RoundedTextField();

    public SetupWizard() {
        setTitle("Set up Study Orbit");
        setModal(true);
        setSize(520, 500);
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
        panel.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.TITLE);
        heading.setForeground(Theme.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(heading);
        panel.add(Box.createVerticalStrut(6));

        JLabel hint = Theme.muted(subtitle);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(hint);
        panel.add(Box.createVerticalStrut(20));
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

        JList<String> list = new JList<String>(courseModel);
        list.setBackground(Theme.SURFACE);
        list.setForeground(Theme.TEXT);
        list.setFont(Theme.BODY);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(400, 110));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scroll);
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
        JPanel panel = page("Schedule", "Add one class and one deadline. You can add more later.");

        panel.add(label("Class course"));
        courseBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        courseBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(courseBox);
        panel.add(Box.createVerticalStrut(8));

        panel.add(label("Day"));
        dayBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        dayBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(dayBox);
        panel.add(Box.createVerticalStrut(8));

        panel.add(label("Start, end and room"));
        JPanel times = new JPanel(new GridLayout(1, 3, 8, 0));
        times.setOpaque(false);
        times.setAlignmentX(Component.LEFT_ALIGNMENT);
        startField.setText("9:00");
        endField.setText("10:30");
        times.add(startField);
        times.add(endField);
        times.add(roomField);
        panel.add(times);
        panel.add(Box.createVerticalStrut(12));

        panel.add(label("Deadline title"));
        deadlineTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        deadlineTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(deadlineTitle);
        panel.add(Box.createVerticalStrut(8));

        panel.add(label("Deadline date (2026-10-30)"));
        deadlineDate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        deadlineDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(deadlineDate);
        panel.add(Box.createVerticalStrut(16));

        panel.add(buttons("Back", () -> steps.show(stepPanel, "courses"), "Finish", this::finish));
        panel.add(Box.createVerticalGlue());
        return panel;
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

            saveClass();
            saveDeadline();

            finished = true;
            dispose();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void saveClass() {
        String course = (String) courseBox.getSelectedItem();
        if (course == null || course.isEmpty()) {
            return;
        }
        String start = startField.getText().trim();
        String end = endField.getText().trim();
        if (start.isEmpty() || end.isEmpty()) {
            return;
        }
        try {
            LocalTime startTime = LocalTime.parse(start, TIME_FORMAT);
            LocalTime endTime = LocalTime.parse(end, TIME_FORMAT);
            AppData.schedule().addClass(course, (String) dayBox.getSelectedItem(),
                    startTime, endTime, roomField.getText().trim());
        } catch (Exception exception) {
            // skip an invalid class instead of blocking the setup
        }
    }

    private void saveDeadline() {
        String title = deadlineTitle.getText().trim();
        String date = deadlineDate.getText().trim();
        if (title.isEmpty() || date.isEmpty()) {
            return;
        }
        String course = (String) courseBox.getSelectedItem();
        if (course == null) {
            course = "";
        }
        try {
            AppData.schedule().addDeadline(course, title, LocalDate.parse(date));
        } catch (Exception exception) {
            // skip an invalid deadline instead of blocking the setup
        }
    }
}
