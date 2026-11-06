package ui.dialogs;

import data.AppData;
import java.awt.*;
import java.time.LocalTime;
import java.util.List;
import javax.swing.*;
import model.Course;
import model.ScheduleClass;
import ui.Theme;
import ui.components.RoundedComboBox;
import ui.components.RoundedTextField;
import ui.components.TimePicker;

public final class ClassDialog {
    private static final String[] DAYS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

    private ClassDialog() {}

    public static ScheduleClass show(Component parent, String lockedCourseCode, ScheduleClass existing) {
        RoundedComboBox<String> courseBox = new RoundedComboBox<>();
        boolean locked = lockedCourseCode != null && lockedCourseCode.isBlank() == false;
        if (locked) {
            courseBox.addItem(lockedCourseCode);
        } else {
            List<Course> courses = AppData.get().getCourses();
            for (int index = 0; index < courses.size(); index++) {
                courseBox.addItem(courses.get(index).getCode());
            }
        }
        if (courseBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(parent, "Add a course first, then add its class time.");
            return null;
        }

        courseBox.setPreferredSize(new Dimension(340, 34));
        RoundedComboBox<String> dayBox = new RoundedComboBox<>(DAYS);
        dayBox.setPreferredSize(new Dimension(340, 34));
        TimePicker startPicker = new TimePicker(existing == null ? LocalTime.of(9, 0) : existing.getStart());
        startPicker.setPreferredSize(new Dimension(340, 40));
        TimePicker endPicker = new TimePicker(existing == null ? LocalTime.of(10, 30) : existing.getEnd());
        endPicker.setPreferredSize(new Dimension(340, 40));
        RoundedTextField roomField = new RoundedTextField();
        roomField.setPreferredSize(new Dimension(340, 34));

        if (existing != null) {
            if (locked == false) {
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
            dayBox.setSelectedItem(existing.getDay());
            roomField.setText(existing.getRoom());
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.add(new JLabel("Course"));
        form.add(courseBox);
        form.add(new JLabel("Day"));
        form.add(dayBox);
        form.add(new JLabel("Start time"));
        form.add(startPicker);
        form.add(new JLabel("End time"));
        form.add(endPicker);
        form.add(new JLabel("Room"));
        form.add(roomField);

        while (true) {
            boolean accepted = FormDialog.show(parent,
                    existing == null ? "Add class" : "Edit class", form);
            if (accepted == false) {
                return null;
            }

            String courseCode = (String) courseBox.getSelectedItem();
            if (courseCode == null || courseCode.isBlank()) {
                JOptionPane.showMessageDialog(parent, "Choose the course for this class.",
                        "Check your input", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            LocalTime start = startPicker.getTime();
            LocalTime end = endPicker.getTime();
            if (end.isAfter(start) == false) {
                JOptionPane.showMessageDialog(parent, "The end time must be after the start time.",
                        "Check the time", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            String day = (String) dayBox.getSelectedItem();
            String room = roomField.getText().trim();
            if (existing == null) {
                return new ScheduleClass(courseCode, day, start, end, room);
            }
            existing.setCourseCode(courseCode);
            existing.setDay(day);
            existing.setStart(start);
            existing.setEnd(end);
            existing.setRoom(room);
            return existing;
        }
    }
}
