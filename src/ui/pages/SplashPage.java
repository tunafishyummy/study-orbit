package ui.pages;

import ui.Assets;
import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 1, the welcome page
public class SplashPage extends JPanel {
    private static final int TITLE_LINE_SPACING = -30;

    public SplashPage(Runnable onContinue) {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        JLabel study = new JLabel("Study");
        JLabel orbit = new JLabel("Orbit");
        for (JLabel line : new JLabel[] {study, orbit}) {
            line.setFont(Theme.font(Font.PLAIN, 90));
            line.setForeground(Theme.TEXT);
            line.setAlignmentX(Component.CENTER_ALIGNMENT);
            title.add(line);
            if (line == study) {
                title.add(Box.createVerticalStrut(TITLE_LINE_SPACING));
            }
        }

        JLabel motto = new JLabel("An All-Around Learning Tool Built for Students");
        motto.setFont(Theme.font(Font.PLAIN, 16));
        motto.setForeground(Theme.MUTED);

        JLabel profile = new JLabel("No selected profile");
        profile.setFont(Theme.font(Font.BOLD, 18));
        profile.setForeground(Theme.TEXT);
        Chevron chevron = new Chevron();
        JButton continueButton = new JButton("Continue");
        continueButton.setFont(Theme.font(Font.PLAIN, 20));
        continueButton.setForeground(new Color(0x606060));
        continueButton.setBackground(Theme.BG);
        continueButton.setContentAreaFilled(false);
        continueButton.setBorderPainted(false);
        continueButton.setFocusPainted(false);
        continueButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        continueButton.addActionListener(e -> onContinue.run());

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(centered(profile));
        controls.add(centered(chevron));
        controls.add(Box.createVerticalStrut(14));
        controls.add(centered(continueButton));

        left.add(Box.createVerticalGlue());
        left.add(centered(title));
        left.add(Box.createVerticalStrut(12));
        left.add(centered(motto));
        left.add(Box.createVerticalStrut(58));
        left.add(controls);
        left.add(Box.createVerticalGlue());

        JPanel art = new JPanel(new BorderLayout());
        art.setOpaque(false);
        art.add(new WelcomeLogo(), BorderLayout.CENTER);

        add(left, BorderLayout.WEST);
        add(art, BorderLayout.CENTER);
    }

    private static JPanel centered(Component component) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row.setOpaque(false);
        row.add(component);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        return row;
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
            int x = Math.max(0, getWidth() - width - (int) Math.round(RIGHT_MARGIN * scale));
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
