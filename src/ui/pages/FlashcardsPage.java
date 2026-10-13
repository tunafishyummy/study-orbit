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
        noteCard.setLayout(new BorderLayout(0, 10));
        JLabel noteTitle = new JLabel("When to use inheritance (OOP)");
        noteTitle.setFont(Theme.HEADING);
        noteTitle.setForeground(Theme.TEXT);
        JTextArea noteText = new JTextArea(
                "Inheritance is used when two classes share the same fields and methods. "
                + "The shared parts live in a parent class and the child class reuses them. "
                + "It keeps the code shorter and easier to change, since a fix in the parent "
                + "reaches every child class. A child class can also add its own fields or "
                + "override a method when the behavior needs to be different.");
        noteText.setFont(Theme.BODY);
        noteText.setForeground(Theme.TEXT);
        noteText.setBackground(Theme.SURFACE);
        noteText.setLineWrap(true);
        noteText.setWrapStyleWord(true);
        noteText.setEditable(false);
        noteCard.add(noteTitle, BorderLayout.NORTH);
        noteCard.add(noteText, BorderLayout.CENTER);
        body.add(noteCard);

        JPanel cards = Theme.card();
        cards.setLayout(new BorderLayout(0, 10));
        JLabel suggested = new JLabel("Suggested Cards (3)");
        suggested.setFont(Theme.HEADING);
        suggested.setForeground(Theme.TEXT);
        cards.add(suggested, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(cardRow("What is inheritance?",
                "A class that reuses the fields and methods of another class."));
        list.add(Box.createVerticalStrut(10));
        list.add(cardRow("When should inheritance be used?",
                "When two classes share the same fields and methods."));
        list.add(Box.createVerticalStrut(10));
        list.add(cardRow("Why does inheritance make code easier to change?",
                "A fix in the parent class reaches every child class."));
        cards.add(list, BorderLayout.CENTER);
        body.add(cards);

        return body;
    }

    private JPanel cardRow(String front, String back) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setBackground(Theme.BG);
        row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel frontLabel = new JLabel("Front  " + front);
        frontLabel.setFont(Theme.BODY);
        frontLabel.setForeground(Theme.TEXT);
        frontLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel backLabel = new JLabel("Back  " + back);
        backLabel.setFont(Theme.BODY);
        backLabel.setForeground(Theme.MUTED);
        backLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        texts.add(frontLabel);
        texts.add(Box.createVerticalStrut(3));
        texts.add(backLabel);
        row.add(texts, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        JButton accept = Theme.button("Accept");
        accept.addActionListener(e -> accept.setText("Accepted"));
        JButton edit = Theme.button("Edit");
        buttons.add(accept);
        buttons.add(edit);
        row.add(buttons, BorderLayout.EAST);
        return row;
    }

    private JPanel footer() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(Theme.button("Cancel"));
        footer.add(Theme.primaryButton("Save 3 cards to review"));
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
