package ui.pages;

import ui.Theme;

import javax.swing.*;
import java.awt.*;

// Screen 7, quizzes
public class QuizzesPage extends JPanel {

    public QuizzesPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(Theme.header("Quizzes"), BorderLayout.NORTH);
    }
}
