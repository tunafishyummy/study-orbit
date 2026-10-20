package ui.pages;

import data.AppData;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import model.Flashcard;
import ui.Theme;
import ui.components.RoundedPanel;

public class FlashcardsPage extends JPanel {
    private JPanel deckListPanel;
    private JLabel cardLabel;
    private RoundedPanel flashcardCard;
    private String selectedDeck = null;
    private List<Flashcard> currentDeckCards;
    private int currentIndex = 0;
    private boolean showingFront = true;

    // Animation state variables
    private Timer flipTimer;
    private boolean isFlipping = false;
    private float scaleX = 1.0f; 
    private boolean flippingToBack = false;

    public FlashcardsPage(Runnable onNavigateToCreate) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

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

        // Custom animated card container panel overriding paintComponent for scaling
        flashcardCard = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS) {
            @Override
            protected void paintComponent(Graphics g) {
                if (scaleX < 1.0f) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    int w = getWidth();
                    int h = getHeight();
                    int newW = Math.max(1, (int) (w * scaleX));
                    int offsetX = (w - newW) / 2;

                    g2.translate(offsetX, 0);
                    g2.scale(scaleX, 1.0);
                    super.paintComponent(g2);
                    g2.dispose();
                } else {
                    super.paintComponent(g);
                }
            }
        };
        flashcardCard.setLayout(new BorderLayout());
        flashcardCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Center Panel using GridBagLayout to guarantee absolute vertical & horizontal centering
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        cardLabel = new JLabel("", SwingConstants.CENTER);
        cardLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        cardLabel.setForeground(Theme.TEXT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(cardLabel, gbc);

        flashcardCard.add(centerPanel, BorderLayout.CENTER);

        // Click anywhere on the card to trigger animated flip
        MouseAdapter flipClickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startFlipAnimation();
            }
        };
        flashcardCard.addMouseListener(flipClickListener);
        centerPanel.addMouseListener(flipClickListener);
        cardLabel.addMouseListener(flipClickListener);

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

    private void startFlipAnimation() {
        if (isFlipping || currentDeckCards == null || currentDeckCards.isEmpty()) return;

        isFlipping = true;
        flippingToBack = showingFront;

        flipTimer = new Timer(15, e -> {
            if (flippingToBack == showingFront) {
                scaleX -= 0.15f;
                if (scaleX <= 0.0f) {
                    scaleX = 0.0f;
                    showingFront = !showingFront;
                    displayCurrentCard();
                }
            } else {
                scaleX += 0.15f;
                if (scaleX >= 1.0f) {
                    scaleX = 1.0f;
                    isFlipping = false;
                    ((Timer) e.getSource()).stop();
                }
            }
            flashcardCard.repaint();
        });
        flipTimer.start();
    }

    private void setCardText(String text) {
        String escaped = text != null ? text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>") : "";
        cardLabel.setText("<html><div style='text-align: center; padding: 20px;'>" + escaped + "</div></html>");
    }

    public void refreshDecks(Runnable onNavigateToCreate) {
        deckListPanel.removeAll();
        
        List<Flashcard> allCards = new ArrayList<>();
        for (Object obj : AppData.get().getFlashcards()) {
            if (obj instanceof Flashcard) {
                allCards.add((Flashcard) obj);
            }
        }

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
        currentDeckCards = new ArrayList<>();
        if (selectedDeck != null) {
            for (Flashcard f : allCards) {
                if (f.getDeckTitle().equalsIgnoreCase(selectedDeck)) currentDeckCards.add(f);
            }
        }
        currentIndex = 0;
        showingFront = true;
        scaleX = 1.0f;
        displayCurrentCard();
    }

    private void displayCurrentCard() {
        if (currentDeckCards != null && !currentDeckCards.isEmpty()) {
            Flashcard f = currentDeckCards.get(currentIndex);
            if (showingFront) {
                setCardText(f.getFront());
            } else {
                setCardText(f.getBack());
            }
        } else if (selectedDeck != null) {
            setCardText("No flashcards in this deck yet.<br>Click 'ADD FLASHCARD' to generate them from your notes!");
        } else {
            setCardText("NO DECKS YET<br>Create a flashcard from your notes to start reviewing!");
        }
    }

    private void showNextCard() {
        if (currentDeckCards != null && !currentDeckCards.isEmpty()) {
            currentIndex = (currentIndex + 1) % currentDeckCards.size();
            showingFront = true;
            scaleX = 1.0f;
            displayCurrentCard();
            flashcardCard.repaint();
        }
    }

    private void showPreviousCard() {
        if (currentDeckCards != null && !currentDeckCards.isEmpty()) {
            currentIndex = (currentIndex - 1 + currentDeckCards.size()) % currentDeckCards.size();
            showingFront = true;
            scaleX = 1.0f;
            displayCurrentCard();
            flashcardCard.repaint();
        }
    }
}
