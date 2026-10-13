package ui.pages;

import ui.Assets;
import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 1, the welcome page
public class SplashPage extends JPanel {

    public SplashPage(Runnable onContinue) {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 0));

        JLabel title = new JLabel("<html>Study<br>Orbit</html>");
        title.setFont(new Font("SansSerif", Font.PLAIN, 68));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel profile = new JLabel("No selected profile");
        profile.setFont(new Font("SansSerif", Font.BOLD, 18));
        profile.setForeground(Theme.TEXT);
        profile.setAlignmentX(Component.LEFT_ALIGNMENT);

        Chevron chevron = new Chevron();
        chevron.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton continueButton = new JButton("Continue");
        continueButton.setFont(new Font("SansSerif", Font.PLAIN, 20));
        continueButton.setForeground(Theme.MUTED);
        continueButton.setBackground(Theme.BG);
        continueButton.setContentAreaFilled(false);
        continueButton.setBorderPainted(false);
        continueButton.setFocusPainted(false);
        continueButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        continueButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        continueButton.addActionListener(e -> onContinue.run());

        left.add(Box.createVerticalGlue());
        left.add(title);
        left.add(Box.createVerticalStrut(44));
        left.add(profile);
        left.add(Box.createVerticalStrut(6));
        left.add(chevron);
        left.add(Box.createVerticalStrut(26));
        left.add(continueButton);
        left.add(Box.createVerticalGlue());

        // the logo keeps its own size and aspect ratio, anchored to the lower right
        JPanel art = new JPanel(new GridBagLayout());
        art.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 24);

        JLabel logo = new JLabel();
        Image image = Assets.image("Logo.png");
        if (image != null) {
            logo.setIcon(new ImageIcon(image.getScaledInstance(460, -1, Image.SCALE_SMOOTH)));
        }
        art.add(logo, c);

        add(left, BorderLayout.WEST);
        add(art, BorderLayout.CENTER);
    }

    // the small dropdown indicator under the profile line
    private static class Chevron extends JComponent {
        Chevron() {
            setPreferredSize(new Dimension(14, 9));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.TEXT);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(1, 1, 7, 7);
            g2.drawLine(7, 7, 13, 1);
        }
    }
}
