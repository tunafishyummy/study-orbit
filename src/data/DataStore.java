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
        String createDeadlines = "CREATE TABLE IF NOT EXISTS deadline (id TEXT PRIMARY KEY, course_code TEXT, title TEXT, due_date TEXT, done INTEGER, reminder_on INTEGER)";

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

}