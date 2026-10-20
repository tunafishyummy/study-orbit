package ui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedComboBox<E> extends JComboBox<E> {
    public RoundedComboBox() {
        super();
        prepare();
    }

    public RoundedComboBox(E[] items) {
        super(items);
        prepare();
    }

    private void prepare() {
        RoundedInputPainter.prepare(this, new Insets(5, 12, 5, 12));
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
        g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                RoundedInputPainter.ARC, RoundedInputPainter.ARC));
        super.paintChildren(g2);
        g2.dispose();
    }
}
