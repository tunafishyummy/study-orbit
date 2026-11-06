package ui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedToggleButton extends JToggleButton {
    public RoundedToggleButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        RoundedInputPainter.paintBackground(this, g);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (isBorderPainted()) {
            RoundedInputPainter.paintBorder(this, g);
        }
    }
}
