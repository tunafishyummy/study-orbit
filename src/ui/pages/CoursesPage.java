package ui.pages;

import data.AppData;
import data.NoteManager;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import model.Course;
import model.Deadline;
import model.ScheduleClass;
import ui.Theme;
import ui.components.RoundedTextField;

public class CoursesPage extends JPanel {
    private final NoteManager manager;
    private final Consumer<Course> openCourse;
    private final DefaultListModel<Course> listModel = new DefaultListModel<>();
    private final JList<Course> list = new JList<>(listModel);
    private final JTextField codeField = new RoundedTextField(10);
    private final JTextField titleField = new RoundedTextField(20);

    public CoursesPage() {
        this(course -> {});
    }

    public CoursesPage(Consumer<Course> openCourse) {
        this.openCourse = openCourse;
        this.manager = AppData.get();
        setBackground(Theme.BG);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        JLabel header = new JLabel("Your Courses");
        header.setFont(Theme.TITLE);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hint = Theme.muted("Click a course to open its notes, deadlines, and schedule.");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(header);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(hint);
        add(headerPanel, BorderLayout.NORTH);

        list.setFont(Theme.BODY);
        list.setBackground(Theme.SURFACE);
        list.setForeground(Theme.TEXT);
        list.setCursor(new Cursor(Cursor.HAND_CURSOR));
        list.setCellRenderer((l, c, i, sel, focus) -> {
            JLabel lbl = new JLabel("  " + c);
            lbl.setOpaque(true);
            lbl.setFont(Theme.BODY);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 10, 0, 0, Theme.parse(c.getColorHex())));
            lbl.setPreferredSize(new Dimension(100, 32));
            if (sel) {
                lbl.setBackground(Theme.SELECT);
                lbl.setForeground(Color.WHITE);
            } else {
                lbl.setBackground(Theme.SURFACE);
                lbl.setForeground(Theme.TEXT);
            }
            return lbl;
        });
        list.addListSelectionListener(e -> {
            Course c = list.getSelectedValue();
            if (c != null) {
                codeField.setText(c.getCode());
                titleField.setText(c.getTitle());
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() != 1 || event.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                int index = list.locationToIndex(event.getPoint());
                if (index < 0 || index >= listModel.size()) {
                    return;
                }
                openCourse.accept(listModel.get(index));
            }
        });
        add(Theme.roundedScrollPane(list), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(260, 0));
        form.add(new JLabel("Course code (e.g. CS 002)"));
        form.add(codeField);
        form.add(new JLabel("Course title"));
        form.add(titleField);
        JButton add = Theme.primaryButton("Add course");
        add.addActionListener(e -> run(() -> {
            List<String> usedColors = new ArrayList<String>();
            for (Course each : manager.getCourses()) {
                usedColors.add(each.getColorHex());
            }
            Color nextColor = Theme.nextCourseColor(usedColors);
            manager.addCourse(codeField.getText(), titleField.getText(), Theme.toHex(nextColor));
            clearForm();
        }));
        JButton update = Theme.button("Update selected");
        update.addActionListener(e -> run(() -> {
            Course selected = list.getSelectedValue();
            if (selected == null) {
                throw new IllegalArgumentException("Select a course to update.");
            }
            String oldCode = selected.getCode();
            manager.updateCourse(oldCode, codeField.getText(), titleField.getText(), selected.getColorHex());
            AppData.schedule().renameCourse(oldCode, codeField.getText().trim());
        }));
        JButton delete = Theme.button("Delete selected");
        delete.addActionListener(e -> run(() -> {
            Course selected = list.getSelectedValue();
            if (selected == null) {
                throw new IllegalArgumentException("Select a course to delete.");
            }
            int deadlineCount = 0;
            for (Deadline deadline : AppData.schedule().getDeadlines()) {
                if (deadline.getCourseCode().equalsIgnoreCase(selected.getCode())) {
                    deadlineCount++;
                }
            }
            int classCount = 0;
            for (ScheduleClass item : AppData.schedule().getClasses()) {
                if (item.getCourseCode().equalsIgnoreCase(selected.getCode())) {
                    classCount++;
                }
            }
            String message = "Delete " + selected + "?\n\n"
                    + "Its notes will be kept without a course.\n"
                    + deadlineCount + " deadlines will be kept without a course.\n"
                    + classCount + " class meetings will be removed.";
            int ok = JOptionPane.showConfirmDialog(this, message, "Delete course",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                manager.deleteCourse(selected.getCode());
                AppData.schedule().deleteCourseData(selected.getCode());
                clearForm();
            }
        }));
        form.add(add);
        form.add(update);
        form.add(delete);
        add(form, BorderLayout.EAST);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                refresh();
            }
        });
        EventQueue.invokeLater(this::refresh);
    }

    private void clearForm() {
        codeField.setText("");
        titleField.setText("");
        list.clearSelection();
    }

    // selects one course in the list
    public void showCourse(Course course) {
        refresh();
        for (int index = 0; index < listModel.size(); index++) {
            if (listModel.get(index).getCode().equals(course.getCode())) {
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
                break;
            }
        }
    }

    public void refresh() {
        listModel.clear();
        for (Course course : manager.getCourses()) {
            listModel.addElement(course);
        }
    }

    private interface Action {
        void run() throws IOException;
    }

    private void run(Action action) {
        try {
            action.run();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Check your input", JOptionPane.WARNING_MESSAGE);
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, "Could not save: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            refresh();
        }
    }
}
