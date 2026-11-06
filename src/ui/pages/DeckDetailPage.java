package ui.pages;

import data.AppData;
import data.FlashcardManager;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import model.Flashcard;
import ui.Theme;
import ui.components.RoundedPanel;
import ui.components.RoundedTextArea;
import ui.components.RoundedTextField;
import ui.dialogs.FormDialog;

// one deck with its cards and the review, edit and delete tools
public class DeckDetailPage extends JPanel {
    private final Runnable onBack;
    private final FlashcardManager manager;
    private final JLabel deckTitle = new JLabel(" ");
    private final JLabel deckMeta = new JLabel(" ");
    private final JLabel cardLabel = new JLabel("", SwingConstants.CENTER);
    private final JButton editCardButton = Theme.button("Edit card");
    private final JButton deleteCardButton = Theme.button("Delete card");
    private final RoundedPanel flashcardCard;
    private String deckName;
    private List<Flashcard> deckCards = new ArrayList<Flashcard>();
    private int currentIndex;
    private boolean showingFront = true;
    private boolean isFlipping;
    private boolean flippingToBack;
    private float scaleX = 1.0f;
    private Timer flipTimer;

    public DeckDetailPage(Runnable onBack) {
        this.onBack = onBack;
        this.manager = new FlashcardManager(AppData.get().getStoreReference());
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);

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

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        cardLabel.setFont(Theme.font(Font.BOLD, 22));
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

        MouseAdapter flipClickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
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

        JPanel cardNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        cardNav.setOpaque(false);
        JButton prevButton = Theme.button("Previous");
        JButton nextButton = Theme.primaryButton("Next Card");
        prevButton.addActionListener(e -> showPreviousCard());
        nextButton.addActionListener(e -> showNextCard());
        editCardButton.addActionListener(e -> editCurrentCard());
        deleteCardButton.addActionListener(e -> deleteCurrentCard());
        cardNav.add(prevButton);
        cardNav.add(nextButton);
        cardNav.add(editCardButton);
        cardNav.add(deleteCardButton);
        flashcardCard.add(cardNav, BorderLayout.SOUTH);

        add(flashcardCard, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                refresh();
            }
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel identity = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        identity.setOpaque(false);
        JButton back = Theme.button("<- All decks");
        back.addActionListener(e -> onBack.run());
        identity.add(back);
        identity.add(Box.createHorizontalStrut(12));

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        deckTitle.setFont(Theme.font(Font.BOLD, 24));
        deckTitle.setForeground(Theme.TEXT);
        deckTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        deckMeta.setFont(Theme.BODY);
        deckMeta.setForeground(Theme.MUTED);
        deckMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        names.add(deckTitle);
        names.add(Box.createVerticalStrut(2));
        names.add(deckMeta);
        identity.add(names);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton rename = Theme.button("Rename deck");
        rename.addActionListener(e -> renameDeck());
        JButton delete = Theme.button("Delete deck");
        delete.addActionListener(e -> deleteDeck());
        actions.add(rename);
        actions.add(delete);

