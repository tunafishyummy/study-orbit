package data;

import model.StudyActivity;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActivityManager {

    private final DataStore store;
    private final SettingsManager settings;
    private final List<StudyActivity> activity = new ArrayList<StudyActivity>();

    public ActivityManager(DataStore store, SettingsManager settings) throws IOException {
        this.store = store;
        this.settings = settings;
        activity.addAll(store.loadActivity());
    }

    private String activeProfileId() {
        return settings.get(SettingsManager.ACTIVE_PROFILE, "");
    }

    private StudyActivity findOrCreate(LocalDate date) {
        String profileId = activeProfileId();
        for (int index = 0; index < activity.size(); index++) {
            StudyActivity entry = activity.get(index);
            if (entry.getDate().equals(date) && entry.getProfileId().equals(profileId)) {
                return entry;
            }
        }
        StudyActivity entry = new StudyActivity(date, profileId, 0, 0, 0);
        activity.add(entry);
        return entry;
    }

    public void recordNote() throws IOException {
        findOrCreate(LocalDate.now()).addNote();
        store.saveActivity(activity);
    }

    public void recordQuiz(int cardsReviewed) throws IOException {
        StudyActivity today = findOrCreate(LocalDate.now());
        today.addQuiz();
        today.addCardsReviewed(cardsReviewed);
        store.saveActivity(activity);
    }

    public boolean isActive(LocalDate date) {
        String profileId = activeProfileId();
        for (int index = 0; index < activity.size(); index++) {
            StudyActivity entry = activity.get(index);
            if (entry.getDate().equals(date) && entry.getProfileId().equals(profileId)) {
                return entry.isEmpty() == false;
            }
        }
        return false;
    }

    public StudyActivity getForDate(LocalDate date) {
        String profileId = activeProfileId();
        for (int index = 0; index < activity.size(); index++) {
            StudyActivity entry = activity.get(index);
            if (entry.getDate().equals(date) && entry.getProfileId().equals(profileId)) {
                return entry;
            }
        }
        return new StudyActivity(date, profileId, 0, 0, 0);
    }

    public int getCurrentStreak() {
        LocalDate cursor = LocalDate.now();
        if (isActive(cursor) == false) {
            cursor = cursor.minusDays(1);
        }
        int streak = 0;
        while (isActive(cursor)) {
            streak = streak + 1;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public int getLongestStreak() {
        java.util.TreeSet<LocalDate> dates = new java.util.TreeSet<LocalDate>();
        String profileId = activeProfileId();
        for (int index = 0; index < activity.size(); index++) {
            StudyActivity entry = activity.get(index);
            if (entry.getProfileId().equals(profileId) && entry.isEmpty() == false) {
                dates.add(entry.getDate());
            }
        }

        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate date : dates) {
            if (previous != null && date.equals(previous.plusDays(1))) {
                run = run + 1;
            } else {
                run = 1;
            }
            if (run > longest) {
                longest = run;
            }
            previous = date;
        }
        return longest;
    }

    public int getNotesThisWeek() {
        return countWeek(true, false, false);
    }

    public int getCardsThisWeek() {
        return countWeek(false, true, false);
    }

    public int getQuizzesThisWeek() {
        return countWeek(false, false, true);
    }

    public int getStudyDaysThisWeek() {
        LocalDate start = startOfWeek();
        int days = 0;
        LocalDate cursor = start;
        while (cursor.isAfter(LocalDate.now()) == false) {
            if (isActive(cursor)) {
                days = days + 1;
            }
            cursor = cursor.plusDays(1);
        }
        return days;
    }

    private int countWeek(boolean notes, boolean cards, boolean quizzes) {
        LocalDate start = startOfWeek();
        LocalDate end = LocalDate.now();
        int total = 0;
        String profileId = activeProfileId();
        for (int index = 0; index < activity.size(); index++) {
            StudyActivity entry = activity.get(index);
            if (entry.getProfileId().equals(profileId) == false) {
                continue;
            }
            if (entry.getDate().isBefore(start) || entry.getDate().isAfter(end)) {
                continue;
            }
            if (notes) {
                total = total + entry.getNotesMade();
            }
            if (cards) {
                total = total + entry.getCardsReviewed();
            }
            if (quizzes) {
                total = total + entry.getQuizzesTaken();
            }
        }
        return total;
    }

    private LocalDate startOfWeek() {
        LocalDate today = LocalDate.now();
        return today.minusDays(today.getDayOfWeek().getValue() - 1);
    }
}
