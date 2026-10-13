package ui.pages;

import ui.Assets;
import ui.Theme;

import javax.swing.*;
import java.awt.*;

public class SplashPage extends JPanel {

    public SplashPage(Runnable onContinue) {
        setLayout(new GridBagLayout());
        setBackground(Theme.BG);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel();
        Image image = Assets.image("Logo.png");
        if (image != null) {
            logo.setIcon(new ImageIcon(image.getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
        }
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("STUDY ORBIT");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("An All-Around Learning Tool Built for Students");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setForeground(Theme.MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton continueButton = Theme.primaryButton("Continue");
        continueButton.addActionListener(e -> onContinue.run());
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel offline = Theme.muted("Works offline. No account needed.");
        offline.setAlignmentX(Component.CENTER_ALIGNMENT);

        column.add(logo);
        column.add(Box.createVerticalStrut(12));
        column.add(title);
        column.add(Box.createVerticalStrut(6));
        column.add(subtitle);
        column.add(Box.createVerticalStrut(28));
        column.add(continueButton);
        column.add(Box.createVerticalStrut(10));
        column.add(offline);

        add(column);
    }
}
