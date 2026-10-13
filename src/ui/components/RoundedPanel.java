package ui.components;

import javax.swing.*;
import java.awt.*;

// a panel that paints its own rounded background
public class RoundedPanel extends JPanel {
    private final Color fill;
    private final int arc;

    public RoundedPanel(Color fill, int arc) {
        this.fill = fill;
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
