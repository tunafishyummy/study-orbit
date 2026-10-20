package ui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedTextArea extends JTextArea {
    public RoundedTextArea() {
        this(0, 0);
    }

    public RoundedTextArea(int rows, int columns) {
        super(rows, columns);
        RoundedInputPainter.prepare(this, new Insets(9, 12, 9, 12));
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
