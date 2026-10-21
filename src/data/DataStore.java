package data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Course;
import model.Deadline;
import model.Flashcard;
import model.Note;
import model.Profile;
import model.ScheduleClass;
import model.StudyActivity;

public class DataStore {
    private final String url;

    public DataStore(Path dbFilePath) {
        try {
            if (dbFilePath.getParent() != null) {
                Files.createDirectories(dbFilePath.getParent());
            }
            this.url = "jdbc:sqlite:" + dbFilePath.toAbsolutePath().toString();
            initDatabase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SQLite database", e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void initDatabase() {
        String createCourses = "CREATE TABLE IF NOT EXISTS courses (code TEXT PRIMARY KEY, title TEXT NOT NULL, color_hex TEXT)";
        String createNotes = "CREATE TABLE IF NOT EXISTS notes (id TEXT PRIMARY KEY, title TEXT NOT NULL, body TEXT, course_code TEXT, tags TEXT, created TEXT, edited TEXT)";
        String createFlashcards = "CREATE TABLE IF NOT EXISTS flashcards (id TEXT PRIMARY KEY, deck_title TEXT NOT NULL, front TEXT NOT NULL, back TEXT NOT NULL, tags TEXT, created TEXT, course_code TEXT)";
        String createProfiles = "CREATE TABLE IF NOT EXISTS profiles (id TEXT PRIMARY KEY, name TEXT NOT NULL, avatar_color TEXT, created TEXT)";
        String createSettings = "CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT)";
        String createActivity = "CREATE TABLE IF NOT EXISTS activity (log_date TEXT, profile_id TEXT, notes_made INTEGER, cards_reviewed INTEGER, quizzes_taken INTEGER, PRIMARY KEY (log_date, profile_id))";
        String createClasses = "CREATE TABLE IF NOT EXISTS schedule_class (id TEXT PRIMARY KEY, course_code TEXT, day TEXT, start_time TEXT, end_time TEXT, room TEXT)";
        String createDeadlines = "CREATE TABLE IF NOT EXISTS deadline (id TEXT PRIMARY KEY, course_code TEXT, title TEXT, due_date TEXT, due_time TEXT, done INTEGER, reminder_on INTEGER)";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createCourses);
            stmt.execute(createNotes);
            stmt.execute(createFlashcards);
            stmt.execute(createProfiles);
            stmt.execute(createSettings);
            stmt.execute(createActivity);
            stmt.execute(createClasses);
            stmt.execute(createDeadlines);
            // older databases were made before flashcards had a course, so add the column once
            try {
                stmt.execute("ALTER TABLE flashcards ADD COLUMN course_code TEXT");
            } catch (SQLException alreadyThere) {
                // column exists already: nothing to do
            }
            // older deadlines had no due time, so add the column once
            try {
                stmt.execute("ALTER TABLE deadline ADD COLUMN due_time TEXT");
            } catch (SQLException alreadyThere) {
                // column exists already: nothing to do
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void load(List<Course> courses, List<Note> notes, List<Flashcard> flashcards) throws IOException {
        courses.clear();
        notes.clear();
        flashcards.clear();

        try (Connection conn = connect()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM courses")) {
                while (rs.next()) {
                    courses.add(new Course(rs.getString("code"), rs.getString("title"), rs.getString("color_hex")));
                }
            }
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM notes")) {
                while (rs.next()) {
                    String tagText = rs.getString("tags");
                    List<String> tags = new ArrayList<>();
                    if (tagText != null && !tagText.isBlank()) for (String t : tagText.split(",")) tags.add(t.trim());
                    notes.add(new Note(rs.getString("id"), rs.getString("title"), rs.getString("body"), rs.getString("course_code"), tags, LocalDateTime.parse(rs.getString("created")), LocalDateTime.parse(rs.getString("edited"))));
                }
            }
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM flashcards")) {
                while (rs.next()) {
                    String tagText = rs.getString("tags");
                    List<String> tags = new ArrayList<>();
                    if (tagText != null && !tagText.isBlank()) for (String t : tagText.split(",")) tags.add(t.trim());
                    flashcards.add(new Flashcard(rs.getString("id"), rs.getString("deck_title"), rs.getString("course_code"), rs.getString("front"), rs.getString("back"), tags, LocalDateTime.parse(rs.getString("created"))));
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error loading from SQLite", e);
        }
    }

    public void save(List<Course> courses, List<Note> notes, List<Flashcard> flashcards) throws IOException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM courses");
                stmt.execute("DELETE FROM notes");
                stmt.execute("DELETE FROM flashcards");
            }

            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO courses (code, title, color_hex) VALUES (?, ?, ?)")) {
                for (Course c : courses) {
                    pstmt.setString(1, c.getCode());
                    pstmt.setString(2, c.getTitle());
                    pstmt.setString(3, c.getColorHex());
                    pstmt.executeUpdate();
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO notes (id, title, body, course_code, tags, created, edited) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                for (Note n : notes) {
                    pstmt.setString(1, n.getId());
                    pstmt.setString(2, n.getTitle());
                    pstmt.setString(3, n.getBody());
                    pstmt.setString(4, n.getCourseCode());
                    pstmt.setString(5, String.join(",", n.getTags()));
                    pstmt.setString(6, n.getCreated().toString());
                    pstmt.setString(7, n.getEdited().toString());
                    pstmt.executeUpdate();
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO flashcards (id, deck_title, front, back, tags, created, course_code) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                for (Flashcard f : flashcards) {
                    pstmt.setString(1, f.getId());
                    pstmt.setString(2, f.getDeckTitle());
                    pstmt.setString(3, f.getFront());
                    pstmt.setString(4, f.getBack());
                    pstmt.setString(5, String.join(",", f.getTags()));
                    pstmt.setString(6, f.getCreated().toString());
                    pstmt.setString(7, f.getCourseCode());
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new IOException("Error saving to SQLite", e);
        }
    }

    public List<Profile> loadProfiles() throws IOException {
        List<Profile> profiles = new ArrayList<Profile>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM profiles")) {
            while (rs.next()) {
                profiles.add(new Profile(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("avatar_color"),
                        LocalDateTime.parse(rs.getString("created"))));
            }
        } catch (SQLException e) {
            throw new IOException("Error loading profiles", e);
        }
        return profiles;
    }

