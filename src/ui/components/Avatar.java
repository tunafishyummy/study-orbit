package ui.components;

import ui.Theme;

import javax.swing.*;
import java.awt.*;

// a simple profile avatar with a drawn person shape
public class Avatar extends JComponent {

    private final Color color;

    public Avatar() {
        this(Theme.PRIMARY);
    }

    public Avatar(Color color) {
        this.color = color;
        setPreferredSize(new Dimension(96, 96));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight());
        g2.setColor(Theme.BG);
        g2.fillOval(0, 0, size, size);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(1, 1, size - 2, size - 2);

        int head = size / 4;
        g2.drawOval(size / 2 - head / 2, size / 5, head, head);
        g2.drawArc(size / 2 - head, size / 5 + head + 2, head * 2, head, 0, 180);
        g2.dispose();
    }
}
