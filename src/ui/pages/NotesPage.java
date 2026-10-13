package ui.pages;

import java.awt.*;
import javax.swing.*;

public class NotesPage extends JPanel {

    public NotesPage() {
        setLayout(new BorderLayout());

        add(new JLabel("Notes", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
