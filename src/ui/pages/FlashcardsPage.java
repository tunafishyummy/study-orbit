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
    private final Runnable onNavigateToCreate;

    // Animation state variables
    private Timer flipTimer;
    private boolean isFlipping = false;
    private float scaleX = 1.0f; 
    private boolean flippingToBack = false;

    public FlashcardsPage(Runnable onNavigateToCreate) {
        this.onNavigateToCreate = onNavigateToCreate;
        setLayout(new BorderLayout(0, 8));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        JLabel header = Theme.header("Review Flashcards");
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);

        deckListPanel = new DeckColumnPanel();
        deckListPanel.setLayout(new BoxLayout(deckListPanel, BoxLayout.Y_AXIS));
        deckListPanel.setOpaque(false);

        JScrollPane deckScroll = new JScrollPane(deckListPanel);
        deckScroll.setOpaque(false);
        deckScroll.getViewport().setOpaque(false);
        deckScroll.setBorder(null);
        deckScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(deckScroll, BorderLayout.WEST);

        // Custom animated card container panel overriding paintComponent for scaling
        flashcardCard = new RoundedPanel(Theme.SURFACE, 20) {
            @Override
            protected void paintComponent(Graphics g) {
                if (scaleX < 1.0f) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    int w = getWidth();
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
        Cursor flipCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        flashcardCard.setCursor(flipCursor);
        centerPanel.setCursor(flipCursor);
        cardLabel.setCursor(flipCursor);

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

        refresh();
    }

    public void refresh() {
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
                refresh();
            });
            deckListPanel.add(deckBtn);
            deckListPanel.add(Box.createVerticalStrut(10));
        }

        JButton addBtn = new AddFlashcardButton();
        addBtn.addActionListener(e -> onNavigateToCreate.run());
        RoundedPanel addFlashcardContainer = new RoundedPanel(Theme.STUDY_CELL, 16);
        addFlashcardContainer.setLayout(new GridBagLayout());
        addFlashcardContainer.setPreferredSize(new Dimension(220, 64));
        addFlashcardContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        addFlashcardContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        addFlashcardContainer.add(addBtn);
        deckListPanel.add(Box.createVerticalStrut(10));
        deckListPanel.add(addFlashcardContainer);
        deckListPanel.revalidate();
        deckListPanel.repaint();

        updateCardDisplay(allCards);
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

    // opens the deck of this card and shows the card, used when a search result is clicked
    public void showCard(Flashcard card) {
        selectedDeck = card.getDeckTitle().trim();
        refresh();   // rebuilds the deck list and loads this deck's cards
        for (int i = 0; i < currentDeckCards.size(); i++) {
            if (currentDeckCards.get(i).getId().equals(card.getId())) {
                currentIndex = i;
                break;
            }
        }
        showingFront = true;   // always start on the question side
        scaleX = 1.0f;
        displayCurrentCard();
        flashcardCard.repaint();
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
            setCardText("No flashcards in this deck yet. Click 'ADD FLASHCARD' to generate them from your notes!");
        } else {
            setCardText("NO DECKS YET Create a flashcard from your notes to start reviewing!");
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

    private static class AddFlashcardButton extends ui.components.RoundedButton {
        AddFlashcardButton() {
            super("");
            setToolTipText("Add flashcard");
            setBackground(new Color(0x3B3B3B));
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(40, 40));
            setMaximumSize(new Dimension(40, 40));
            setFocusPainted(false);
            setBorderPainted(false);
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
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

    private static class DeckColumnPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(240, getPreferredSize().height);
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
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
}