package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class RoundedInputPainter {
    static final int ARC = ui.Theme.CORNER_RADIUS;

    private RoundedInputPainter() {}

    static void prepare(JComponent component, Insets padding) {
        component.setOpaque(false);
        component.setBorder(new EmptyBorder(padding));
        component.setBackground(ui.Theme.SURFACE);
        component.setForeground(ui.Theme.TEXT);
    }

    static void paintBackground(JComponent component, Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(component.getBackground());
        g2.fillRoundRect(0, 0, component.getWidth(), component.getHeight(), ARC, ARC);
        g2.dispose();
    }

    static void paintBorder(JComponent component, Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ui.Theme.BORDER);
        g2.drawRoundRect(0, 0, component.getWidth() - 1, component.getHeight() - 1, ARC, ARC);
        g2.dispose();
    }
}
