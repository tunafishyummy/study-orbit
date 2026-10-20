package ui.pages;

import data.AppData;
import data.NoteManager;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.Course;
import model.Flashcard;
import model.Note;
import ui.Theme;
import ui.components.RoundedComboBox;
import ui.components.RoundedToggleButton;
import ui.components.RoundedTextField;

// Screen 9: Search across courses, notes, and flashcards
public class SearchPage extends JPanel {
    private final NoteManager manager;
    private final JTextField searchField = new RoundedTextField();
    private final JComboBox<String> courseBox = new RoundedComboBox<>();
    private final DefaultListModel<SearchItem> listModel = new DefaultListModel<>();
    private final JList<SearchItem> list = new JList<>(listModel);
    private final JLabel status = new JLabel();
    private String filter = "All";

    public static class SearchItem {
        public final String type;
        public final String title;
        public final String subtitle;

        public SearchItem(String type, String title, String subtitle) {
            this.type = type;
            this.title = title;
            this.subtitle = subtitle;
        }

        @Override
        public String toString() {
            return "[" + type + "] " + title + (subtitle.isEmpty() ? "" : "  -  " + subtitle);
        }
    }

    public SearchPage() {
        this.manager = AppData.get();
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(header(), BorderLayout.NORTH);
        add(results(), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
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
        JLabel subtitle = Theme.muted("Search across courses, notes, and flashcards");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup group = new ButtonGroup();
        for (String name : new String[]{"All", "Courses", "Notes", "Flashcards"}) {
            JToggleButton button = new RoundedToggleButton(name);
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
        list.setCellRenderer((l, item, index, selected, focus) -> {
            JLabel label = new JLabel("  " + item.toString());
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
        panel.add(Theme.roundedScrollPane(list), BorderLayout.CENTER);
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
        String query = searchField.getText().trim().toLowerCase();
        String courseFilter = (String) courseBox.getSelectedItem();
        listModel.clear();

        // 1. Search Courses
        if ("All".equals(filter) || "Courses".equals(filter)) {
            for (Course c : manager.getCourses()) {
                if (matchesCourse(c, query)) {
                    listModel.addElement(new SearchItem("Course", c.getCode() + " - " + c.getTitle(), ""));
                }
            }
        }

        // 2. Search Notes
        if ("All".equals(filter) || "Notes".equals(filter)) {
            for (Note note : manager.getAllNotes()) {
                if (courseFilter != null && !courseFilter.equals("All courses") && !note.getCourseCode().equals(courseFilter)) {
                    continue;
                }
                if (matchesNote(note, query)) {
                    String courseStr = note.getCourseCode().isEmpty() ? "No course" : note.getCourseCode();
                    listModel.addElement(new SearchItem("Note", note.getTitle(), courseStr));
                }
            }
        }

        // 3. Search Flashcards
        if ("All".equals(filter) || "Flashcards".equals(filter)) {
            for (Flashcard f : manager.getFlashcards()) {
                if (matchesFlashcard(f, query)) {
                    listModel.addElement(new SearchItem("Flashcard", f.getFront(), "Deck: " + f.getDeckTitle()));
                }
            }
        }

        if (listModel.isEmpty()) {
            status.setText("No matches found.");
        } else if (listModel.size() == 1) {
            status.setText("1 result");
        } else {
            status.setText(listModel.size() + " results");
        }
    }

    private boolean matchesCourse(Course c, String query) {
        if (query.isEmpty()) return true;
        return c.getCode().toLowerCase().contains(query) || c.getTitle().toLowerCase().contains(query);
    }

    private boolean matchesNote(Note note, String query) {
        if (query.isEmpty()) return true;
        return note.getTitle().toLowerCase().contains(query) ||
               note.getBody().toLowerCase().contains(query) ||
               note.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query));
    }

    private boolean matchesFlashcard(Flashcard f, String query) {
        if (query.isEmpty()) return true;
        return f.getDeckTitle().toLowerCase().contains(query) ||
               f.getFront().toLowerCase().contains(query) ||
               f.getBack().toLowerCase().contains(query) ||
               f.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query));
    }

    private void styleFilter(JToggleButton button) {
        button.setFont(Theme.BODY);
        button.setForeground(Theme.TEXT);
        button.setBackground(Theme.SURFACE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
    }
}
