package data;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsManager {

    public static final String ACTIVE_PROFILE = "active_profile_id";
    public static final String LARGE_TEXT = "large_text";
    public static final String DEADLINE_REMINDERS = "deadline_reminders";
    public static final String DAILY_REVIEW_REMINDERS = "daily_review_reminders";
    public static final String API_KEY = "api_key";
    public static final String FIRST_RUN_DONE = "first_run_done";
    public static final String WEEKLY_GOAL = "weekly_goal";

    private final DataStore store;
    private final Map<String, String> values;

    public SettingsManager(DataStore store) throws IOException {
        this.store = store;
        this.values = new LinkedHashMap<String, String>(store.loadSettings());
    }

    public String get(String key, String fallback) {
        String value = values.get(key);
        if (value == null) {
            return fallback;
        }
        return value;
    }

    public boolean getBoolean(String key, boolean fallback) {
        String value = values.get(key);
        if (value == null) {
            return fallback;
        }
        if ("1".equals(value) || "true".equalsIgnoreCase(value)) {
            return true;
        }
        return false;
    }

    public int getInt(String key, int fallback) {
        String value = values.get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public void set(String key, String value) throws IOException {
        values.put(key, value);
        store.saveSetting(key, value);
    }

    public void setBoolean(String key, boolean value) throws IOException {
        String stored = "0";
        if (value) {
            stored = "1";
        }
        set(key, stored);
    }

    public void setInt(String key, int value) throws IOException {
        set(key, Integer.toString(value));
    }
}
