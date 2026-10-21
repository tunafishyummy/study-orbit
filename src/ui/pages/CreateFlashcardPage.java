package ui.pages;

import data.AIFlashcardService;
import data.AppData;
import data.FlashcardManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import model.Flashcard;
import model.Note;
import ui.Theme;
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
    private final List<AIFlashcardService.SuggestedCard> activeSuggestions = new ArrayList<>();
    private final Runnable onBackCallback;

    // Custom ScrollablePanel that forces exact viewport width matching to eliminate horizontal scrolling
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
            return true; // Crucial: forces child components to match viewport width exactly, preventing horizontal scrollbars
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

        // Left Panel: Reference Note Viewer
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

        // Right Panel: Form + AI Suggested Flashcards
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
        rightContentContainer.add(Box.createVerticalStrut(6));

        JButton generateAiBtn = Theme.button("Generate Cards from Note (AI)");
        generateAiBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateAiBtn.addActionListener(e -> fetchAiSuggestions());
        rightContentContainer.add(generateAiBtn);
        rightContentContainer.add(Box.createVerticalStrut(10));

        // Fixed BoxLayout initialization error
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

    public void clearSuggestions() {
        activeSuggestions.clear();
        suggestedContainer.removeAll();
        suggestedContainer.revalidate();
        suggestedContainer.repaint();
    }

    private AIFlashcardService aiService() {
        return new AIFlashcardService(AppData.settings().get(data.SettingsManager.API_KEY, ""));
    }

    private void fetchAiSuggestions() {
        Note selected = (Note) noteSelector.getSelectedItem();
        if (selected == null || selected.getBody().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select a valid note first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        suggestedContainer.removeAll();
        JLabel loadingLabel = Theme.muted("Generating suggestions with Gemini API...");
        loadingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        suggestedContainer.add(loadingLabel);
        suggestedContainer.revalidate();
        suggestedContainer.repaint();

        new Thread(() -> {
            try {
                List<AIFlashcardService.SuggestedCard> suggestions = aiService().generateSuggestions(selected.getTitle(), selected.getBody());
                SwingUtilities.invokeLater(() -> {
                    activeSuggestions.clear();
                    activeSuggestions.addAll(suggestions);
                    renderSuggestions();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    suggestedContainer.removeAll();
                    JLabel errLabel = new JLabel("Failed to generate: " + ex.getMessage());
                    errLabel.setForeground(Theme.PRIMARY);
                    errLabel.setFont(Theme.BODY);
                    errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    suggestedContainer.add(errLabel);
                    suggestedContainer.revalidate();
                    suggestedContainer.repaint();
                });
            }
        }).start();
    }

    private void fetchReplacementCard() {
        Note selected = (Note) noteSelector.getSelectedItem();
        if (selected == null || selected.getBody().isBlank()) return;

        new Thread(() -> {
            try {
                List<AIFlashcardService.SuggestedCard> newCards = aiService().generateSuggestions(selected.getTitle(), selected.getBody());
                if (!newCards.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        activeSuggestions.add(newCards.get(0));
                        renderSuggestions();
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void renderSuggestions() {
        suggestedContainer.removeAll();
        if (activeSuggestions.isEmpty()) {
            JLabel emptyLabel = Theme.muted("No suggestions generated.");
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            suggestedContainer.add(emptyLabel);
        } else {
            for (int i = 0; i < activeSuggestions.size(); i++) {
                AIFlashcardService.SuggestedCard sc = activeSuggestions.get(i);
                
                RoundedPanel card = new RoundedPanel(Theme.BG, Theme.CORNER_RADIUS, Theme.BORDER);
                card.setLayout(new BorderLayout(8, 8));
                card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

                JTextArea cardText = new JTextArea("Front: " + sc.front + "\nBack: " + sc.back);
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
                JButton deleteBtn = Theme.button("Delete");

                final int index = i;
                addBtn.addActionListener(e -> {
                    frontArea.setText(sc.front);
                    backArea.setText(sc.back);
                    activeSuggestions.remove(index);
                    renderSuggestions();
                    fetchReplacementCard();
                });

                deleteBtn.addActionListener(e -> {
                    activeSuggestions.remove(index);
                    renderSuggestions();
                    fetchReplacementCard();
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
        
        for (Object obj : AppData.get().getFlashcards()) {
            if (obj instanceof Flashcard) {
                Flashcard f = (Flashcard) obj;
                if (f.getDeckTitle() != null && !f.getDeckTitle().isBlank()) {
                    existingDecks.add(f.getDeckTitle());
                }
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
            // the card belongs to the course of the note it was made from
            Note source = (Note) noteSelector.getSelectedItem();
            String courseCode = source == null ? "" : source.getCourseCode();
            flashcardManager.addFlashcard(deck, courseCode, front, back, tags);
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