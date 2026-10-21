package ui.pages;

import data.AIFlashcardService;
import data.AppData;
import data.NoteManager;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import model.Course;
import model.Note;
import ui.Theme;
import ui.components.NativeFileDialog;
import ui.components.RoundedComboBox;
import ui.components.RoundedTextArea;
import ui.components.RoundedTextField;

/** Screen 4: Notes page. Create, edit, move, tag, delete, export notes, and AI image OCR upload. */
public class NotesPage extends JPanel {
    private static final String ALL = "All courses";
    private static final String NONE = "No course";

    private final NoteManager manager;
    private final JComboBox<String> filterBox = new RoundedComboBox<>();
    private final DefaultListModel<Note> listModel = new DefaultListModel<>();
    private final JList<Note> list = new JList<>(listModel);
    private final JTextField titleField = new RoundedTextField();
    private final JComboBox<String> courseBox = new RoundedComboBox<>();
    private final JTextField tagsField = new RoundedTextField();
    private final JTextArea bodyArea = new RoundedTextArea();
    private final AIFlashcardService aiService = new AIFlashcardService("GEMINI_API_KEY");
    private Note current; // null = writing a new note
    private boolean loading;

    public NotesPage() {
        this.manager = AppData.get();
        setBackground(Theme.BG);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        JLabel header = new JLabel("Notes");
        header.setFont(Theme.TITLE);
        add(header, BorderLayout.NORTH);

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(260, 0));
        filterBox.addActionListener(e -> { if (!loading) refreshList(); });
        JButton newNote = Theme.primaryButton("+ New note");
        newNote.addActionListener(e -> startNewNote());
        left.add(filterBox, BorderLayout.NORTH);
        list.setFont(Theme.BODY);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !loading && list.getSelectedValue() != null) open(list.getSelectedValue());
        });
        left.add(Theme.roundedScrollPane(list), BorderLayout.CENTER);
        left.add(newNote, BorderLayout.SOUTH);
        add(left, BorderLayout.WEST);

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

        editor.add(topContainer, BorderLayout.NORTH);

        bodyArea.setFont(Theme.font(Font.PLAIN, 15));
        bodyArea.setBackground(Theme.SURFACE);
        bodyArea.setForeground(Theme.TEXT);
        bodyArea.setCaretColor(Theme.TEXT);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setMargin(new Insets(8, 8, 8, 8));
        editor.add(Theme.inputScrollPane(bodyArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton export = Theme.button("Export .txt");
        export.addActionListener(e -> export());
        JButton delete = Theme.button("Delete");
        delete.addActionListener(e -> delete());
        JButton save = Theme.primaryButton("Save note");
        save.addActionListener(e -> save());
        buttons.add(export);
        buttons.add(delete);
        buttons.add(save);
        editor.add(buttons, BorderLayout.SOUTH);
        add(editor, BorderLayout.CENTER);

        EventQueue.invokeLater(this::reload);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { reload(); }
        });
    }

    private JPanel buildNoteToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolbar.setOpaque(false);

        JPopupMenu uploadMenu = new JPopupMenu();
        uploadMenu.setBackground(Theme.SURFACE);
        uploadMenu.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JMenuItem uploadFileItem = new JMenuItem("📁 Upload files");
        uploadFileItem.setFont(Theme.BODY);
        uploadFileItem.setBackground(Theme.SURFACE);
        uploadFileItem.setForeground(Theme.TEXT);
        uploadFileItem.addActionListener(e -> uploadAndExtractImageText());
        uploadMenu.add(uploadFileItem);

        JButton uploadOcrBtn = Theme.primaryButton("📷 Upload & Extract Text (AI)");
        uploadOcrBtn.setToolTipText("Upload image or document for Gemini AI text extraction");
        
        uploadOcrBtn.addActionListener(e -> {
            uploadMenu.show(uploadOcrBtn, 0, uploadOcrBtn.getHeight());
        });

        toolbar.add(uploadOcrBtn);
        return toolbar;
    }

    private void uploadAndExtractImageText() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        NativeFileDialog fileDialog = new NativeFileDialog();
        File selectedFile = fileDialog.open("Select Image or Document for AI Text Extraction",
                "Images and documents",
                new String[] { "png", "jpg", "jpeg", "webp", "bmp", "pdf", "docx", "txt" });

        if (selectedFile != null) {
            
            JDialog loadingDialog = new JDialog(parentFrame, "Processing", true);
            JLabel msg = new JLabel("  Extracting text with Gemini AI...", JLabel.CENTER);
            loadingDialog.add(msg);
            loadingDialog.setSize(260, 90);
            loadingDialog.setLocationRelativeTo(this);

            new Thread(() -> {
                try {
                    byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                    String mimeType = "image/jpeg";
                    String nameLower = selectedFile.getName().toLowerCase();
                    if (nameLower.endsWith(".png")) {
                        mimeType = "image/png";
                    } else if (nameLower.endsWith(".pdf")) {
                        mimeType = "application/pdf";
                    }

                    String extractedText = aiService.extractTextFromImage(fileBytes, mimeType);
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        if (!extractedText.isEmpty()) {
                            if (bodyArea.getText().isBlank()) {
                                bodyArea.setText(extractedText);
                            } else {
                                bodyArea.append("\n\n" + extractedText);
                            }
                            JOptionPane.showMessageDialog(this, "Text extracted successfully!", "AI OCR Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        JOptionPane.showMessageDialog(this, "Extraction failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();

            loadingDialog.setVisible(true);
        }
    }

    public void reload() {
        loading = true;
        Object prevFilter = filterBox.getSelectedItem();
        Object prevCourse = courseBox.getSelectedItem();
        filterBox.removeAllItems();
        courseBox.removeAllItems();
        filterBox.addItem(ALL);
        filterBox.addItem(NONE);
        courseBox.addItem(NONE);
        for (Course c : manager.getCourses()) {
            filterBox.addItem(c.getCode());
            courseBox.addItem(c.getCode());
        }
        if (prevFilter != null) filterBox.setSelectedItem(prevFilter);
        if (prevCourse != null) courseBox.setSelectedItem(prevCourse);
        if (filterBox.getSelectedItem() == null) filterBox.setSelectedIndex(0);
        loading = false;
        refreshList();
    }

    // opens one note in the editor, used when a search result is clicked
    public void showNote(Note n) {
        reload();
        loading = true;
        filterBox.setSelectedItem(ALL);   // make sure the note is in the list
        loading = false;
        refreshList();
        current = n;
        list.setSelectedValue(n, true);
        open(n);
    }

    private void refreshList() {
        loading = true;
        listModel.clear();
        String f = (String) filterBox.getSelectedItem();
        String code = f == null || f.equals(ALL) ? null : f.equals(NONE) ? "" : f;
        for (Note n : manager.getNotesByCourse(code)) listModel.addElement(n);
        if (current != null && listModel.contains(current)) list.setSelectedValue(current, true);
        loading = false;
    }

    private void startNewNote() {
        current = null;
        list.clearSelection();
        titleField.setText("");
        tagsField.setText("");
        bodyArea.setText("");
        String f = (String) filterBox.getSelectedItem();
        courseBox.setSelectedItem(f == null || f.equals(ALL) ? NONE : f);
        titleField.requestFocus();
    }

    private void open(Note n) {
        current = n;
        titleField.setText(n.getTitle());
        tagsField.setText(String.join(", ", n.getTags()));
        bodyArea.setText(n.getBody());
        courseBox.setSelectedItem(n.getCourseCode().isEmpty() ? NONE : n.getCourseCode());
        bodyArea.setCaretPosition(0);
    }

    private String selectedCourseCode() {
        String s = (String) courseBox.getSelectedItem();
        return s == null || s.equals(NONE) ? "" : s;
    }

    private List<String> parseTags() {
        List<String> tags = new ArrayList<>();
        for (String t : tagsField.getText().split(",")) if (!t.isBlank()) tags.add(t.trim());
        return tags;
    }

    private void save() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please give the note a title.", "Check your input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (current == null) current = manager.addNote(title, bodyArea.getText(), selectedCourseCode(), parseTags());
            else manager.updateNote(current, title, bodyArea.getText(), selectedCourseCode(), parseTags());
            refreshList();
        } catch (IOException ex) {
            error(ex);
        }
    }

    private void delete() {
        if (current == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Delete \"" + current + "\"?", "Delete note", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            manager.deleteNote(current);
            startNewNote();
            refreshList();
        } catch (IOException ex) {
            error(ex);
        }
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
            } catch (IOException ex) {
                error(ex);
            }
        }
    }

    private void error(IOException ex) {
        JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}