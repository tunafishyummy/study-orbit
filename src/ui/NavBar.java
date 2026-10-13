package ui;

import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

public class NavBar extends JPanel {

    // Same order as the sidebar in the design
    public static final String[] PAGES = {
        "Dashboard", "Courses", "Notes", "Flashcards", "Quizzes",
        "Schedule", "Search", "Progress", "Settings"
    };

    public NavBar(Consumer<String> onNavigate) {
        this(onNavigate, "Dashboard");
    }

    public NavBar(Consumer<String> onNavigate, String selectedPage) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(110, 0));
        setBackground(Theme.NAVBAR);

        JLabel logo = new JLabel();
        Image image = Assets.image("images/LogoNavbar.png");
        if (image != null) {
            int logoSize = 56;
            logo.setIcon(new Icon() {
                @Override
                public int getIconWidth() {
                    return logoSize;
                }

                @Override
                public int getIconHeight() {
                    return logoSize;
                }

                @Override
                public void paintIcon(Component component, Graphics graphics, int x, int y) {
                    Graphics2D g2 = (Graphics2D) graphics.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.drawImage(image, x, y, logoSize, logoSize, component);
                    g2.dispose();
                }
            });
        }
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("Study Orbit");
        name.setFont(Theme.BODY);
        name.setForeground(Theme.NAVBAR_TEXT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(10));
        add(logo);
        add(Box.createVerticalStrut(4));
        add(name);
        add(Box.createVerticalStrut(12));

        for (String page : PAGES) {
            final NavButton button = new NavButton(page);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(104, 35));
            button.addActionListener(e -> {
                select(page);
                onNavigate.accept(page);
            });
            add(button);
            add(Box.createVerticalStrut(5));
        }

        select(selectedPage);
    }

    private void select(String page) {
        for (Component component : getComponents()) {
            if (component instanceof NavButton) {
                NavButton button = (NavButton) component;
                button.setActive(button.page.equals(page));
            }
        }
    }

    // a nav item that paints its own chip so the bar stays dark in both themes
    private class NavButton extends JButton {
        private final String page;
        private boolean active;

        NavButton(String page) {
            super(page);
            this.page = page;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(Theme.BODY);
            setForeground(Theme.NAVBAR_TEXT);
            setMargin(new Insets(2, 4, 2, 4));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        void setActive(boolean value) {
            active = value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (active) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.NAVBAR_SELECTED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
