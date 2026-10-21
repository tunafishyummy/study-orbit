package data;

import java.io.IOException;
import java.nio.file.Path;

public final class AppData {

    private static DataStore store;
    private static NoteManager noteManager;
    private static SettingsManager settingsManager;
    private static ProfileManager profileManager;
    private static ActivityManager activityManager;
    private static ScheduleManager scheduleManager;

    private AppData() {}

    public static synchronized DataStore store() {
        if (store == null) {
            store = new DataStore(Path.of("data", "studyorbit.db"));
        }
        return store;
    }

    public static synchronized NoteManager get() {
        if (noteManager == null) {
            try {
                noteManager = new NoteManager(store());
            } catch (IOException exception) {
                throw new RuntimeException("Could not load the note manager", exception);
            }
        }
        return noteManager;
    }

    public static synchronized SettingsManager settings() {
        if (settingsManager == null) {
            try {
                settingsManager = new SettingsManager(store());
            } catch (IOException exception) {
                throw new RuntimeException("Could not load the settings", exception);
            }
        }
        return settingsManager;
    }

    public static synchronized ProfileManager profiles() {
        if (profileManager == null) {
            try {
                profileManager = new ProfileManager(store(), settings());
            } catch (IOException exception) {
                throw new RuntimeException("Could not load the profiles", exception);
            }
        }
        return profileManager;
    }

    public static synchronized ActivityManager activity() {
        if (activityManager == null) {
            try {
                activityManager = new ActivityManager(store(), settings());
            } catch (IOException exception) {
                throw new RuntimeException("Could not load the activity", exception);
            }
        }
        return activityManager;
    }

    public static synchronized ScheduleManager schedule() {
        if (scheduleManager == null) {
            try {
                scheduleManager = new ScheduleManager(store());
            } catch (IOException exception) {
                throw new RuntimeException("Could not load the schedule", exception);
            }
        }
        return scheduleManager;
    }
}
