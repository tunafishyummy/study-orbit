package ui.components;

import ui.Theme;

import javax.swing.*;
import java.awt.*;

// a small card that shows the AI is working
public class ProcessingCard extends JPanel {
    private final JLabel statusLabel;
    private final JProgressBar bar;

    public ProcessingCard(String title) {
        setLayout(new BorderLayout(0, 8));
        setBackground(Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.HEADING);
        titleLabel.setForeground(Theme.TEXT);
        add(titleLabel, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBackground(Theme.BG);
        bar.setForeground(Theme.PRIMARY);
        bar.setBorderPainted(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        bar.setPreferredSize(new Dimension(10, 10));

        statusLabel = Theme.muted("Starting...");
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(bar);
        center.add(Box.createVerticalStrut(6));
        center.add(statusLabel);
        add(center, BorderLayout.CENTER);

        setVisible(false);
    }

    public void start(String message) {
        statusLabel.setText(message);
        setVisible(true);
        revalidate();
        repaint();
    }

    public void update(String message) {
        statusLabel.setText(message);
    }

    public void stop() {
        setVisible(false);
        revalidate();
        repaint();
    }
}
