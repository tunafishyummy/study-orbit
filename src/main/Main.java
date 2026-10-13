package main;

import javax.swing.SwingUtilities;
import ui.StudyOrbitApp;
import ui.Theme;

public class Main {

    public static void main(String[] args) {
        Theme.apply();
        SwingUtilities.invokeLater(() -> new StudyOrbitApp().setVisible(true));
    }
}
