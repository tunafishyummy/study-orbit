package ui.components;

import java.awt.*;
import javax.swing.*;
import ui.Theme;

public class RoundedPasswordField extends JPasswordField {
    public RoundedPasswordField() {
        super();
        RoundedInputPainter.prepare(this, new Insets(7, 12, 7, 12));
        setCaretColor(Theme.TEXT);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        RoundedInputPainter.paintBackground(this, graphics);
        super.paintComponent(graphics);
    }

    @Override
    protected void paintBorder(Graphics graphics) {
        RoundedInputPainter.paintBorder(this, graphics);
    }
}
