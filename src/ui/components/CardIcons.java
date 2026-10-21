package ui.components;

import javax.swing.*;
import java.awt.*;

// small painted icons for the settings card headers
public final class CardIcons {
    private CardIcons() {}

    public static Icon person() {
        return new Glyph(Glyph.PERSON);
    }

    public static Icon palette() {
        return new Glyph(Glyph.PALETTE);
    }

    public static Icon bell() {
        return new Glyph(Glyph.BELL);
    }

    public static Icon spark() {
        return new Glyph(Glyph.SPARK);
    }

    private static class Glyph implements Icon {
        static final int PERSON = 0;
        static final int PALETTE = 1;
        static final int BELL = 2;
        static final int SPARK = 3;

        private final int kind;

        Glyph(int kind) {
            this.kind = kind;
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create(x, y, 16, 16);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);

            if (kind == PERSON) {
                g2.fillOval(5, 2, 7, 7);
                g2.fillArc(2, 10, 13, 13, 0, 180);
            } else if (kind == PALETTE) {
                g2.fillOval(0, 1, 15, 14);
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillOval(4, 4, 3, 3);
                g2.fillOval(9, 3, 3, 3);
                g2.fillOval(9, 9, 3, 3);
            } else if (kind == BELL) {
                g2.fillArc(3, 2, 10, 11, 0, 180);
                g2.fillRect(3, 7, 10, 3);
                g2.fillOval(6, 12, 4, 3);
            } else {
                g2.fillPolygon(new int[] { 8, 9, 15, 9, 8, 7, 1, 7 },
                        new int[] { 1, 6, 8, 10, 15, 10, 8, 6 }, 8);
            }
            g2.dispose();
        }
    }
}
