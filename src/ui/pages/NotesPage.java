package ui.pages;

import ai.AiClient;
import ai.AiConfig;
import ai.StudyGenerator;
import data.AIFlashcardService;
import data.AppData;
import data.NoteManager;
import data.PlainText;
import data.SettingsManager;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.Course;
import model.Note;
import ui.ImportDocumentDialog;
import ui.Theme;
import ui.components.NativeFileDialog;
import ui.components.ProcessingCard;
import ui.components.RoundedComboBox;
import ui.components.RoundedPanel;
import ui.components.RoundedTextArea;
import ui.components.RoundedTextField;

// notes page with a reader and the full editor
public class NotesPage extends JPanel {
    private static final String ALL = "All courses";
    private static final String NONE = "No course";
    private static final String OCR_PROMPT =
            "Extract all readable text from this image accurately and keep the original wording and order. "
            + "Return plain text only, with no markdown symbols like #, ** or ---.";
    private static final String OCR_SYSTEM = "You read text from images and return only the plain text.";
    private static final DateTimeFormatter NOTE_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final NoteManager manager;
    private final JPanel headerPanel = new JPanel(new BorderLayout());
    private final JComboBox<String> filterBox = new RoundedComboBox<>();
    private final DefaultListModel<Note> listModel = new DefaultListModel<>();
    private final JList<Note> list = new JList<>(listModel);
    private final JTextField titleField = new RoundedTextField();
    private final JComboBox<String> courseBox = new RoundedComboBox<>();
    private final JTextField tagsField = new RoundedTextField();
    private final JTextArea bodyArea = new RoundedTextArea();
    private final RoundedTextField noteSearch = new RoundedTextField();
    private final JLabel listLabel = new JLabel("Created notes");
    private final JButton newNoteButton = Theme.primaryButton("+ New note");
    private CardLayout centerModes;
    private JPanel centerPanel;
    private CardLayout readerCards;
    private JPanel readerPanel;
    private JTextArea readerBody;
    private JLabel readerTitle;
    private JLabel readerMeta;
    private JLabel emptyTitle;
    private JLabel emptyHint;
    private JButton editNoteButton;
    private ProcessingCard processingCard;
    private Note current;
    private String lockedCourseCode;
    private boolean embedded;
    private boolean loading;
    private boolean populating;
    private boolean dirty;
    private boolean readerMode = true;
    private String appliedFilter = ALL;
    private String lastCourseValue;
    private Runnable changeListener;

    public NotesPage() {
        this(false);
    }

