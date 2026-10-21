package main;

import data.AppData;
import data.SettingsManager;
import logic.ReminderService;

import javax.swing.SwingUtilities;
import ui.StudyOrbitApp;
import ui.Theme;

public class Main {

    public static void main(String[] args) {
        Theme.setLargeText(AppData.settings().getBoolean(SettingsManager.LARGE_TEXT, false));
        Theme.apply();
        new ReminderService();
        SwingUtilities.invokeLater(() -> new StudyOrbitApp().setVisible(true));
    }
}
