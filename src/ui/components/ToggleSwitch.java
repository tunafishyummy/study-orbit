package ui.components;

import ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// a small on and off switch
public class ToggleSwitch extends JComponent {
    private boolean on;
    private Runnable onChange;

    public ToggleSwitch(boolean on) {
        this.on = on;
        setPreferredSize(new Dimension(46, 24));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ToggleSwitch.this.on = !ToggleSwitch.this.on;
                repaint();
                if (onChange != null) {
                    onChange.run();
                }
            }
        });
    }

    public boolean isOn() {
        return on;
    }

    public void setOnChange(Runnable change) {
        this.onChange = change;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color track;
        if (on) {
            track = Theme.PRIMARY;
        } else {
            track = Theme.BORDER;
        }
        g2.setColor(track);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

        int knob = getHeight() - 6;
        int x;
        if (on) {
            x = getWidth() - knob - 3;
        } else {
            x = 3;
        }
        g2.setColor(Color.WHITE);
        g2.fillOval(x, 3, knob, knob);
    }
}