    public NotesPage(boolean embedded) {
        this.manager = AppData.get();
        this.embedded = embedded;
        setBackground(Theme.BG);
        setLayout(new BorderLayout(12, 12));

        JLabel pageTitle = new JLabel("Notes");
        pageTitle.setFont(Theme.TITLE);
        headerPanel.setOpaque(false);
        headerPanel.add(pageTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        add(buildSidebar(), BorderLayout.WEST);

        centerModes = new CardLayout();
        centerPanel = new JPanel(centerModes);
        centerPanel.setOpaque(false);
        centerPanel.add(buildReader(), "reader");
        centerPanel.add(buildEditor(), "editor");
        add(centerPanel, BorderLayout.CENTER);

        installDirtyTracking();
        setEmbedded(embedded);

        newNoteButton.addActionListener(e -> startNewNote());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                refresh();
            }
        });
        EventQueue.invokeLater(this::reload);
    }

    private JPanel buildSidebar() {
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(260, 0));

        JPanel filters = new JPanel();
        filters.setOpaque(false);
        filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS));

        listLabel.setFont(Theme.font(Font.BOLD, 13));
        listLabel.setForeground(Theme.MUTED);
        listLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filters.add(listLabel);
        filters.add(Box.createVerticalStrut(6));

        filterBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        filterBox.addActionListener(e -> filterChanged());
        filters.add(filterBox);
        filters.add(Box.createVerticalStrut(6));

        JLabel searchLabel = Theme.muted("Search notes");
        searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filters.add(searchLabel);
        filters.add(Box.createVerticalStrut(3));
        noteSearch.setToolTipText("Search notes");
        noteSearch.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        noteSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) { searchChanged(); }
            public void removeUpdate(DocumentEvent event) { searchChanged(); }
            public void changedUpdate(DocumentEvent event) { searchChanged(); }
        });
        filters.add(noteSearch);
        left.add(filters, BorderLayout.NORTH);

        list.setFont(Theme.BODY);
        list.setBackground(Theme.SURFACE);
        list.setForeground(Theme.TEXT);
        list.setCursor(new Cursor(Cursor.HAND_CURSOR));
        list.setCellRenderer(new NoteRowRenderer());
        list.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || loading) {
                return;
            }
            Note selected = list.getSelectedValue();
            if (selected == null || selected == current) {
                return;
            }
            if (confirmDiscard() == false) {
                restoreSelection();
                return;
            }
            open(selected);
        });
        left.add(Theme.roundedScrollPane(list), BorderLayout.CENTER);
        left.add(newNoteButton, BorderLayout.SOUTH);
        return left;
    }

    private JPanel buildReader() {
        readerCards = new CardLayout();
        readerPanel = new JPanel(readerCards);
        readerPanel.setOpaque(false);

        RoundedPanel document = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS, Theme.BORDER);
        document.setLayout(new BorderLayout(0, 12));
        document.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        readerTitle = new JLabel(" ");
        readerTitle.setFont(Theme.font(Font.BOLD, 22));
        readerTitle.setForeground(Theme.TEXT);
        readerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        readerMeta = new JLabel(" ");
        readerMeta.setFont(Theme.BODY);
        readerMeta.setForeground(Theme.MUTED);
        readerMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        meta.add(readerTitle);
        meta.add(Box.createVerticalStrut(4));
        meta.add(readerMeta);
        document.add(meta, BorderLayout.NORTH);

        readerBody = new JTextArea();
        readerBody.setEditable(false);
        readerBody.setLineWrap(true);
        readerBody.setWrapStyleWord(true);
        readerBody.setFont(Theme.font(Font.PLAIN, 15));
        readerBody.setBackground(Theme.SURFACE);
        readerBody.setForeground(Theme.TEXT);
        readerBody.setMargin(new Insets(10, 0, 10, 0));
        JScrollPane scroll = new JScrollPane(readerBody);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        document.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        editNoteButton = Theme.button("Edit note");
        editNoteButton.addActionListener(e -> showEditor());
        JButton create = Theme.primaryButton("Create note");
        create.addActionListener(e -> startNewNote());
        actions.add(editNoteButton);
        actions.add(create);
        document.add(actions, BorderLayout.SOUTH);
        readerPanel.add(document, "document");

        RoundedPanel empty = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS, Theme.BORDER);
        empty.setLayout(new GridBagLayout());
        JPanel emptyBox = new JPanel();
        emptyBox.setOpaque(false);
        emptyBox.setLayout(new BoxLayout(emptyBox, BoxLayout.Y_AXIS));
        emptyTitle = new JLabel("No notes yet");
        emptyTitle.setFont(Theme.font(Font.BOLD, 20));
        emptyTitle.setForeground(Theme.TEXT);
        emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyHint = new JLabel("Create your first note.");
        emptyHint.setFont(Theme.BODY);
        emptyHint.setForeground(Theme.MUTED);
        emptyHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton emptyCreate = Theme.primaryButton("Create note");
        emptyCreate.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyCreate.addActionListener(e -> startNewNote());
        emptyBox.add(emptyTitle);
        emptyBox.add(Box.createVerticalStrut(6));
        emptyBox.add(emptyHint);
        emptyBox.add(Box.createVerticalStrut(14));
        emptyBox.add(emptyCreate);
        empty.add(emptyBox);
        readerPanel.add(empty, "empty");
        return readerPanel;
    }

    private JPanel buildEditor() {
        JPanel editor = new JPanel(new BorderLayout(0, 8));
        editor.setOpaque(false);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 6));
        fields.setOpaque(false);
        fields.add(new JLabel("Title"));
        fields.add(new JLabel("Course"));
        fields.add(titleField);
        fields.add(courseBox);
        fields.add(new JLabel("Tags (comma separated)"));
        fields.add(new JLabel(""));
        fields.add(tagsField);

        topContainer.add(fields);
        topContainer.add(Box.createVerticalStrut(8));
        topContainer.add(buildNoteToolbar());

        processingCard = new ProcessingCard("Processing your document");
        processingCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        processingCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        topContainer.add(Box.createVerticalStrut(8));
        topContainer.add(processingCard);
        editor.add(topContainer, BorderLayout.NORTH);

        bodyArea.setFont(Theme.font(Font.PLAIN, 15));
        bodyArea.setBackground(Theme.SURFACE);
        bodyArea.setForeground(Theme.TEXT);
        bodyArea.setCaretColor(Theme.TEXT);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setMargin(new Insets(8, 8, 8, 8));
        editor.add(Theme.inputScrollPane(bodyArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtons.setOpaque(false);
        JButton back = Theme.button("Back to reading");
        back.addActionListener(e -> backToReading());
        leftButtons.add(back);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);
        JButton export = Theme.button("Export .txt");
        export.addActionListener(e -> export());
        JButton delete = Theme.button("Delete");
        delete.addActionListener(e -> delete());
        JButton save = Theme.primaryButton("Save note");
        save.addActionListener(e -> save());
        rightButtons.add(export);
        rightButtons.add(delete);
        rightButtons.add(save);

        bottom.add(leftButtons, BorderLayout.WEST);
        bottom.add(rightButtons, BorderLayout.EAST);
        editor.add(bottom, BorderLayout.SOUTH);
        return editor;
    }

    private JPanel buildNoteToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolbar.setOpaque(false);

        JButton importButton = Theme.button("Import Document (AI)");
        importButton.setToolTipText("The AI reads a document and drafts a note you can edit");
        importButton.addActionListener(e -> importFromDocument(importButton));
        toolbar.add(importButton);

        JButton ocrButton = Theme.button("Upload Image (OCR)");
        ocrButton.setToolTipText("Read text from an image. Needs a vision model such as qwen2.5vl:7b");
        ocrButton.addActionListener(e -> uploadAndExtractImageText(ocrButton));
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(ocrButton);

        JButton cleanButton = Theme.button("Clean formatting");
        cleanButton.setToolTipText("Remove markdown symbols from this note");
        cleanButton.addActionListener(e -> cleanFormatting());
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(cleanButton);
        return toolbar;
    }

    private void installDirtyTracking() {
        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent event) { markDirty(); }
            public void removeUpdate(DocumentEvent event) { markDirty(); }
            public void changedUpdate(DocumentEvent event) { markDirty(); }
        };
        titleField.getDocument().addDocumentListener(listener);
        tagsField.getDocument().addDocumentListener(listener);
        bodyArea.getDocument().addDocumentListener(listener);
        courseBox.addActionListener(e -> courseChanged());
    }

    private void courseChanged() {
        if (populating || loading) {
            return;
        }
        String selected = (String) courseBox.getSelectedItem();
        if (selected == null || selected.equals(lastCourseValue)) {
            return;
        }
        lastCourseValue = selected;
        dirty = true;
    }

    private void markDirty() {
        if (populating == false && loading == false) {
            dirty = true;
        }
    }

    private void searchChanged() {
        if (loading) {
            return;
        }
        refreshList();
    }

    private void filterChanged() {
        if (loading) {
            return;
        }
        String selected = (String) filterBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        if (confirmDiscard() == false) {
            loading = true;
            filterBox.setSelectedItem(appliedFilter);
            loading = false;
            return;
        }
        appliedFilter = selected;
        if (current != null) {
            open(current);
        } else {
            clearFields();
        }
        dirty = false;
        refreshList();
    }

    private void refreshList() {
        String query = noteSearch.getText().trim().toLowerCase();
        loading = true;
        list.clearSelection();
        listModel.clear();
        String code = scopeCode();
        for (Note note : manager.getNotesByCourse(code)) {
            if (query.isEmpty() || matches(note, query)) {
                listModel.addElement(note);
            }
        }
        if (readerMode && current != null && listModel.contains(current) == false) {
            current = null;
        }
        if (current != null && listModel.contains(current)) {
            list.setSelectedValue(current, true);
        } else if (readerMode && listModel.isEmpty() == false) {
            list.setSelectedIndex(0);
            if (listModel.isEmpty() == false) {
                open(listModel.get(0));
            }
        } else {
            list.clearSelection();
        }
        loading = false;
        listLabel.setText("Created notes (" + listModel.getSize() + ")");
        updateReader();
    }

    private String scopeCode() {
        if (lockedCourseCode != null && lockedCourseCode.isBlank() == false) {
            return lockedCourseCode;
        }
        String filter = (String) filterBox.getSelectedItem();
        if (filter == null || filter.equals(ALL)) {
            return null;
        }
        if (filter.equals(NONE)) {
            return "";
        }
        return filter;
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

    private void updateReader() {
        if (current == null) {
            readerCards.show(readerPanel, "empty");
            boolean searching = noteSearch.getText().trim().isEmpty() == false;
            if (listModel.isEmpty()) {
                emptyTitle.setText(searching ? "No notes match your search" : "No notes yet");
                emptyHint.setText(searching ? "Try a different search." : "Create your first note.");
            } else {
                emptyTitle.setText("Select a note");
                emptyHint.setText("Pick a note from the list to read it.");
            }
            return;
        }
        readerCards.show(readerPanel, "document");
        readerTitle.setText(current.getTitle());
        StringBuilder meta = new StringBuilder();
        if (current.getCourseCode().isEmpty() == false) {
            meta.append(current.getCourseCode()).append("   ");
        }
        meta.append("Edited ").append(current.getEdited().format(NOTE_DATE));
        if (current.getTags().isEmpty() == false) {
            meta.append("   ").append(String.join(", ", current.getTags()));
        }
        readerMeta.setText(meta.toString());
        readerBody.setText(PlainText.clean(current.getBody()));
        readerBody.setCaretPosition(0);
    }

    private void showReader() {
        readerMode = true;
        centerModes.show(centerPanel, "reader");
        updateReader();
    }

    private void showEditor() {
        readerMode = false;
        centerModes.show(centerPanel, "editor");
    }

    private void open(Note note) {
        current = note;
        populate(note);
        updateReader();
    }

    private void populate(Note note) {
        populating = true;
        titleField.setText(note.getTitle());
        tagsField.setText(String.join(", ", note.getTags()));
        bodyArea.setText(note.getBody());
        bodyArea.setCaretPosition(0);
        if (lockedCourseCode != null) {
            courseBox.setSelectedItem(lockedCourseCode);
        } else {
            courseBox.setSelectedItem(note.getCourseCode().isEmpty() ? NONE : note.getCourseCode());
        }
        lastCourseValue = (String) courseBox.getSelectedItem();
        populating = false;
        dirty = false;
    }

    private void clearFields() {
        populating = true;
        titleField.setText("");
        tagsField.setText("");
        bodyArea.setText("");
        if (lockedCourseCode != null) {
            courseBox.setSelectedItem(lockedCourseCode);
        } else {
            courseBox.setSelectedItem(NONE);
        }
        lastCourseValue = (String) courseBox.getSelectedItem();
        populating = false;
    }

    private void restoreSelection() {
        loading = true;
        if (current != null && listModel.contains(current)) {
            list.setSelectedValue(current, true);
        } else {
            list.clearSelection();
        }
        loading = false;
    }

    private void backToReading() {
        if (confirmDiscard() == false) {
            return;
        }
        if (current != null) {
            open(current);
        } else {
            clearFields();
        }
        dirty = false;
        showReader();
    }

    private void startNewNote() {
        if (confirmDiscard() == false) {
            return;
        }
        beginNewNote();
    }

    private void beginNewNote() {
        current = null;
        loading = true;
        list.clearSelection();
        loading = false;
        clearFields();
        populating = true;
        if (lockedCourseCode != null) {
            courseBox.setSelectedItem(lockedCourseCode);
        } else {
            String filter = (String) filterBox.getSelectedItem();
            courseBox.setSelectedItem(filter == null || filter.equals(ALL) ? NONE : filter);
        }
        lastCourseValue = (String) courseBox.getSelectedItem();
        populating = false;
        dirty = false;
        showEditor();
        updateReader();
        titleField.requestFocusInWindow();
    }

    public void startNewNoteFor(String courseCode) {
        if (confirmDiscard() == false) {
            return;
        }
        if (embedded == false && lockedCourseCode == null) {
            loading = true;
            filterBox.setSelectedItem(ALL);
            loading = false;
            appliedFilter = ALL;
            refreshList();
        }
        beginNewNote();
        if (courseCode != null && courseCode.isBlank() == false) {
            populating = true;
            courseBox.setSelectedItem(courseCode);
            lastCourseValue = (String) courseBox.getSelectedItem();
            populating = false;
        }
    }

    public void showNote(Note note) {
        if (confirmDiscard() == false) {
            return;
        }
        reload();
        loading = true;
        if (embedded == false) {
            filterBox.setSelectedItem(ALL);
            appliedFilter = ALL;
        }
        noteSearch.setText("");
        loading = false;
        refreshList();
        current = note;
        loading = true;
        list.setSelectedValue(note, true);
        loading = false;
        open(note);
        showReader();
    }

    public void setCourseScope(Course course) {
        String code = course == null ? null : course.getCode();
        boolean changed = lockedCourseCode == null ? code != null : lockedCourseCode.equals(code) == false;
        lockedCourseCode = code;
        if (changed) {
            current = null;
            dirty = false;
            readerMode = true;
            clearFields();
        }
        reload();
        if (changed) {
            showReader();
        }
    }

    public void setEmbedded(boolean value) {
        embedded = value;
        headerPanel.setVisible(value == false);
        filterBox.setVisible(value == false);
        newNoteButton.setText(value ? "Create note" : "+ New note");
        noteSearch.setToolTipText(value ? "Search this course's notes" : "Search all notes");
        setBorder(value
                ? BorderFactory.createEmptyBorder()
                : BorderFactory.createEmptyBorder(0, 24, 24, 24));
    }

    public void refresh() {
        reload();
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    private void notifyChange() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public boolean canLeave() {
        if (readerMode || dirty == false) {
            return true;
        }
        if (confirmDiscard() == false) {
            return false;
        }
        discardChanges();
        return true;
    }

    private boolean confirmDiscard() {
        if (dirty == false) {
            return true;
        }
        Object[] options = { "Keep editing", "Discard changes" };
        String message = current == null
                ? "You have unsaved changes in this new note."
                : "You have unsaved changes in \"" + current.getTitle() + "\".";
        int choice = JOptionPane.showOptionDialog(this, message, "Unsaved changes",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                options, options[0]);
        return choice == 1;
    }

    private void discardChanges() {
        if (current != null) {
            open(current);
        } else {
            clearFields();
        }
        dirty = false;
        showReader();
    }

    public void reload() {
        if (current != null && manager.getAllNotes().contains(current) == false) {
            current = null;
            clearFields();
        }
        loading = true;
        Object previousFilter = filterBox.getSelectedItem();
        Object previousCourse = courseBox.getSelectedItem();

        DefaultComboBoxModel<String> filterModel = new DefaultComboBoxModel<>();
        boolean scoped = lockedCourseCode != null && lockedCourseCode.isBlank() == false;
        if (scoped) {
            filterModel.addElement(lockedCourseCode);
        } else {
            filterModel.addElement(ALL);
            filterModel.addElement(NONE);
            for (Course course : manager.getCourses()) {
                filterModel.addElement(course.getCode());
            }
        }
        filterBox.setModel(filterModel);
        if (scoped) {
            filterBox.setSelectedItem(lockedCourseCode);
        } else {
            if (previousFilter != null) {
                filterBox.setSelectedItem(previousFilter);
            }
            if (filterBox.getSelectedIndex() < 0) {
                filterBox.setSelectedIndex(0);
            }
        }

        DefaultComboBoxModel<String> courseModel = new DefaultComboBoxModel<>();
        if (scoped) {
            courseModel.addElement(lockedCourseCode);
        } else {
            courseModel.addElement(NONE);
            for (Course course : manager.getCourses()) {
                courseModel.addElement(course.getCode());
            }
        }
        courseBox.setModel(courseModel);
        if (scoped) {
            courseBox.setSelectedItem(lockedCourseCode);
        } else {
            if (previousCourse != null) {
                courseBox.setSelectedItem(previousCourse);
            }
            if (courseBox.getSelectedIndex() < 0) {
                courseBox.setSelectedIndex(0);
            }
        }

        appliedFilter = (String) filterBox.getSelectedItem();
        if (appliedFilter == null) {
            appliedFilter = ALL;
        }
        lastCourseValue = (String) courseBox.getSelectedItem();
        loading = false;
        refreshList();
    }

    private String selectedCourseCode() {
        if (lockedCourseCode != null) {
            return lockedCourseCode;
        }
        String selected = (String) courseBox.getSelectedItem();
        return selected == null || selected.equals(NONE) ? "" : selected;
    }

    private List<String> parseTags() {
        List<String> tags = new ArrayList<>();
        for (String tag : tagsField.getText().split(",")) {
            if (tag.isBlank() == false) {
                tags.add(tag.trim());
            }
        }
        return tags;
    }

    private void save() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please give the note a title.",
                    "Check your input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (current == null) {
                current = manager.addNote(title, bodyArea.getText(), selectedCourseCode(), parseTags());
                AppData.activity().recordNote();
            } else {
                manager.updateNote(current, title, bodyArea.getText(), selectedCourseCode(), parseTags());
            }
            dirty = false;
            refreshList();
            loading = true;
            list.setSelectedValue(current, true);
            loading = false;
            showReader();
            notifyChange();
        } catch (IOException exception) {
            error(exception);
        }
    }

    private void delete() {
        if (current == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete \"" + current + "\"?",
                "Delete note", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            manager.deleteNote(current);
            current = null;
            dirty = false;
            readerMode = true;
            refreshList();
            showReader();
            notifyChange();
        } catch (IOException exception) {
            error(exception);
        }
    }

    private void cleanFormatting() {
        String cleaned = PlainText.clean(bodyArea.getText());
        if (cleaned.equals(bodyArea.getText())) {
            JOptionPane.showMessageDialog(this, "This note is already plain text.",
                    "Clean formatting", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        bodyArea.setText(cleaned);
        bodyArea.setCaretPosition(0);
        dirty = true;
    }

    private void importFromDocument(JButton source) {
        String document = ImportDocumentDialog.show(this);
        if (document == null) {
            return;
        }
        source.setEnabled(false);
        processingCard.start("Reading your document...");

        new SwingWorker<String[], String>() {
            @Override
            protected String[] doInBackground() throws Exception {
                AiConfig config = AiConfig.load();
                AiClient client = new AiClient(config);
                if (config.isDemoMode() == false && client.isAvailable() == false) {
                    if (config.isLocal()) {
                        throw new IOException("The local AI is not running. Start Ollama first with \"ollama run "
                                + config.getModel() + "\", or pick another provider in Settings.");
                    }
                    throw new IOException("Could not reach " + config.getBaseUrl()
                            + ". Check the internet connection and the key in Settings.");
                }
                StudyGenerator generator = new StudyGenerator(client, config);
                return generator.suggestNote(document, message -> publish(message));
            }

            @Override
            protected void process(List<String> updates) {
                processingCard.update(updates.get(updates.size() - 1));
            }

            @Override
            protected void done() {
                source.setEnabled(true);
                processingCard.stop();
                try {
                    String[] result = get();
                    current = null;
                    loading = true;
                    list.clearSelection();
                    loading = false;
                    populating = true;
                    String title = PlainText.clean(result[0]).replace('\n', ' ').trim();
                    if (title.isEmpty()) {
                        title = "Imported note";
                    }
                    titleField.setText(title);
                    bodyArea.setText(PlainText.clean(result[1]));
                    bodyArea.setCaretPosition(0);
                    if (lockedCourseCode != null) {
                        courseBox.setSelectedItem(lockedCourseCode);
                    } else {
                        String filter = (String) filterBox.getSelectedItem();
                        courseBox.setSelectedItem(filter == null || filter.equals(ALL) ? NONE : filter);
                    }
                    lastCourseValue = (String) courseBox.getSelectedItem();
                    populating = false;
                    dirty = true;
                    showEditor();
                    updateReader();
                    JOptionPane.showMessageDialog(NotesPage.this,
                            "The AI drafted a note from your document. Check it, then press Save note.");
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
                    JOptionPane.showMessageDialog(NotesPage.this, cause.getMessage(),
                            "The AI could not finish", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void uploadAndExtractImageText(JButton source) {
        AiConfig config = AiConfig.load();
        boolean gemini = AiConfig.GEMINI.equals(config.getProvider());

        if (config.isDemoMode()) {
            JOptionPane.showMessageDialog(this,
                    "Demo mode is on, so image reading is off.\n"
                    + "Turn demo mode off and use a vision model, or switch to Gemini.",
                    "AI OCR", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (gemini) {
            if (ocrKey().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Image text extraction needs a Gemini API key.\n"
                        + "Open Settings, then AI Assistant, choose Gemini and paste your key.",
                        "AI OCR", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else if (config.supportsVision() == false) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "The current model (" + config.getModel() + ") cannot read images.\n\n"
                    + config.visionHint() + "\n\n"
                    + "Do you still want to try?",
                    "Vision model needed", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        NativeFileDialog fileDialog = new NativeFileDialog();
        File selectedFile = fileDialog.open("Select an image for AI text extraction",
                "Images", new String[] { "png", "jpg", "jpeg", "webp", "bmp" });
        if (selectedFile == null) {
            return;
        }

        source.setEnabled(false);
        JDialog loadingDialog = new JDialog(parentFrame, "Processing", false);
        JLabel message = new JLabel("  Extracting text with AI...", JLabel.CENTER);
        loadingDialog.add(message);
        loadingDialog.setSize(300, 90);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setVisible(true);

        new Thread(() -> {
            try {
                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                String mimeType = mimeTypeFor(selectedFile);
                String extractedText;
                if (gemini) {
                    AIFlashcardService service = new AIFlashcardService(ocrKey(), config.getModel());
                    extractedText = service.extractTextFromImage(fileBytes, mimeType);
                } else {
                    AiClient client = new AiClient(config);
                    if (client.isAvailable() == false) {
                        if (config.isLocal()) {
                            throw new IOException("The local AI is not running. Start Ollama first with \"ollama run "
                                    + config.getModel() + "\" and try again.");
                        }
                        throw new IOException("Could not reach " + config.getBaseUrl()
                                + ". Check the internet connection and the key in Settings.");
                    }
                    extractedText = client.completeWithImage(OCR_SYSTEM, OCR_PROMPT, fileBytes, mimeType);
                }
                final String text = extractedText;
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    source.setEnabled(true);
                    if (text.isEmpty() == false) {
                        showEditor();
                        insertExtractedText(PlainText.clean(text));
                        JOptionPane.showMessageDialog(this, "Text extracted successfully!",
                                "AI OCR Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No text was found in that image.",
                                "AI OCR", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            } catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    source.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Extraction failed: " + exception.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private String ocrKey() {
        AiConfig config = AiConfig.load();
        if (AiConfig.GEMINI.equals(config.getProvider())) {
            String key = config.getApiKey();
            if (key != null && key.isBlank() == false) {
                return key;
            }
        }
        return AppData.settings().get(SettingsManager.API_KEY, "");
    }

    private String mimeTypeFor(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        if (name.endsWith(".bmp")) {
            return "image/bmp";
        }
        return "image/jpeg";
    }

    private void insertExtractedText(String text) {
        if (bodyArea.getText().isBlank()) {
            bodyArea.setText(text);
        } else {
            bodyArea.append("\n\n" + text);
        }
        bodyArea.setCaretPosition(0);
        dirty = true;
    }

    private void export() {
        if (current == null) {
            JOptionPane.showMessageDialog(this, "Save the note first, then export it.");
            return;
        }
        NativeFileDialog fileDialog = new NativeFileDialog();
        File target = fileDialog.save("Export note", "Text file", new String[] { "txt" },
                current.getTitle().replaceAll("[^a-zA-Z0-9-_ ]", "") + ".txt");
        if (target != null) {
            try {
                manager.exportNote(current, target.toPath());
            } catch (IOException exception) {
                error(exception);
            }
        }
    }

    private void error(IOException exception) {
        JOptionPane.showMessageDialog(this, "Could not save: " + exception.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private class NoteRowRenderer extends JPanel implements ListCellRenderer<Note> {
        private final JLabel title = new JLabel();
        private final JLabel meta = new JLabel();

        NoteRowRenderer() {
            setLayout(new BorderLayout(0, 2));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(7, 10, 7, 10)));
            title.setFont(Theme.font(Font.BOLD, 13));
            meta.setFont(Theme.font(Font.PLAIN, 11));
            add(title, BorderLayout.NORTH);
            add(meta, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Note> source, Note note,
                int index, boolean selected, boolean focus) {
            title.setText(note.getTitle());
            String date = note.getEdited().format(NOTE_DATE);
            if (embedded) {
                meta.setText(date);
            } else if (note.getCourseCode().isEmpty()) {
                meta.setText(date);
            } else {
                meta.setText(note.getCourseCode() + "  |  " + date);
            }
            setOpaque(true);
            setBackground(selected ? Theme.SELECT : Theme.SURFACE);
            title.setForeground(selected ? Color.WHITE : Theme.TEXT);
            meta.setForeground(selected ? new Color(0xE6E2FF) : Theme.MUTED);
            return this;
        }
    }
}
