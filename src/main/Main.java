package main;

import javax.swing.SwingUtilities;
import ui.StudyOrbitApp;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudyOrbitApp().setVisible(true));
    }
}
