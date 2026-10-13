package ui.pages;

import data.AppData;
import data.NoteManager;
import model.Course;
import model.Note;
import ui.Theme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

// Screen 9, search across notes and flashcards
public class SearchPage extends JPanel {
    private final NoteManager manager;
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> courseBox = new JComboBox<>();
    private final DefaultListModel<Note> listModel = new DefaultListModel<>();
    private final JList<Note> list = new JList<>(listModel);
    private final JLabel status = new JLabel();
    private String filter = "All";

    public SearchPage() {
        manager = AppData.get();
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(header(), BorderLayout.NORTH);
        add(results(), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });
        courseBox.addActionListener(e -> refresh());
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                reloadCourses();
                refresh();
            }
        });
        reloadCourses();
        refresh();
    }

    private JPanel header() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = Theme.header("Search");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = Theme.muted("Search what you want like courses, flashcards, notes");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup group = new ButtonGroup();
        for (String name : new String[]{"All", "Notes", "Flashcards"}) {
            JToggleButton button = new JToggleButton(name);
            styleFilter(button);
            if (name.equals("All")) {
                button.setSelected(true);
            }
            button.addActionListener(e -> {
                filter = name;
                refresh();
            });
            group.add(button);
            filters.add(button);
        }
        filters.add(courseBox);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(12));
        header.add(searchField);
        header.add(Box.createVerticalStrut(10));
        header.add(filters);
        return header;
    }

    private JPanel results() {
        JPanel panel = Theme.card();
        panel.setLayout(new BorderLayout(0, 8));

        status.setFont(Theme.BODY);
        status.setForeground(Theme.MUTED);
        panel.add(status, BorderLayout.NORTH);

        list.setFont(Theme.BODY);
        list.setBackground(Theme.SURFACE);
        list.setForeground(Theme.TEXT);
        list.setCellRenderer((l, note, index, selected, focus) -> {
            JLabel label = new JLabel(rowText(note));
            label.setOpaque(true);
            label.setFont(Theme.BODY);
            label.setForeground(Theme.TEXT);
            if (selected) {
                label.setBackground(Theme.SELECT);
            } else {
                label.setBackground(Theme.SURFACE);
            }
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            return label;
        });
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private void reloadCourses() {
        String selected = (String) courseBox.getSelectedItem();
        courseBox.removeAllItems();
        courseBox.addItem("All courses");
        for (Course course : manager.getCourses()) {
            courseBox.addItem(course.getCode());
        }
        if (selected != null) {
            courseBox.setSelectedItem(selected);
        }
    }

    private void refresh() {
        if ("Flashcards".equals(filter)) {
            listModel.clear();
            status.setText("No flashcards yet.");
            return;
        }

        String query = searchField.getText().trim().toLowerCase();
        String course = (String) courseBox.getSelectedItem();
        listModel.clear();
        for (Note note : manager.getAllNotes()) {
            if (course != null && !course.equals("All courses") && !note.getCourseCode().equals(course)) {
                continue;
            }
            if (!query.isEmpty() && !matches(note, query)) {
                continue;
            }
            listModel.addElement(note);
        }

        if (listModel.isEmpty()) {
            status.setText("No matches found.");
        } else if (listModel.size() == 1) {
            status.setText("1 result");
        } else {
            status.setText(listModel.size() + " results");
        }
    }

    private boolean matches(Note note, String query) {
        if (note.getTitle().toLowerCase().contains(query)) {
            return true;
        }
        if (note.getBody().toLowerCase().contains(query)) {
            return true;
        }
        for (String tag : note.getTags()) {
            if (tag.toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }

    private String rowText(Note note) {
        String course = note.getCourseCode();
        if (course.isEmpty()) {
            course = "No course";
        }
        return note.getTitle() + "  -  " + course;
    }

    private void styleFilter(JToggleButton button) {
        button.setFont(Theme.BODY);
        button.setForeground(Theme.TEXT);
        button.setBackground(Theme.SURFACE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
    }
}
