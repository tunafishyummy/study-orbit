package ui.pages;

import data.AppData;
import data.NoteManager;
import model.Course;
import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 7, build a quiz and try a sample question
public class QuizzesPage extends JPanel {
    private final NoteManager manager;
    private final JComboBox<String> courseBox = new JComboBox<>();

    public QuizzesPage() {
        manager = AppData.get();
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(Theme.header("Quizzes"), BorderLayout.NORTH);
        add(buildCard(), BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                reloadCourses();
            }
        });
        reloadCourses();
    }

    private JPanel buildCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 14));

        JLabel heading = new JLabel("Create a quiz");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.TEXT);
        card.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JSpinner count = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
        form.add(labeled("Course", courseBox));
        form.add(Box.createVerticalStrut(10));
        form.add(labeled("Number of questions", count));
        form.add(Box.createVerticalStrut(10));
        form.add(labeled("Question types", null));
        form.add(check("Multiple choice", true));
        form.add(check("Identification", true));
        form.add(check("True or false", true));
        card.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(Theme.primaryButton("Start quiz"));
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        JLabel label = new JLabel(text + "  ");
        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT);
        row.add(label);
        if (field != null) {
            row.add(field);
        }
        return row;
    }

    private JCheckBox check(String text, boolean selected) {
        JCheckBox box = new JCheckBox(text, selected);
        box.setFont(Theme.BODY);
        box.setForeground(Theme.TEXT);
        box.setOpaque(false);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    private void reloadCourses() {
        courseBox.removeAllItems();
        for (Course course : manager.getCourses()) {
            courseBox.addItem(course.getCode());
        }
        if (courseBox.getItemCount() == 0) {
            courseBox.addItem("CS 002");
        }
    }
}
