package ui.dialogs;

import data.AppData;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.*;
import model.Course;
import model.Deadline;
import ui.Theme;
import ui.components.RoundedComboBox;
import ui.components.RoundedTextField;
import ui.components.TimePicker;

public final class DeadlineDialog {
    private DeadlineDialog() {}

    public static Deadline show(Component parent, String lockedCourseCode, Deadline existing) {
        RoundedComboBox<String> courseBox = new RoundedComboBox<>();
        boolean locked = lockedCourseCode != null && lockedCourseCode.isBlank() == false;
        if (locked) {
            courseBox.addItem(lockedCourseCode);
        } else {
            courseBox.addItem("");
            List<Course> courses = AppData.get().getCourses();
            for (int index = 0; index < courses.size(); index++) {
                courseBox.addItem(courses.get(index).getCode());
            }
        }

        RoundedTextField titleField = new RoundedTextField();
        titleField.setPreferredSize(new Dimension(340, 34));
        RoundedTextField dateField = new RoundedTextField(12);
        dateField.setPreferredSize(new Dimension(340, 34));
        TimePicker timePicker = new TimePicker(LocalTime.of(23, 59));
        timePicker.setPreferredSize(new Dimension(340, 40));
        courseBox.setPreferredSize(new Dimension(340, 34));

        if (existing != null) {
            titleField.setText(existing.getTitle());
            dateField.setText(existing.getDueDate().toString());
            timePicker.setTime(existing.getDueTime());
            if (locked == false && courseBox.getItemCount() > 0) {
                boolean found = false;
                for (int index = 0; index < courseBox.getItemCount(); index++) {
                    if (courseBox.getItemAt(index).equals(existing.getCourseCode())) {
                        found = true;
                        break;
                    }
                }
                if (found == false && existing.getCourseCode().isBlank() == false) {
                    courseBox.addItem(existing.getCourseCode());
                }
                courseBox.setSelectedItem(existing.getCourseCode());
            }
        } else {
            dateField.setText(LocalDate.now().toString());
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.add(new JLabel("Title"));
        form.add(titleField);
        form.add(new JLabel("Due date (2026-10-30)"));
        form.add(dateField);
        form.add(new JLabel("Due time"));
        form.add(timePicker);
        form.add(new JLabel(locked ? "Course" : "Course (optional)"));
        form.add(courseBox);

        while (true) {
            boolean accepted = FormDialog.show(parent,
                    existing == null ? "Add deadline" : "Edit deadline", form);
            if (accepted == false) {
                return null;
            }

            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Give the deadline a title.",
                        "Check your input", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            LocalDate dueDate;
            try {
                dueDate = LocalDate.parse(dateField.getText().trim());
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(parent, "Use a date like 2026-10-30.",
                        "Check the date", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            String courseCode = (String) courseBox.getSelectedItem();
            if (courseCode == null) {
                courseCode = "";
            }
            LocalTime dueTime = timePicker.getTime();
            if (existing == null) {
                return new Deadline(courseCode, title, dueDate, dueTime);
            }
            existing.setCourseCode(courseCode);
            existing.setTitle(title);
            existing.setDueDate(dueDate);
            existing.setDueTime(dueTime);
            return existing;
        }
    }
}
