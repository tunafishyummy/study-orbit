package ui.pages;

import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 5, make flashcards from a note
public class FlashcardsPage extends JPanel {

    public FlashcardsPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(header(), BorderLayout.NORTH);
        add(body(), BorderLayout.CENTER);
        add(footer(), BorderLayout.SOUTH);
    }

    private JPanel header() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        header.add(Theme.header("Create Flashcards"));
        header.add(chip("From Notes", Theme.HEADER_GREEN));
        return header;
    }

    private JPanel body() {
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 16));
        body.setOpaque(false);

        JPanel noteCard = Theme.card();
        noteCard.setLayout(new BorderLayout());
        body.add(noteCard);

        JPanel cards = Theme.card();
        cards.setLayout(new BorderLayout());
        JLabel suggested = new JLabel("Suggested Cards");
        suggested.setFont(Theme.HEADING);
        suggested.setForeground(Theme.TEXT);
        cards.add(suggested, BorderLayout.NORTH);
        body.add(cards);

        return body;
    }

    private JPanel footer() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(Theme.button("Cancel"));
        footer.add(Theme.primaryButton("Save cards to review"));
        return footer;
    }

    private JLabel chip(String text, Color color) {
        JLabel chip = new JLabel(" " + text + " ");
        chip.setFont(Theme.BODY);
        chip.setForeground(Color.WHITE);
        chip.setBackground(color);
        chip.setOpaque(true);
        return chip;
    }
}
