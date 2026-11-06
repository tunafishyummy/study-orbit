package ui.pages;

import ai.AiClient;
import ai.AiConfig;
import ai.StudyGenerator;
import data.AppData;
import data.FlashcardManager;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import model.Flashcard;
import model.Note;
import ui.ImportDocumentDialog;
import ui.Theme;
import ui.components.ProcessingCard;
import ui.components.RoundedComboBox;
import ui.components.RoundedPanel;
import ui.components.RoundedTextArea;
import ui.components.RoundedTextField;

public class CreateFlashcardPage extends JPanel {
    private static final String NEW_DECK_OPTION = "+ Create New Deck";
    private static final Color PILL_LABEL = new Color(0xC62F37);
    private static final Color PILL_NOTE = new Color(0xE26B6B);

    private JComboBox<Note> noteSelector;
    private JTextArea noteBodyArea;
    private JScrollPane noteScroll;
    private JPanel notePreviewContainer;
    private final List<Note> selectedNotes = new ArrayList<>();
    private final JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final PlusButton plusButton = new PlusButton();
    private JComboBox<String> deckSelector;
    private JTextField newDeckField;
    private JTextArea frontArea;
    private JTextArea backArea;
    private JTextField tagsField;
    private ScrollablePanel suggestedContainer;
    private FlashcardManager flashcardManager;
    private JLabel aiStatusLabel;
    private JButton generateButton;
    private JButton importButton;
    private JButton leftImportButton;
    private ProcessingCard processingCard;
    private final List<Flashcard> activeSuggestions = new ArrayList<Flashcard>();
    private final Runnable onBackCallback;

    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public CreateFlashcardPage(Runnable onBack) {
        this.onBackCallback = onBack;
        flashcardManager = new FlashcardManager(AppData.get().getStoreReference());

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = Theme.header("Create Flashcards");
        headerPanel.add(title, BorderLayout.WEST);

        JButton backToReviewBtn = Theme.button("Back to Review");
        backToReviewBtn.addActionListener(e -> {
            clearSuggestions();
            onBackCallback.run();
        });
        headerPanel.add(backToReviewBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel splitContent = new JPanel(new GridLayout(1, 2, 24, 0));
        splitContent.setOpaque(false);
        splitContent.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel noteTopBar = new JPanel();
        noteTopBar.setOpaque(false);
        noteTopBar.setLayout(new BoxLayout(noteTopBar, BoxLayout.Y_AXIS));
        chipRow.setOpaque(false);
        chipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noteHint = Theme.muted("Select notes to include in flashcard generation");
        noteHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftImportButton = Theme.button("Import Document");
        leftImportButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftImportButton.addActionListener(e -> generateFromDocument());
        noteTopBar.add(chipRow);
        noteTopBar.add(Box.createVerticalStrut(6));
        noteTopBar.add(noteHint);
        noteTopBar.add(Box.createVerticalStrut(6));
        noteTopBar.add(leftImportButton);

        List<Note> notes = AppData.get().getAllNotes();
        noteSelector = new RoundedComboBox<>(notes.toArray(new Note[0]));
        noteSelector.setFont(Theme.BODY);

        noteBodyArea = new RoundedTextArea();
        noteBodyArea.setFont(Theme.BODY);
        noteBodyArea.setForeground(Theme.TEXT);
        noteBodyArea.setLineWrap(true);
        noteBodyArea.setWrapStyleWord(true);
        noteBodyArea.setEditable(false);
        noteBodyArea.setOpaque(false);

        noteScroll = Theme.inputScrollPane(noteBodyArea);
        notePreviewContainer = new JPanel(new BorderLayout());
        notePreviewContainer.setOpaque(false);
        notePreviewContainer.add(noteScroll, BorderLayout.CENTER);
        plusButton.addActionListener(e -> showNotePicker());

        JPanel notePreviewCard = Theme.card();
        notePreviewCard.setLayout(new BorderLayout());
        notePreviewCard.add(notePreviewContainer, BorderLayout.CENTER);

        JPanel leftColumn = new JPanel(new BorderLayout(0, 14));
        leftColumn.setOpaque(false);
        leftColumn.add(noteTopBar, BorderLayout.NORTH);
        leftColumn.add(notePreviewCard, BorderLayout.CENTER);
        refreshNoteSelection();
        splitContent.add(leftColumn);

        RoundedPanel rightPanel = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS);
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        ScrollablePanel rightContentContainer = new ScrollablePanel(null);
        rightContentContainer.setLayout(new BoxLayout(rightContentContainer, BoxLayout.Y_AXIS));

        JLabel formTitle = new JLabel("New Flashcard Details");
        formTitle.setFont(Theme.HEADING);
        formTitle.setForeground(Theme.TEXT);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(formTitle);
        rightContentContainer.add(Box.createVerticalStrut(12));

        rightContentContainer.add(label("Select or Create Deck"));
        deckSelector = new RoundedComboBox<>();
        deckSelector.setFont(Theme.BODY);
        deckSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        deckSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        deckSelector.addActionListener(e -> toggleNewDeckField());
        rightContentContainer.add(deckSelector);
        rightContentContainer.add(Box.createVerticalStrut(8));

        newDeckField = new RoundedTextField();
        newDeckField.setFont(Theme.BODY);
        newDeckField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        newDeckField.setAlignmentX(Component.LEFT_ALIGNMENT);
        newDeckField.setVisible(false);
        rightContentContainer.add(newDeckField);
        rightContentContainer.add(Box.createVerticalStrut(10));

        rightContentContainer.add(label("Front (Question / Prompt)"));
        frontArea = new RoundedTextArea(3, 20);
        frontArea.setFont(Theme.BODY);
        frontArea.setLineWrap(true);
        frontArea.setWrapStyleWord(true);
        JScrollPane frontScroll = Theme.inputScrollPane(frontArea);
        frontScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        frontScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(frontScroll);
        rightContentContainer.add(Box.createVerticalStrut(10));

        rightContentContainer.add(label("Back (Answer / Definition)"));
        backArea = new RoundedTextArea(3, 20);
        backArea.setFont(Theme.BODY);
        backArea.setLineWrap(true);
        backArea.setWrapStyleWord(true);
        JScrollPane backScroll = Theme.inputScrollPane(backArea);
        backScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        backScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(backScroll);
        rightContentContainer.add(Box.createVerticalStrut(10));

        rightContentContainer.add(label("Tags (comma separated)"));
        tagsField = new RoundedTextField();
        tagsField.setFont(Theme.BODY);
        tagsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        tagsField.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(tagsField);
        rightContentContainer.add(Box.createVerticalStrut(14));

        JButton saveCardBtn = Theme.primaryButton("Save Flashcard");
        saveCardBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveCardBtn.addActionListener(e -> saveFlashcard());
        rightContentContainer.add(saveCardBtn);

        rightContentContainer.add(Box.createVerticalStrut(20));
        JPanel line = new JPanel();
        line.setBackground(Theme.BORDER);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(line);
        rightContentContainer.add(Box.createVerticalStrut(16));

        JLabel suggestedTitle = new JLabel("Suggested Flashcards");
        suggestedTitle.setFont(Theme.HEADING);
        suggestedTitle.setForeground(Theme.TEXT);
        suggestedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(suggestedTitle);
        rightContentContainer.add(Box.createVerticalStrut(4));

        JLabel suggestedHint = Theme.muted("The AI reads a document or note and suggests cards. Accept the ones you like.");
        suggestedHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(suggestedHint);
        rightContentContainer.add(Box.createVerticalStrut(8));

        JPanel aiButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        aiButtonRow.setOpaque(false);
        aiButtonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        importButton = Theme.button("Import Document");
        importButton.addActionListener(e -> generateFromDocument());
        generateButton = Theme.button("Generate From Selected Note");
        generateButton.addActionListener(e -> generateFromSelectedNote());
        aiButtonRow.add(importButton);
        aiButtonRow.add(generateButton);
        rightContentContainer.add(aiButtonRow);
        rightContentContainer.add(Box.createVerticalStrut(8));

        aiStatusLabel = Theme.muted("No suggestions yet.");
        aiStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(aiStatusLabel);
        rightContentContainer.add(Box.createVerticalStrut(8));

        processingCard = new ProcessingCard("Processing your document");
        processingCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        processingCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        rightContentContainer.add(processingCard);
        rightContentContainer.add(Box.createVerticalStrut(8));

        suggestedContainer = new ScrollablePanel(null);
        suggestedContainer.setLayout(new BoxLayout(suggestedContainer, BoxLayout.Y_AXIS));
        suggestedContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContentContainer.add(suggestedContainer);

        JScrollPane rightScroll = new JScrollPane(rightContentContainer);
        rightScroll.setBorder(null);
        rightScroll.setOpaque(false);
        rightScroll.getViewport().setOpaque(false);
        rightScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightPanel.add(rightScroll, BorderLayout.CENTER);

        splitContent.add(rightPanel);
        add(splitContent, BorderLayout.CENTER);

        refreshDecksDropdown();
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public void clearSuggestions() {
        activeSuggestions.clear();
        suggestedContainer.removeAll();
        suggestedContainer.revalidate();
        suggestedContainer.repaint();
    }

    private void generateFromDocument() {
        String document = ImportDocumentDialog.show(this);
        if (document != null) {
            generateSuggestions(document);
        }
    }

    private void generateFromSelectedNote() {
        Note selected = (Note) noteSelector.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "There is no note to read yet. Create one in the Notes tab first.",
                    "No note selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        StringBuilder selectedText = new StringBuilder();
        for (Note note : selectedNotes) {
            if (note.getBody() != null && !note.getBody().isBlank()) {
                if (selectedText.length() > 0) selectedText.append("\n\n");
                selectedText.append(note.getTitle()).append("\n").append(note.getBody());
            }
        }
        if (selectedText.length() == 0) {
            JOptionPane.showMessageDialog(this, "The selected notes still have no text inside.",
                    "Empty note", JOptionPane.WARNING_MESSAGE);
            return;
        }
        generateSuggestions(selectedText.toString());
    }

    private void generateSuggestions(String document) {
        generateButton.setEnabled(false);
        importButton.setEnabled(false);
        leftImportButton.setEnabled(false);
        processingCard.start("Reading your document...");
        activeSuggestions.clear();
        renderSuggestions();

        new SwingWorker<List<Flashcard>, String>() {
            @Override
            protected List<Flashcard> doInBackground() throws Exception {
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
                return generator.suggestFlashcards(document, deckTitle(), message -> publish(message));
            }

            @Override
            protected void process(List<String> updates) {
                processingCard.update(updates.get(updates.size() - 1));
            }

            @Override
            protected void done() {
                generateButton.setEnabled(true);
                importButton.setEnabled(true);
                leftImportButton.setEnabled(true);
                processingCard.stop();
                try {
                    List<Flashcard> result = get();
                    activeSuggestions.addAll(result);
                    renderSuggestions();
                    if (result.isEmpty()) {
                        aiStatusLabel.setText("The AI did not find clear cards in this document.");
                    } else {
                        aiStatusLabel.setText("Found " + result.size() + " cards. Press Add on the ones you want, then save.");
                    }
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
                    aiStatusLabel.setText("Generation failed.");
                    JOptionPane.showMessageDialog(CreateFlashcardPage.this, cause.getMessage(),
                            "The AI could not finish", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String deckTitle() {
        String selected = (String) deckSelector.getSelectedItem();
        if (NEW_DECK_OPTION.equals(selected)) {
            String typed = newDeckField.getText().trim();
            if (typed.isEmpty()) {
                return "General";
            }
            return typed;
        }
        if (selected == null || selected.isBlank()) {
            return "General";
        }
        return selected;
    }

    private void renderSuggestions() {
        suggestedContainer.removeAll();
        if (activeSuggestions.isEmpty()) {
            JLabel emptyLabel = Theme.muted("No suggestions yet. Generate from a note or import a document.");
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            suggestedContainer.add(emptyLabel);
        } else {
            for (int index = 0; index < activeSuggestions.size(); index++) {
                final Flashcard suggestion = activeSuggestions.get(index);
                final int position = index;

                RoundedPanel card = new RoundedPanel(Theme.BG, Theme.CORNER_RADIUS, Theme.BORDER);
                card.setLayout(new BorderLayout(8, 8));
                card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

                JTextArea cardText = new JTextArea("Front: " + suggestion.getFront() + "\nBack: " + suggestion.getBack());
                cardText.setFont(Theme.BODY);
                cardText.setForeground(Theme.TEXT);
                cardText.setEditable(false);
                cardText.setOpaque(false);
                cardText.setLineWrap(true);
                cardText.setWrapStyleWord(true);
                card.add(cardText, BorderLayout.CENTER);

                JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                btnRow.setOpaque(false);

                JButton addBtn = Theme.primaryButton("Add");
                addBtn.addActionListener(e -> {
                    frontArea.setText(suggestion.getFront());
                    backArea.setText(suggestion.getBack());
                    aiStatusLabel.setText("Card loaded into the form. Press Save Flashcard.");
                });

                JButton deleteBtn = Theme.button("Ignore");
                deleteBtn.addActionListener(e -> {
                    activeSuggestions.remove(position);
                    renderSuggestions();
                });

                btnRow.add(addBtn);
                btnRow.add(deleteBtn);
                card.add(btnRow, BorderLayout.SOUTH);

                suggestedContainer.add(card);
                suggestedContainer.add(Box.createVerticalStrut(10));
            }
        }
        suggestedContainer.revalidate();
        suggestedContainer.repaint();
    }

    public void refreshDecksDropdown() {
        deckSelector.removeAllItems();
        Set<String> existingDecks = new LinkedHashSet<>();
        for (Flashcard card : AppData.get().getFlashcards()) {
            if (card.getDeckTitle() != null && card.getDeckTitle().isBlank() == false) {
                existingDecks.add(card.getDeckTitle());
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
        clearSuggestions();
        List<Note> notes = AppData.get().getAllNotes();
        java.util.Map<String, Note> byId = new java.util.HashMap<>();
        for (Note note : notes) byId.put(note.getId(), note);
        List<Note> refreshedSelection = new ArrayList<>();
        for (Note note : selectedNotes) {
            Note refreshed = byId.get(note.getId());
            if (refreshed != null) refreshedSelection.add(refreshed);
        }
        selectedNotes.clear();
        selectedNotes.addAll(refreshedSelection);
        noteSelector.removeAllItems();
        for (int index = 0; index < notes.size(); index++) {
            noteSelector.addItem(notes.get(index));
        }

        refreshDecksDropdown();
        refreshNoteSelection();
    }

    private void refreshNoteSelection() {
        chipRow.removeAll();
        chipRow.add(new Pill("Orbiting Note:", PILL_LABEL, false));
        for (Note note : selectedNotes) {
            Pill chip = new Pill(note.getTitle() + "  \u00D7", PILL_NOTE, true);
            chip.setToolTipText("Click to remove");
            chip.addActionListener(e -> {
                selectedNotes.remove(note);
                clearSuggestions();
                refreshNoteSelection();
            });
            chipRow.add(chip);
        }
        chipRow.add(plusButton);
        chipRow.revalidate();
        chipRow.repaint();

        Note selected = selectedNotes.isEmpty() ? null : selectedNotes.get(0);
        noteSelector.setSelectedItem(selected);
        notePreviewContainer.removeAll();
        if (selectedNotes.isEmpty()) {
            JButton addNotesButton = Theme.button("Press + to add notes...");
            addNotesButton.addActionListener(e -> showNotePicker());
            JPanel emptyState = new JPanel(new GridBagLayout());
            emptyState.setOpaque(false);
            emptyState.add(addNotesButton);
            notePreviewContainer.add(emptyState, BorderLayout.CENTER);
        } else {
            StringBuilder preview = new StringBuilder();
            for (Note note : selectedNotes) {
                if (preview.length() > 0) preview.append("\n\n");
                preview.append(note.getTitle()).append("\n").append(note.getBody());
            }
            noteBodyArea.setText(preview.toString());
            noteBodyArea.setCaretPosition(0);
            notePreviewContainer.add(noteScroll, BorderLayout.CENTER);
        }
        notePreviewContainer.revalidate();
        notePreviewContainer.repaint();
    }

    private boolean isNoteSelected(Note note) {
        for (Note selected : selectedNotes) {
            if (selected.getId().equals(note.getId())) return true;
        }
        return false;
    }

    private void showNotePicker() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.SURFACE);
        menu.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        List<Note> allNotes = AppData.get().getAllNotes();
        List<Note> available = new ArrayList<>();
        for (Note note : allNotes) {
            if (!isNoteSelected(note)) available.add(note);
        }
        if (available.isEmpty()) {
            JMenuItem none = new JMenuItem(allNotes.isEmpty()
                    ? "No notes yet. Create one in the Notes tab." : "All notes are added");
            none.setEnabled(false);
            menu.add(none);
        } else {
            for (Note note : available) {
                String label = note.getTitle()
                        + (note.getCourseCode().isEmpty() ? "" : "  (" + note.getCourseCode() + ")");
                JMenuItem item = new JMenuItem(label);
                item.setFont(Theme.BODY);
                item.setOpaque(true);
                item.setBackground(Theme.SURFACE);
                item.setForeground(Theme.TEXT);
                item.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
                item.addActionListener(e -> {
                    selectedNotes.add(note);
                    clearSuggestions();
                    refreshNoteSelection();
                });
                menu.add(item);
            }
        }
        menu.show(plusButton, 0, plusButton.getHeight() + 4);
    }

    private void saveFlashcard() {
        String selectedOption = (String) deckSelector.getSelectedItem();
        String deck;
        if (NEW_DECK_OPTION.equals(selectedOption)) {
            deck = newDeckField.getText().trim();
        } else if (selectedOption == null) {
            deck = "";
        } else {
            deck = selectedOption.trim();
        }

        String front = frontArea.getText().trim();
        String back = backArea.getText().trim();
        String tagText = tagsField.getText().trim();

        if (deck.isEmpty() || front.isEmpty() || back.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Deck title, front, and back cannot be empty.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<String> tags = new ArrayList<String>();
            if (tagText.isBlank() == false) {
                String[] parts = tagText.split(",");
                for (int index = 0; index < parts.length; index++) {
                    if (parts[index].isBlank() == false) {
                        tags.add(parts[index].trim());
                    }
                }

            }
            Note source = (Note) noteSelector.getSelectedItem();
            String courseCode = "";
            if (source != null) {
                courseCode = source.getCourseCode();
            }
            flashcardManager.addFlashcard(deck, courseCode, front, back, tags);
            JOptionPane.showMessageDialog(this, "Successfully added");
            frontArea.setText("");
            backArea.setText("");
            tagsField.setText("");
            newDeckField.setText("");
            refreshDecksDropdown();
            deckSelector.setSelectedItem(deck);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Error saving: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class Pill extends JButton {
        private final Color fill;

        Pill(String text, Color fill, boolean clickable) {
            super(text);
            this.fill = fill;
            setFont(Theme.font(Font.PLAIN, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            if (clickable) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics metrics = getFontMetrics(getFont());
            return new Dimension(metrics.stringWidth(getText()) + 28, metrics.getHeight() + 10);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), (getWidth() - metrics.stringWidth(getText())) / 2,
                    (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
            g2.dispose();
        }
    }

    private static class PlusButton extends JButton {
        PlusButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Add a note");
            setPreferredSize(new Dimension(30, 30));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2.drawLine(centerX - 8, centerY, centerX + 8, centerY);
            g2.drawLine(centerX, centerY - 8, centerX, centerY + 8);
            g2.dispose();
        }
    }
}