    public void saveProfiles(List<Profile> profiles) throws IOException {
        try (Connection conn = connect()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM profiles");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO profiles (id, name, avatar_color, created) VALUES (?, ?, ?, ?)")) {
                for (int index = 0; index < profiles.size(); index++) {
                    Profile profile = profiles.get(index);
                    pstmt.setString(1, profile.getId());
                    pstmt.setString(2, profile.getName());
                    pstmt.setString(3, profile.getAvatarColor());
                    pstmt.setString(4, profile.getCreated().toString());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error saving profiles", e);
        }
    }

    public Map<String, String> loadSettings() throws IOException {
        Map<String, String> settings = new LinkedHashMap<String, String>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT key, value FROM settings")) {
            while (rs.next()) {
                settings.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            throw new IOException("Error loading settings", e);
        }
        return settings;
    }

    public void saveSetting(String key, String value) throws IOException {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)")) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new IOException("Error saving a setting", e);
        }
    }

    public List<StudyActivity> loadActivity() throws IOException {
        List<StudyActivity> activity = new ArrayList<StudyActivity>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM activity")) {
            while (rs.next()) {
                activity.add(new StudyActivity(
                        LocalDate.parse(rs.getString("log_date")),
                        rs.getString("profile_id"),
                        rs.getInt("notes_made"),
                        rs.getInt("cards_reviewed"),
                        rs.getInt("quizzes_taken")));
            }
        } catch (SQLException e) {
            throw new IOException("Error loading activity", e);
        }
        return activity;
    }

    public void saveActivity(List<StudyActivity> activity) throws IOException {
        try (Connection conn = connect()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM activity");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO activity (log_date, profile_id, notes_made, cards_reviewed, quizzes_taken) VALUES (?, ?, ?, ?, ?)")) {
                for (int index = 0; index < activity.size(); index++) {
                    StudyActivity entry = activity.get(index);
                    pstmt.setString(1, entry.getDate().toString());
                    pstmt.setString(2, entry.getProfileId());
                    pstmt.setInt(3, entry.getNotesMade());
                    pstmt.setInt(4, entry.getCardsReviewed());
                    pstmt.setInt(5, entry.getQuizzesTaken());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error saving activity", e);
        }
    }

    public List<ScheduleClass> loadClasses() throws IOException {
        List<ScheduleClass> classes = new ArrayList<ScheduleClass>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM schedule_class")) {
            while (rs.next()) {
                classes.add(new ScheduleClass(
                        rs.getString("id"),
                        rs.getString("course_code"),
                        rs.getString("day"),
                        LocalTime.parse(rs.getString("start_time")),
                        LocalTime.parse(rs.getString("end_time")),
                        rs.getString("room")));
            }
        } catch (SQLException e) {
            throw new IOException("Error loading classes", e);
        }
        return classes;
    }

    public void saveClasses(List<ScheduleClass> classes) throws IOException {
        try (Connection conn = connect()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM schedule_class");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO schedule_class (id, course_code, day, start_time, end_time, room) VALUES (?, ?, ?, ?, ?, ?)")) {
                for (int index = 0; index < classes.size(); index++) {
                    ScheduleClass item = classes.get(index);
                    pstmt.setString(1, item.getId());
                    pstmt.setString(2, item.getCourseCode());
                    pstmt.setString(3, item.getDay());
                    pstmt.setString(4, item.getStart().toString());
                    pstmt.setString(5, item.getEnd().toString());
                    pstmt.setString(6, item.getRoom());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error saving classes", e);
        }
    }

    public List<Deadline> loadDeadlines() throws IOException {
        List<Deadline> deadlines = new ArrayList<Deadline>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM deadline")) {
            while (rs.next()) {
                boolean done = false;
                if (rs.getInt("done") == 1) {
                    done = true;
                }
                boolean reminderOn = false;
                if (rs.getInt("reminder_on") == 1) {
                    reminderOn = true;
                }
                String timeText = rs.getString("due_time");
                LocalTime dueTime = LocalTime.of(23, 59);
                if (timeText != null && timeText.isEmpty() == false) {
                    dueTime = LocalTime.parse(timeText);
                }
                deadlines.add(new Deadline(
                        rs.getString("id"),
                        rs.getString("course_code"),
                        rs.getString("title"),
                        LocalDate.parse(rs.getString("due_date")),
                        dueTime,
                        done,
                        reminderOn));
            }
        } catch (SQLException e) {
            throw new IOException("Error loading deadlines", e);
        }
        return deadlines;
    }

    public void saveDeadlines(List<Deadline> deadlines) throws IOException {
        try (Connection conn = connect()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM deadline");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO deadline (id, course_code, title, due_date, due_time, done, reminder_on) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                for (int index = 0; index < deadlines.size(); index++) {
                    Deadline item = deadlines.get(index);
                    int done = 0;
                    if (item.isDone()) {
                        done = 1;
                    }
                    int reminderOn = 0;
                    if (item.isReminderOn()) {
                        reminderOn = 1;
                    }
                    pstmt.setString(1, item.getId());
                    pstmt.setString(2, item.getCourseCode());
                    pstmt.setString(3, item.getTitle());
                    pstmt.setString(4, item.getDueDate().toString());
                    pstmt.setString(5, item.getDueTime().toString());
                    pstmt.setInt(6, done);
                    pstmt.setInt(7, reminderOn);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error saving deadlines", e);
        }
    }

}