        header.add(identity, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    public void showDeck(String name) {
        deckName = name;
        currentIndex = 0;
        refresh();
    }

    public void restoreDeck(String name) {
        showDeck(name);
    }

    public void showCard(Flashcard card) {
        deckName = card.getDeckTitle();
        currentIndex = 0;
        refresh();
        for (int index = 0; index < deckCards.size(); index++) {
            if (deckCards.get(index).getId().equals(card.getId())) {
                currentIndex = index;
                break;
            }
        }
        showingFront = true;
        scaleX = 1.0f;
        displayCurrentCard();
        flashcardCard.repaint();
    }

    private void refresh() {
        deckCards = new ArrayList<Flashcard>();
        if (deckName != null) {
            for (Flashcard card : manager.getAllFlashcards()) {
                if (card.getDeckTitle().equalsIgnoreCase(deckName)) {
                    deckCards.add(card);
                }
            }
        }
        if (currentIndex >= deckCards.size()) {
            currentIndex = deckCards.isEmpty() ? 0 : deckCards.size() - 1;
        }
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        deckTitle.setText(deckName == null ? "Deck" : deckName);
        deckMeta.setText(deckCards.size() + (deckCards.size() == 1 ? " card" : " cards")
                + " - click the card to flip");
        showingFront = true;
        scaleX = 1.0f;
        displayCurrentCard();
    }

    private Flashcard currentCard() {
        if (deckCards.isEmpty()) {
            return null;
        }
        if (currentIndex < 0 || currentIndex >= deckCards.size()) {
            return null;
        }
        return deckCards.get(currentIndex);
    }

    private void displayCurrentCard() {
        Flashcard card = currentCard();
        if (card == null) {
            setCardText(deckName == null
                    ? "Pick a deck first."
                    : "This deck has no cards yet. Add one from the Create page.");
        } else if (showingFront) {
            setCardText(card.getFront());
        } else {
            setCardText(card.getBack());
        }
        editCardButton.setEnabled(card != null);
        deleteCardButton.setEnabled(card != null);
    }

    private void showNextCard() {
        if (deckCards.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex + 1) % deckCards.size();
        showingFront = true;
        scaleX = 1.0f;
        displayCurrentCard();
        flashcardCard.repaint();
    }

    private void showPreviousCard() {
        if (deckCards.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex - 1 + deckCards.size()) % deckCards.size();
        showingFront = true;
        scaleX = 1.0f;
        displayCurrentCard();
        flashcardCard.repaint();
    }

    private void startFlipAnimation() {
        if (isFlipping || deckCards.isEmpty()) {
            return;
        }
        isFlipping = true;
        flippingToBack = showingFront;
        if (flippingToBack) {
            try {
                AppData.activity().recordCards(1);
            } catch (java.io.IOException exception) {
                System.out.println("Could not record the card review: " + exception.getMessage());
            }
        }

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
        String escaped = text != null
                ? text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>")
                : "";
        cardLabel.setText("<html><div style='text-align: center; padding: 20px;'>" + escaped + "</div></html>");
    }

    private void editCurrentCard() {
        Flashcard card = currentCard();
        if (card == null) {
            return;
        }

        RoundedTextField deckField = new RoundedTextField();
        deckField.setText(card.getDeckTitle());
        deckField.setPreferredSize(new Dimension(420, 34));

        RoundedTextArea frontArea = new RoundedTextArea(3, 20);
        frontArea.setFont(Theme.BODY);
        frontArea.setLineWrap(true);
        frontArea.setWrapStyleWord(true);
        frontArea.setText(card.getFront());
        JScrollPane frontScroll = Theme.inputScrollPane(frontArea);
        frontScroll.setPreferredSize(new Dimension(420, 80));

        RoundedTextArea backArea = new RoundedTextArea(3, 20);
        backArea.setFont(Theme.BODY);
        backArea.setLineWrap(true);
        backArea.setWrapStyleWord(true);
        backArea.setText(card.getBack());
        JScrollPane backScroll = Theme.inputScrollPane(backArea);
        backScroll.setPreferredSize(new Dimension(420, 80));

        RoundedTextField tagsField = new RoundedTextField();
        tagsField.setText(String.join(", ", card.getTags()));
        tagsField.setPreferredSize(new Dimension(420, 34));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.add(formLabel("Deck"));
        form.add(deckField);
        form.add(formLabel("Front"));
        form.add(frontScroll);
        form.add(formLabel("Back"));
        form.add(backScroll);
        form.add(formLabel("Tags (comma separated)"));
        form.add(tagsField);

        if (FormDialog.show(this, "Edit flashcard", form) == false) {
            return;
        }

        try {
            String deck = deckField.getText().trim();
            manager.updateFlashcard(card, deck, frontArea.getText(), backArea.getText(),
                    parseTags(tagsField.getText()));
            deckName = deck;
            currentIndex = 0;
            refresh();
            if (deckCards.isEmpty()) {
                onBack.run();
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Could not save the card: " + exception.getMessage(),
                    "Edit flashcard", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCurrentCard() {
        Flashcard card = currentCard();
        if (card == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete this card?\n\nFront: " + card.getFront(),
                "Delete flashcard", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            manager.deleteFlashcard(card);
            refresh();
            if (deckCards.isEmpty()) {
                onBack.run();
            }
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, "Could not delete the card: " + exception.getMessage(),
                    "Delete flashcard", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renameDeck() {
        if (deckName == null) {
            return;
        }
        String oldDeck = deckName;
        RoundedTextField nameField = new RoundedTextField();
        nameField.setText(oldDeck);
        nameField.setPreferredSize(new Dimension(360, 34));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.add(formLabel("Deck name"));
        form.add(nameField);

        if (FormDialog.show(this, "Rename deck", form) == false) {
            return;
        }
        String newDeck = nameField.getText().trim();
        if (newDeck.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The deck name cannot be empty.",
                    "Rename deck", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            manager.renameDeck(oldDeck, newDeck);
            deckName = newDeck;
            refresh();
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, "Could not rename the deck: " + exception.getMessage(),
                    "Rename deck", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDeck() {
        if (deckName == null) {
            return;
        }
        int count = deckCards.size();
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete the deck \"" + deckName + "\" and its " + count + " card"
                        + (count == 1 ? "" : "s") + "?",
                "Delete deck", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            manager.deleteDeck(deckName);
            onBack.run();
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, "Could not delete the deck: " + exception.getMessage(),
                    "Delete deck", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT);
        return label;
    }

    private static List<String> parseTags(String text) {
        List<String> tags = new ArrayList<String>();
        if (text == null) {
            return tags;
        }
        String[] parts = text.split(",");
        for (int index = 0; index < parts.length; index++) {
            if (parts[index].isBlank() == false) {
                tags.add(parts[index].trim());
            }
        }
        return tags;
    }
}
