package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import ui.Theme;

public class RoundedScrollPane extends JScrollPane {
    public RoundedScrollPane(Component view) {
        super(view);
        setOpaque(false);
        setBackground(Theme.SURFACE);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        getViewport().setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        RoundedInputPainter.paintBackground(this, g);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        RoundedInputPainter.paintBorder(this, g);
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                RoundedInputPainter.ARC, RoundedInputPainter.ARC));
        super.paintChildren(g2);
        g2.dispose();
    }
}
