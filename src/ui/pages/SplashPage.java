package ui.pages;

import java.awt.*;
import javax.swing.*;
import ui.Theme;

public class SplashPage extends JPanel {

    public SplashPage(Runnable onContinue) {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JButton continueButton = Theme.primaryButton("Continue");
        continueButton.addActionListener(e -> onContinue.run());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(continueButton);
        add(buttonRow, BorderLayout.SOUTH);
    }
}
