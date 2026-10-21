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

    private JComboBox<Note> noteSelector;
    private JTextArea noteBodyArea;
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

        RoundedPanel leftPanel = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS);
        leftPanel.setLayout(new BorderLayout(0, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel noteTopBar = new JPanel(new BorderLayout(10, 0));
        noteTopBar.setOpaque(false);
        JLabel noteLabel = new JLabel("Reference Note:");
        noteLabel.setFont(Theme.HEADING);
        noteLabel.setForeground(Theme.TEXT);
        noteTopBar.add(noteLabel, BorderLayout.WEST);

        List<Note> notes = AppData.get().getAllNotes();
        noteSelector = new RoundedComboBox<>(notes.toArray(new Note[0]));
        noteSelector.setFont(Theme.BODY);
        noteSelector.addActionListener(e -> {
            clearSuggestions();
            updateNoteContent();
        });
        noteTopBar.add(noteSelector, BorderLayout.CENTER);
        leftPanel.add(noteTopBar, BorderLayout.NORTH);

        noteBodyArea = new RoundedTextArea();
        noteBodyArea.setFont(Theme.BODY);
        noteBodyArea.setForeground(Theme.TEXT);
        noteBodyArea.setLineWrap(true);
        noteBodyArea.setWrapStyleWord(true);
        noteBodyArea.setEditable(false);
        noteBodyArea.setOpaque(false);

        JScrollPane noteScroll = Theme.inputScrollPane(noteBodyArea);
        leftPanel.add(noteScroll, BorderLayout.CENTER);
        splitContent.add(leftPanel);

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

        if (notes.isEmpty() == false) {
            noteSelector.setSelectedIndex(0);
            updateNoteContent();
        } else {
            noteBodyArea.setText("No notes available. Create a note first in the Notes tab!");
        }
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
        if (selected.getBody() == null || selected.getBody().isBlank()) {
            JOptionPane.showMessageDialog(this, "This note still has no text inside.",
                    "Empty note", JOptionPane.WARNING_MESSAGE);
            return;
        }
        generateSuggestions(selected.getBody());
    }

    private void generateSuggestions(String document) {
        generateButton.setEnabled(false);
        importButton.setEnabled(false);
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
        noteSelector.removeAllItems();
        for (int index = 0; index < notes.size(); index++) {
            noteSelector.addItem(notes.get(index));
        }

        refreshDecksDropdown();

        if (notes.isEmpty() == false) {
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
}
