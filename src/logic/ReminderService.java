package logic;

import data.AppData;
import data.SettingsManager;
import model.Deadline;

import javax.swing.Timer;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReminderService {

    private final TrayIcon trayIcon;
    private final Set<String> notified = new HashSet<String>();
    private LocalDate reviewRemindedOn;

    public ReminderService() {
        TrayIcon icon = null;
        if (SystemTray.isSupported()) {
            Image image = ui.Assets.image("images/Logo.png");
            if (image != null) {
                icon = new TrayIcon(image, "Study Orbit");
                icon.setImageAutoSize(true);
                try {
                    SystemTray.getSystemTray().add(icon);
                } catch (AWTException exception) {
                    icon = null;
                }
            }
        }
        this.trayIcon = icon;

        Timer timer = new Timer(60000, e -> check());
        timer.setInitialDelay(5000);
        timer.start();
    }

    private void check() {
        try {
            if (AppData.settings().getBoolean(SettingsManager.DEADLINE_REMINDERS, true)) {
                checkDeadlines();
            }
            if (AppData.settings().getBoolean(SettingsManager.DAILY_REVIEW_REMINDERS, true)) {
                checkDailyReview();
            }
        } catch (Exception exception) {
            System.out.println("Reminder check failed: " + exception.getMessage());
        }
    }

    private void checkDeadlines() {
        List<Deadline> dueSoon = AppData.schedule().getDueSoon(2);
        for (int index = 0; index < dueSoon.size(); index++) {
            Deadline deadline = dueSoon.get(index);
            String key = deadline.getId() + "|" + deadline.getDueDate();
            if (notified.contains(key)) {
                continue;
            }
            notified.add(key);
            notify("Deadline reminder", deadline.getTitle() + " is due on " + deadline.getDueDate());
        }
    }

    private void checkDailyReview() {
        LocalDate today = LocalDate.now();
        if (reviewRemindedOn != null && reviewRemindedOn.equals(today)) {
            return;
        }
        if (AppData.activity().isActive(today) == false) {
            reviewRemindedOn = today;
            notify("Daily review", "You have not studied today. Review a few cards to keep your streak.");
        }
    }

    private void notify(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
        System.out.println(title + ": " + message);
    }
}
