package ui;

import ai.PdfTextExtractor;
import data.DocumentTextReader;
import ui.components.NativeFileDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

// collects a supporting document for the AI features
public final class ImportDocumentDialog {
    private ImportDocumentDialog() {}

    public static String show(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Import a document", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(660, 500);
        dialog.setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel hint = Theme.muted("Paste your notes or readings below, or load a .txt, .md, .pdf or .docx file. The AI reads this document in the next step.");
        root.add(hint, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setFont(Theme.BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(8, 8, 8, 8));
        area.setBackground(Theme.SURFACE);
        area.setForeground(Theme.TEXT);
        area.setCaretColor(Theme.TEXT);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        root.add(scroll, BorderLayout.CENTER);

        final String[] result = { null };

        JButton load = Theme.button("Load a document file");
        load.addActionListener(e -> {
            NativeFileDialog fileDialog = new NativeFileDialog();
            File file = fileDialog.open("Choose a document",
                    "Text, markdown, PDF and Word files", new String[] { "txt", "md", "pdf", "docx" });
            if (file == null) {
                return;
            }
            try {
                String name = file.getName().toLowerCase();
                String text;
                if (name.endsWith(".pdf")) {
                    text = PdfTextExtractor.extract(file.toPath());
                    if (text.isBlank()) {
                        JOptionPane.showMessageDialog(dialog,
                                "No text could be read from this PDF. If it is a scanned or picture only PDF, copy the text and paste it below instead.",
                                "PDF has no readable text", JOptionPane.WARNING_MESSAGE);
                    } else if (PdfTextExtractor.looksGarbled(text)) {
                        JOptionPane.showMessageDialog(dialog,
                                "The text of this PDF did not come out clean. Some PDFs store their text in a way this reader cannot fully decode, so check the text below and paste the original instead if it looks wrong.",
                                "PDF text may be rough", JOptionPane.WARNING_MESSAGE);
                    }
                } else if (name.endsWith(".docx")) {
                    text = new DocumentTextReader().read(file);
                } else {
                    text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                }
                area.setText(text);
                area.setCaretPosition(0);
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(dialog, "Could not read the file: " + exception.getMessage(),
                        "Import failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton use = Theme.primaryButton("Use this text");
        use.addActionListener(e -> {
            String text = area.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "The document is still empty. Paste text or load a file first.",
                        "Nothing to import", JOptionPane.WARNING_MESSAGE);
                return;
            }
            result[0] = text;
            dialog.dispose();
        });

        JButton cancel = Theme.button("Cancel");
        cancel.addActionListener(e -> dialog.dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(load);
        buttons.add(cancel);
        buttons.add(use);
        root.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
        return result[0];
    }
}
