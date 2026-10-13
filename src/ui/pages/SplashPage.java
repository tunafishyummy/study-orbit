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

        JLabel title = new JLabel("Study Orbit");
        title.setFont(Theme.font(Font.PLAIN, 68));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel motto = new JLabel("An All-Around Learning Tool Built for Students");
        motto.setFont(Theme.font(Font.PLAIN, 16));
        motto.setForeground(Theme.MUTED);
        motto.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel profile = new JLabel("No selected profile");
        profile.setFont(Theme.font(Font.BOLD, 18));
        profile.setForeground(Theme.TEXT);
        profile.setAlignmentX(Component.LEFT_ALIGNMENT);

        Chevron chevron = new Chevron();
        chevron.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton continueButton = new JButton("Continue");
        continueButton.setFont(Theme.font(Font.PLAIN, 20));
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
        left.add(Box.createVerticalStrut(16));
        left.add(motto);
        left.add(Box.createVerticalStrut(38));
        left.add(profile);
        left.add(Box.createVerticalStrut(6));
        left.add(chevron);
        left.add(Box.createVerticalStrut(26));
        left.add(continueButton);
        left.add(Box.createVerticalGlue());

        JPanel art = new JPanel(new BorderLayout());
        art.setOpaque(false);
        art.add(new WelcomeLogo(), BorderLayout.CENTER);

        add(left, BorderLayout.WEST);
        add(art, BorderLayout.CENTER);
    }

    // paints the logo at the bottom right so the page edge crops its bottom
    private static class WelcomeLogo extends JComponent {
        private static final int WIDTH = 720;
        private static final int RIGHT_MARGIN = 6;
        private static final int BOTTOM_CROP = 170;

        private final Image image;

        WelcomeLogo() {
            image = Assets.image("images/Logo.png");
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (image == null || image.getWidth(null) <= 0 || image.getHeight(null) <= 0) {
                return;
            }

            // scale window whatever
            double scale = Math.min(getWidth() / 565.0, getHeight() / 620.0);
            int width = (int) Math.round(WIDTH * scale);
            int height = (int) Math.round(width * image.getHeight(null) / (double) image.getWidth(null));
            int x = getWidth() - width - (int) Math.round(RIGHT_MARGIN * scale);
            int y = getHeight() - height + (int) Math.round(BOTTOM_CROP * scale);
            g.drawImage(image, x, y, width, height, this);
        }
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
