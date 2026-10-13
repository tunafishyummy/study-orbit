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
    private final CardLayout states = new CardLayout();
    private final JPanel statePanel = new JPanel(states);
    private final JComboBox<String> courseBox = new JComboBox<>();

    public QuizzesPage() {
        manager = AppData.get();
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(Theme.header("Quizzes"), BorderLayout.NORTH);

        statePanel.setOpaque(false);
        statePanel.add(buildCard(), "Build");
        statePanel.add(questionCard(), "Question");
        add(statePanel, BorderLayout.CENTER);

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

        JButton start = Theme.primaryButton("Start quiz");
        start.addActionListener(e -> states.show(statePanel, "Question"));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(start);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel questionCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel progress = new JLabel("Question 3 of 10");
        progress.setFont(Theme.HEADING);
        progress.setForeground(Theme.TEXT);
        JButton end = Theme.button("End quiz");
        end.addActionListener(e -> states.show(statePanel, "Build"));
        top.add(progress, BorderLayout.WEST);
        top.add(end, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel middle = new JPanel();
        middle.setOpaque(false);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        JLabel question = new JLabel("What is inheritance in object-oriented programming?");
        question.setFont(Theme.HEADING);
        question.setForeground(Theme.TEXT);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);
        middle.add(question);
        middle.add(Box.createVerticalStrut(12));

        ButtonGroup options = new ButtonGroup();
        for (String option : new String[]{
                "A class that reuses the fields and methods of another class",
                "A loop that repeats until a condition is false",
                "A method that calls itself with a smaller input",
                "A variable that can hold any type of value"}) {
            JToggleButton button = new JToggleButton(option);
            button.setFont(Theme.BODY);
            button.setForeground(Theme.TEXT);
            button.setBackground(Theme.SURFACE);
            button.setFocusPainted(false);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            options.add(button);
            middle.add(button);
            middle.add(Box.createVerticalStrut(8));
        }
        card.add(middle, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(Theme.button("Flag"));
        footer.add(Theme.primaryButton("Next"));
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
