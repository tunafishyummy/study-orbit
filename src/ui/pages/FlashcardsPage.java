package ui.pages;

import data.AppData;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import model.Flashcard;
import ui.Theme;
import ui.components.RoundedPanel;

public class FlashcardsPage extends JPanel {
    private JPanel deckListPanel;
    private JLabel titleLabel;
    private JTextArea bodyTextArea;
    private String selectedDeck = null;
    private List<Flashcard> currentDeckCards;
    private int currentIndex = 0;

    public FlashcardsPage(Runnable onNavigateToCreate) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel header = Theme.header("Review Flashcards");
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(24, 0));
        content.setOpaque(false);

        deckListPanel = new JPanel();
        deckListPanel.setLayout(new BoxLayout(deckListPanel, BoxLayout.Y_AXIS));
        deckListPanel.setOpaque(false);
        deckListPanel.setPreferredSize(new Dimension(240, 0));

        JScrollPane deckScroll = new JScrollPane(deckListPanel);
        deckScroll.setOpaque(false);
        deckScroll.getViewport().setOpaque(false);
        deckScroll.setBorder(null);
        content.add(deckScroll, BorderLayout.WEST);

        RoundedPanel flashcardCard = new RoundedPanel(Theme.SURFACE, 20);
        flashcardCard.setLayout(new BorderLayout());
        flashcardCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        titleLabel = new JLabel("SELECT A DECK", SwingConstants.CENTER);
        titleLabel.setFont(Theme.HEADING);
        titleLabel.setForeground(Theme.TEXT);
        flashcardCard.add(titleLabel, BorderLayout.NORTH);

        bodyTextArea = new JTextArea("No flashcards available. Click 'ADD FLASHCARD' to generate them from your notes!");
        bodyTextArea.setFont(Theme.BODY);
        bodyTextArea.setForeground(Theme.TEXT);
        bodyTextArea.setLineWrap(true);
        bodyTextArea.setWrapStyleWord(true);
        bodyTextArea.setEditable(false);
        bodyTextArea.setOpaque(false);
        bodyTextArea.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        flashcardCard.add(bodyTextArea, BorderLayout.CENTER);

        JPanel cardNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        cardNav.setOpaque(false);
        JButton prevBtn = Theme.button("Previous");
        JButton nextBtn = Theme.primaryButton("Next Card");
        prevBtn.addActionListener(e -> showPreviousCard());
        nextBtn.addActionListener(e -> showNextCard());
        cardNav.add(prevBtn);
        cardNav.add(nextBtn);
        flashcardCard.add(cardNav, BorderLayout.SOUTH);

        content.add(flashcardCard, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        refreshDecks(onNavigateToCreate);
    }

    public void refreshDecks(Runnable onNavigateToCreate) {
        deckListPanel.removeAll();
        List<Flashcard> allCards = AppData.get().getFlashcards();
        Set<String> decks = new LinkedHashSet<>();
        Set<String> seenLower = new java.util.HashSet<>();

        for (Flashcard f : allCards) {
            if (f.getDeckTitle() != null && !f.getDeckTitle().isBlank()) {
                String trimmed = f.getDeckTitle().trim();
                if (seenLower.add(trimmed.toLowerCase())) {
                    decks.add(trimmed);
                }
            }
        }

        if (selectedDeck == null || !decks.stream().anyMatch(d -> d.equalsIgnoreCase(selectedDeck))) {
            if (!decks.isEmpty()) {
                selectedDeck = decks.iterator().next();
            } else {
                selectedDeck = null;
            }
        }

        for (String deck : decks) {
            JButton deckBtn = new JButton(deck.toUpperCase());
            deckBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            deckBtn.setMaximumSize(new Dimension(220, 45));
            deckBtn.setFocusPainted(false);
            deckBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            if (deck.equalsIgnoreCase(selectedDeck)) {
                deckBtn.setBackground(Theme.PRIMARY);
                deckBtn.setForeground(Color.WHITE);
            } else {
                deckBtn.setBackground(Theme.SURFACE);
                deckBtn.setForeground(Theme.TEXT);
            }
            deckBtn.addActionListener(e -> {
                selectedDeck = deck;
                updateCardDisplay(allCards);
                refreshDecks(onNavigateToCreate);
            });
            deckListPanel.add(deckBtn);
            deckListPanel.add(Box.createVerticalStrut(10));
        }

        JButton addBtn = Theme.primaryButton("ADD FLASHCARD");
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(220, 40));
        addBtn.addActionListener(e -> onNavigateToCreate.run());
        
        deckListPanel.add(Box.createVerticalStrut(20));
        deckListPanel.add(addBtn);
        deckListPanel.revalidate();
        deckListPanel.repaint();

        updateCardDisplay(allCards);
    }

    private void updateCardDisplay(List<Flashcard> allCards) {
        currentDeckCards = new java.util.ArrayList<>();
        if (selectedDeck != null) {
            for (Flashcard f : allCards) {
                if (f.getDeckTitle().equalsIgnoreCase(selectedDeck)) currentDeckCards.add(f);
            }
        }
        currentIndex = 0;
        displayCurrentCard();
    }

    private void displayCurrentCard() {
        if (currentDeckCards != null && !currentDeckCards.isEmpty()) {
            Flashcard f = currentDeckCards.get(currentIndex);
            titleLabel.setText(f.getFront());
            bodyTextArea.setText(f.getBack());
        } else if (selectedDeck != null) {
            titleLabel.setText(selectedDeck.toUpperCase());
            bodyTextArea.setText("No flashcards in this deck yet. Click 'ADD FLASHCARD' to generate them from your notes!");
        } else {
            titleLabel.setText("NO DECKS YET");
            bodyTextArea.setText("Create a flashcard from your notes to start reviewing!");
        }
    }

    private void showNextCard() {
        if (currentDeckCards != null && !currentDeckCards.isEmpty()) {
            currentIndex = (currentIndex + 1) % currentDeckCards.size();
            displayCurrentCard();
        }
    }

    private void showPreviousCard() {
        if (currentDeckCards != null && !currentDeckCards.isEmpty()) {
            currentIndex = (currentIndex - 1 + currentDeckCards.size()) % currentDeckCards.size();
            displayCurrentCard();
        }
    }
}