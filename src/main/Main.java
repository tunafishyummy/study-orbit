package main;

import logic.ReminderService;

import javax.swing.SwingUtilities;
import ui.StudyOrbitApp;
import ui.Theme;

public class Main {

    public static void main(String[] args) {
        Theme.apply();
        new ReminderService();
        SwingUtilities.invokeLater(() -> new StudyOrbitApp().setVisible(true));
    }
}
