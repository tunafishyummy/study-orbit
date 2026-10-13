package ui.pages;

import data.AppData;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import model.Flashcard;
import model.Note;
import ui.Theme;
import ui.components.RoundedPanel;

public class CreateFlashcardPage extends JPanel {
    private static final String NEW_DECK_OPTION = "+ Create New Deck";

    private JComboBox<Note> noteSelector;
    private JTextArea noteBodyArea;
    private JComboBox<String> deckSelector;
    private JTextField newDeckField;
    private JTextArea frontArea;
    private JTextArea backArea;
    private JTextField tagsField;


    public CreateFlashcardPage(Runnable onBack) {


        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = Theme.header("Create Flashcards");
        headerPanel.add(title, BorderLayout.WEST);

        JButton backToReviewBtn = Theme.button("Back to Review");
        backToReviewBtn.addActionListener(e -> onBack.run());
        headerPanel.add(backToReviewBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Split Content: Left = Note Viewer, Right = Manual Form + Suggested Header
        JPanel splitContent = new JPanel(new GridLayout(1, 2, 24, 0));
        splitContent.setOpaque(false);
        splitContent.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Left Panel: Note Reference Viewer
        RoundedPanel leftPanel = new RoundedPanel(Theme.SURFACE, 16);
        leftPanel.setLayout(new BorderLayout(0, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel noteTopBar = new JPanel(new BorderLayout(10, 0));
        noteTopBar.setOpaque(false);
        JLabel noteLabel = new JLabel("Reference Note:");
        noteLabel.setFont(Theme.HEADING);
        noteLabel.setForeground(Theme.TEXT);
        noteTopBar.add(noteLabel, BorderLayout.WEST);

        List<Note> notes = AppData.get().getAllNotes();
        noteSelector = new JComboBox<>(notes.toArray(new Note[0]));
        noteSelector.setFont(Theme.BODY);
        noteSelector.addActionListener(e -> updateNoteContent());
        noteTopBar.add(noteSelector, BorderLayout.CENTER);
        leftPanel.add(noteTopBar, BorderLayout.NORTH);

        noteBodyArea = new JTextArea();
        noteBodyArea.setFont(Theme.BODY);
        noteBodyArea.setForeground(Theme.TEXT);
        noteBodyArea.setLineWrap(true);
        noteBodyArea.setWrapStyleWord(true);
        noteBodyArea.setEditable(false);
        noteBodyArea.setOpaque(false);

        JScrollPane noteScroll = new JScrollPane(noteBodyArea);
        noteScroll.setBorder(null);
        noteScroll.setOpaque(false);
        noteScroll.getViewport().setOpaque(false);
        leftPanel.add(noteScroll, BorderLayout.CENTER);
        splitContent.add(leftPanel);

        // Right Panel: Form with Existing/New Deck Dropdown
        RoundedPanel rightPanel = new RoundedPanel(Theme.SURFACE, 16);
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel rightContentContainer = new JPanel();
        rightContentContainer.setLayout(new BoxLayout(rightContentContainer, BoxLayout.Y_AXIS));
        rightContentContainer.setOpaque(false);

        JLabel formTitle = new JLabel("New Flashcard Details");
        formTitle.setFont(Theme.HEADING);
        formTitle.setForeground(Theme.TEXT);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(formTitle);
        rightContentContainer.add(Box.createVerticalStrut(12));

        // Deck Selection Controls
        rightContentContainer.add(label("Select or Create Deck"));
        deckSelector = new JComboBox<>();
        deckSelector.setFont(Theme.BODY);
        deckSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        deckSelector.addActionListener(e -> toggleNewDeckField());
        rightContentContainer.add(deckSelector);
        rightContentContainer.add(Box.createVerticalStrut(8));

        newDeckField = new JTextField();
        newDeckField.setFont(Theme.BODY);
        newDeckField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        newDeckField.setVisible(false); // Hidden unless "+ Create New Deck" is selected
        rightContentContainer.add(newDeckField);
        rightContentContainer.add(Box.createVerticalStrut(10));

        rightContentContainer.add(label("Front (Question / Prompt)"));
        frontArea = new JTextArea(3, 20);
        frontArea.setFont(Theme.BODY);
        frontArea.setLineWrap(true);
        frontArea.setWrapStyleWord(true);
        JScrollPane frontScroll = new JScrollPane(frontArea);
        frontScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        rightContentContainer.add(frontScroll);
        rightContentContainer.add(Box.createVerticalStrut(10));

        rightContentContainer.add(label("Back (Answer / Definition)"));
        backArea = new JTextArea(3, 20);
        backArea.setFont(Theme.BODY);
        backArea.setLineWrap(true);
        backArea.setWrapStyleWord(true);
        JScrollPane backScroll = new JScrollPane(backArea);
        backScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        rightContentContainer.add(backScroll);
        rightContentContainer.add(Box.createVerticalStrut(10));

        rightContentContainer.add(label("Tags (comma separated)"));
        tagsField = new JTextField();
        tagsField.setFont(Theme.BODY);
        tagsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        rightContentContainer.add(tagsField);
        rightContentContainer.add(Box.createVerticalStrut(14));

        JButton saveCardBtn = Theme.primaryButton("Save Flashcard");
        saveCardBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveCardBtn.addActionListener(e -> saveFlashcard());
        rightContentContainer.add(saveCardBtn);

        // Divider
        rightContentContainer.add(Box.createVerticalStrut(20));
        JPanel line = new JPanel();
        line.setBackground(Theme.BORDER);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        rightContentContainer.add(line);
        rightContentContainer.add(Box.createVerticalStrut(16));

        // Suggested Flashcards Placeholder Section
        JLabel suggestedTitle = new JLabel("Suggested Flashcards");
        suggestedTitle.setFont(Theme.HEADING);
        suggestedTitle.setForeground(Theme.TEXT);
        suggestedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(suggestedTitle);
        rightContentContainer.add(Box.createVerticalStrut(6));

        JLabel suggestedSubtitle = Theme.muted("Auto-generated cards from notes coming soon...");
        suggestedSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(suggestedSubtitle);

        JScrollPane rightScroll = new JScrollPane(rightContentContainer);
        rightScroll.setBorder(null);
        rightScroll.setOpaque(false);
        rightScroll.getViewport().setOpaque(false);
        rightPanel.add(rightScroll, BorderLayout.CENTER);

        splitContent.add(rightPanel);
        add(splitContent, BorderLayout.CENTER);

        refreshDecksDropdown();

        if (!notes.isEmpty()) {
            noteSelector.setSelectedIndex(0);
            updateNoteContent();
        } else {
            noteBodyArea.setText("No notes available. Create a note first in the Notes tab!");
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public void refreshDecksDropdown() {
        deckSelector.removeAllItems();
        Set<String> existingDecks = new LinkedHashSet<>();
        
        for (Flashcard f : AppData.get().getFlashcards()) {
            if (f.getDeckTitle() != null && !f.getDeckTitle().isBlank()) {
                existingDecks.add(f.getDeckTitle());
            }
        }

        for (String deck : existingDecks) {
            deckSelector.addItem(deck);
        }
        deckSelector.addItem(NEW_DECK_OPTION);

        if (existingDecks.isEmpty()) {
            deckSelector.setSelectedItem(NEW_DECK_OPTION);
        } else {
            deckSelector.setSelectedIndex(0);
        }
        toggleNewDeckField();
    }

    private void toggleNewDeckField() {
        String selected = (String) deckSelector.getSelectedItem();
        boolean isNew = NEW_DECK_OPTION.equals(selected);
        newDeckField.setVisible(isNew);
        revalidate();
        repaint();
    }

    public void refreshNotes() {
        List<Note> notes = AppData.get().getAllNotes();
        noteSelector.removeAllItems();
        for (Note n : notes) {
            noteSelector.addItem(n);
        }
        
        refreshDecksDropdown();

        if (!notes.isEmpty()) {
            noteSelector.setSelectedIndex(0);
            updateNoteContent();
        } else {
            noteBodyArea.setText("No notes available. Create a note first in the Notes tab!");
        }
    }

    private void updateNoteContent() {
        Note selected = (Note) noteSelector.getSelectedItem();
        if (selected != null) {
            noteBodyArea.setText(selected.getBody());
        }
    }

    private void saveFlashcard() {
        String selectedOption = (String) deckSelector.getSelectedItem();
        String deck;

        if (NEW_DECK_OPTION.equals(selectedOption)) {
            deck = newDeckField.getText().trim();
        } else {
            deck = selectedOption != null ? selectedOption.trim() : "";
        }

        String front = frontArea.getText().trim();
        String back = backArea.getText().trim();
        String tagText = tagsField.getText().trim();

        if (deck.isEmpty() || front.isEmpty() || back.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Deck title, front, and back cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<String> tags = new java.util.ArrayList<>();
            if (!tagText.isBlank()) {
                for (String t : tagText.split(",")) tags.add(t.trim());
            }
            JOptionPane.showMessageDialog(this, "Successfully added");
            frontArea.setText("");
            backArea.setText("");
            tagsField.setText("");
            newDeckField.setText("");
            refreshDecksDropdown();
            deckSelector.setSelectedItem(deck);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}