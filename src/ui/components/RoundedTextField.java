package ui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedTextField extends JTextField {
    public RoundedTextField() {
        this(0);
    }

    public RoundedTextField(int columns) {
        super(columns);
        RoundedInputPainter.prepare(this, new Insets(7, 12, 7, 12));
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
}
