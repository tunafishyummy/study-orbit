package ui;

import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

public class NavBar extends JPanel {
    private static final int NAVBAR_WIDTH = 90;
    private static final int NAVBAR_EDGE_MARGIN = 40;
    private static final int TAB_VERTICAL_EXPANSION = 8;
    private NavButton activeButton;
    private JLabel navTitle;

    // Same order as the sidebar in the design
    public static final String[] PAGES = {
        "Dashboard", "Courses", "Notes", "Flashcards", "Quizzes",
        "Schedule", "Search", "Progress", "Settings"
    };

    public NavBar(Consumer<String> onNavigate) {
        this(onNavigate, "Dashboard", () -> {});
    }

    public NavBar(Consumer<String> onNavigate, String selectedPage) {
        this(onNavigate, selectedPage, () -> {});
    }

    public NavBar(Consumer<String> onNavigate, String selectedPage, Runnable onLogoClick) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(NAVBAR_WIDTH, 0));
        setBackground(Theme.NAVBAR);

        JLabel logo = new JLabel();
        Image image = Assets.image("images/LogoNavbar.png");
        if (image != null) {
            int logoSize = 40;
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
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2.drawImage(image, x, y, logoSize, logoSize, component);
                    g2.dispose();
                }
            });
        }
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onLogoClick.run();
            }
        });

        navTitle = new JLabel("Study Orbit");
        navTitle.setFont(Theme.BODY);
        navTitle.setForeground(Theme.NAVBAR_TEXT);
        navTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(NAVBAR_EDGE_MARGIN));
        add(logo);
        add(Box.createVerticalStrut(4));
        add(navTitle);
        add(verticalStrut(12));

        add(verticalGlue());
        for (String page : PAGES) {
            final NavButton button = new NavButton(page);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            button.setPreferredSize(new Dimension(NAVBAR_WIDTH, 54));
            button.addActionListener(e -> {
                select(page);
                onNavigate.accept(page);
            });
            add(button);
            if (!page.equals(PAGES[PAGES.length - 1])) {
                add(verticalGlue());
            }
        }
        add(verticalStrut(NAVBAR_EDGE_MARGIN));

        select(selectedPage);
    }

    private void select(String page) {
        activeButton = null;
        for (Component component : getComponents()) {
            if (component instanceof NavButton) {
                NavButton button = (NavButton) component;
                boolean active = button.page.equals(page);
                button.setActive(active);
                if (active) activeButton = button;
            }
        }
        repaint();
    }

    private static Component verticalGlue() {
        return new Box.Filler(
                new Dimension(0, 0),
                new Dimension(0, 0),
                new Dimension(0, Short.MAX_VALUE)) {
            @Override
            public boolean contains(int x, int y) {
                return false;
            }
        };
    }

    private static Component verticalStrut(int height) {
        Dimension size = new Dimension(0, height);
        return new Box.Filler(size, size, size) {
            @Override
            public boolean contains(int x, int y) {
                return false;
            }
        };
    }

    private int tabVerticalExpansion(NavButton button) {
        int index = java.util.Arrays.asList(PAGES).indexOf(button.page);
        if (index < 0) return 0;

        int available = Integer.MAX_VALUE;
        if (index > 0) {
            NavButton previous = navButton(PAGES[index - 1]);
            available = Math.min(available, button.getY() - previous.getY() - previous.getHeight());
        } else if (navTitle != null) {
            available = Math.min(available, button.getY() - navTitle.getY() - navTitle.getHeight());
        }
        if (index < PAGES.length - 1) {
            NavButton next = navButton(PAGES[index + 1]);
            available = Math.min(available, next.getY() - button.getY() - button.getHeight());
        } else {
            available = Math.min(available, getHeight() - button.getY() - button.getHeight());
        }
        return Math.max(0, Math.min(TAB_VERTICAL_EXPANSION, available / 2));
    }

    private NavButton navButton(String page) {
        for (Component component : getComponents()) {
            if (component instanceof NavButton && ((NavButton) component).page.equals(page)) {
                return (NavButton) component;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (activeButton == null) return;

        int expansion = tabVerticalExpansion(activeButton);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.BG);
        g2.fillRect(0, activeButton.getY() - expansion, getWidth(),
                activeButton.getHeight() + expansion * 2);
        g2.dispose();
    }

    private class NavButton extends JButton {
        private final String page;

        NavButton(String page) {
            super(page);
            this.page = page;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(Theme.font(Font.PLAIN, 10));
            setForeground(Theme.NAVBAR_TEXT);
            setMargin(new Insets(3, 4, 3, 4));
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.BOTTOM);
            setIconTextGap(2);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPageIcon(false);
        }

        void setActive(boolean value) {
            setPageIcon(value);
            repaint();
        }

        private void setPageIcon(boolean highlighted) {
            String normalPath = "icons/" + page + "Icon.png";
            String iconPath = highlighted
                    ? "icons/" + page + "IconHighlighted.png"
                    : normalPath;
            Image image = Assets.image(iconPath);
            if (image == null && highlighted) {
                image = Assets.image(normalPath);
            }
            setIcon(image == null
                    ? null
                    : new ImageIcon(image.getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        }

        @Override
        public boolean contains(int x, int y) {
            int expansion = tabVerticalExpansion(this);
            return x >= 0 && x < getWidth()
                    && y >= -expansion && y < getHeight() + expansion;
        }
    }
}
