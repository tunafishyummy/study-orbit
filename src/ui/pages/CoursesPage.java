package ui.pages;

import data.AppData;
import data.NoteManager;
import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import model.Course;
import ui.Theme;
import ui.components.RoundedTextField;

public class CoursesPage extends JPanel {
    private final NoteManager manager;
    private final DefaultListModel<Course> listModel = new DefaultListModel<>();
    private final JList<Course> list = new JList<>(listModel);
    private final JTextField codeField = new RoundedTextField(10);
    private final JTextField titleField = new RoundedTextField(20);

    public CoursesPage() {
        this.manager = AppData.get();
        setBackground(Theme.BG);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        JLabel header = new JLabel("Your Courses");
        header.setFont(Theme.TITLE);
        add(header, BorderLayout.NORTH);

        list.setFont(Theme.BODY);
        list.setBackground(Theme.SURFACE);
        list.setForeground(Theme.TEXT);
        list.setCellRenderer((l, c, i, sel, focus) -> {
            JLabel lbl = new JLabel("  " + c);
            lbl.setOpaque(true);
            lbl.setFont(Theme.BODY);
            lbl.setBorder(BorderFactory.createMatteBorder(0, 10, 0, 0, Theme.PRIMARY));
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
            manager.addCourse(codeField.getText(), titleField.getText(), Theme.toHex(Theme.PRIMARY));
            clearForm();
        }));
        JButton update = Theme.button("Update selected");
        update.addActionListener(e -> run(() -> {
            Course sel = list.getSelectedValue();
            if (sel == null) throw new IllegalArgumentException("Select a course to update.");
            manager.updateCourse(sel.getCode(), codeField.getText(), titleField.getText(), Theme.toHex(Theme.PRIMARY));
        }));
        JButton delete = Theme.button("Delete selected");
        delete.addActionListener(e -> run(() -> {
            Course sel = list.getSelectedValue();
            if (sel == null) throw new IllegalArgumentException("Select a course to delete.");
            int ok = JOptionPane.showConfirmDialog(this,
                    "Delete " + sel + "? Its notes will be kept without a course.", "Delete course",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) { manager.deleteCourse(sel.getCode()); clearForm(); }
        }));
        form.add(add);
        form.add(update);
        form.add(delete);
        add(form, BorderLayout.EAST);

        EventQueue.invokeLater(() -> {
            refresh();
        });
    }

    private void clearForm() { codeField.setText(""); titleField.setText(""); list.clearSelection(); }

    // selects one course in the list, used when a search result is clicked
    public void showCourse(Course course) {
        refresh();
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getCode().equals(course.getCode())) {
                list.setSelectedIndex(i);
                list.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    void refresh() {
        listModel.clear();
        for (Course c : manager.getCourses()) listModel.addElement(c);
    }

    private interface Action { void run() throws IOException; }

    private void run(Action a) {
        try {
            a.run();
            refresh();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check your input", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}