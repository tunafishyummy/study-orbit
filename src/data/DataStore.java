package data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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

    private LocalDateTime readTime(String value) {
        if (value == null || value.isEmpty()) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(value);
    }

    private void initDatabase() {
        String createCourses = "CREATE TABLE IF NOT EXISTS courses (profile_id TEXT NOT NULL, code TEXT NOT NULL, title TEXT NOT NULL, color_hex TEXT, PRIMARY KEY (profile_id, code))";
        String createNotes = "CREATE TABLE IF NOT EXISTS notes (id TEXT PRIMARY KEY, profile_id TEXT, title TEXT NOT NULL, body TEXT, course_code TEXT, tags TEXT, created TEXT, edited TEXT)";
        String createFlashcards = "CREATE TABLE IF NOT EXISTS flashcards (id TEXT PRIMARY KEY, profile_id TEXT, deck_title TEXT NOT NULL, front TEXT NOT NULL, back TEXT NOT NULL, tags TEXT, created TEXT, course_code TEXT)";
        String createProfiles = "CREATE TABLE IF NOT EXISTS profiles (id TEXT PRIMARY KEY, name TEXT NOT NULL, avatar_color TEXT, created TEXT)";
        String createSettings = "CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT)";
        String createActivity = "CREATE TABLE IF NOT EXISTS activity (log_date TEXT, profile_id TEXT, notes_made INTEGER, cards_reviewed INTEGER, quizzes_taken INTEGER, PRIMARY KEY (log_date, profile_id))";
        String createClasses = "CREATE TABLE IF NOT EXISTS schedule_class (id TEXT PRIMARY KEY, profile_id TEXT, course_code TEXT, day TEXT, start_time TEXT, end_time TEXT, room TEXT)";
        String createDeadlines = "CREATE TABLE IF NOT EXISTS deadline (id TEXT PRIMARY KEY, profile_id TEXT, course_code TEXT, title TEXT, due_date TEXT, due_time TEXT, done INTEGER, reminder_on INTEGER)";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createCourses);
            stmt.execute(createNotes);
            stmt.execute(createFlashcards);
            stmt.execute(createProfiles);
            stmt.execute(createSettings);
            stmt.execute(createActivity);
            stmt.execute(createClasses);
            stmt.execute(createDeadlines);

            // older databases were shared by every profile, so give their rows to the first profile
            String owner = firstProfileId(conn);
            if (hasColumn(conn, "courses", "profile_id") == false) {
                stmt.execute("ALTER TABLE courses RENAME TO courses_old");
                stmt.execute("CREATE TABLE courses (profile_id TEXT NOT NULL, code TEXT NOT NULL, title TEXT NOT NULL, color_hex TEXT, PRIMARY KEY (profile_id, code))");
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO courses (profile_id, code, title, color_hex) SELECT ?, code, title, color_hex FROM courses_old")) {
                    insert.setString(1, owner);
                    insert.executeUpdate();
                }
                stmt.execute("DROP TABLE courses_old");
            }
            addProfileColumn(conn, "notes", owner);
            addProfileColumn(conn, "flashcards", owner);
            addProfileColumn(conn, "schedule_class", owner);
            addProfileColumn(conn, "deadline", owner);
            addColumn(conn, "flashcards", "course_code", "TEXT");
            addColumn(conn, "deadline", "due_time", "TEXT");
        } catch (SQLException e) {
            throw new RuntimeException("Could not prepare the database tables", e);
        }
    }

    private boolean hasColumn(Connection conn, String table, String column) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addColumn(Connection conn, String table, String column, String type) throws SQLException {
        if (hasColumn(conn, table, column)) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        }
    }

    private void addProfileColumn(Connection conn, String table, String owner) throws SQLException {
        if (hasColumn(conn, table, "profile_id")) {
            return;
        }
        addColumn(conn, table, "profile_id", "TEXT");
        try (PreparedStatement update = conn.prepareStatement(
                "UPDATE " + table + " SET profile_id = ? WHERE profile_id IS NULL OR profile_id = ''")) {
            update.setString(1, owner);
            update.executeUpdate();
        }
    }

    private String firstProfileId(Connection conn) throws SQLException {
        String id = null;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM profiles ORDER BY created LIMIT 1")) {
            if (rs.next()) {
                id = rs.getString(1);
            }
        }
        if (id == null || id.isBlank()) {
            id = activeProfileId(conn);
        }
        return id == null ? "" : id;
    }

    public void adoptOrphanData(String profileId) throws IOException {
        if (profileId == null || profileId.isBlank()) {
            return;
        }
        String[] tables = { "courses", "notes", "flashcards", "schedule_class", "deadline", "activity" };
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            for (String table : tables) {
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE " + table + " SET profile_id = ? WHERE profile_id IS NULL OR profile_id = ''")) {
                    update.setString(1, profileId);
                    update.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new IOException("Error moving the setup data to the profile", e);
        }
    }

    public void deleteProfileData(String profileId) throws IOException {
        if (profileId == null || profileId.isBlank()) {
            return;
        }
        String[] tables = { "courses", "notes", "flashcards", "schedule_class", "deadline", "activity" };
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            for (String table : tables) {
                try (PreparedStatement delete = conn.prepareStatement(
                        "DELETE FROM " + table + " WHERE profile_id = ?")) {
                    delete.setString(1, profileId);
                    delete.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new IOException("Error removing the profile data", e);
        }
    }

    public void load(String profileId, List<Course> courses, List<Note> notes, List<Flashcard> flashcards)
            throws IOException {
        courses.clear();
        notes.clear();
        flashcards.clear();

        try (Connection conn = connect()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM courses WHERE profile_id = ?")) {
                stmt.setString(1, profileId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        courses.add(new Course(rs.getString("code"), rs.getString("title"),
                                rs.getString("color_hex")));
                    }
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM notes WHERE profile_id = ?")) {
                stmt.setString(1, profileId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        notes.add(readNote(rs));
                    }
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM flashcards WHERE profile_id = ?")) {
                stmt.setString(1, profileId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        flashcards.add(readFlashcard(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error loading from SQLite", e);
        }
    }

    private Note readNote(ResultSet rs) throws SQLException {
        String tagText = rs.getString("tags");
        List<String> tags = new ArrayList<String>();
        if (tagText != null && tagText.isBlank() == false) {
            for (String tag : tagText.split(",")) {
                tags.add(tag.trim());
            }
        }
        return new Note(rs.getString("id"), rs.getString("title"), rs.getString("body"),
                rs.getString("course_code"), tags, readTime(rs.getString("created")),
                readTime(rs.getString("edited")));
    }

    private Flashcard readFlashcard(ResultSet rs) throws SQLException {
        String tagText = rs.getString("tags");
        List<String> tags = new ArrayList<String>();
        if (tagText != null && tagText.isBlank() == false) {
            for (String tag : tagText.split(",")) {
                tags.add(tag.trim());
            }
        }
        return new Flashcard(rs.getString("id"), rs.getString("deck_title"), rs.getString("course_code"),
                rs.getString("front"), rs.getString("back"), tags, readTime(rs.getString("created")));
    }

    public void save(String profileId, List<Course> courses, List<Note> notes, List<Flashcard> flashcards)
            throws IOException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM courses WHERE profile_id = ?")) {
                stmt.setString(1, profileId);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM notes WHERE profile_id = ?")) {
                stmt.setString(1, profileId);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM flashcards WHERE profile_id = ?")) {
                stmt.setString(1, profileId);
                stmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO courses (profile_id, code, title, color_hex) VALUES (?, ?, ?, ?)")) {
                for (Course c : courses) {
                    pstmt.setString(1, profileId);
                    pstmt.setString(2, c.getCode());
                    pstmt.setString(3, c.getTitle());
                    pstmt.setString(4, c.getColorHex());
                    pstmt.executeUpdate();
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO notes (id, profile_id, title, body, course_code, tags, created, edited) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (Note n : notes) {
                    pstmt.setString(1, n.getId());
                    pstmt.setString(2, profileId);
                    pstmt.setString(3, n.getTitle());
                    pstmt.setString(4, n.getBody());
                    pstmt.setString(5, n.getCourseCode());
                    pstmt.setString(6, String.join(",", n.getTags()));
                    pstmt.setString(7, n.getCreated().toString());
                    pstmt.setString(8, n.getEdited().toString());
                    pstmt.executeUpdate();
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO flashcards (id, profile_id, deck_title, front, back, tags, created, course_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (Flashcard f : flashcards) {
                    pstmt.setString(1, f.getId());
                    pstmt.setString(2, profileId);
                    pstmt.setString(3, f.getDeckTitle());
                    pstmt.setString(4, f.getFront());
                    pstmt.setString(5, f.getBack());
                    pstmt.setString(6, String.join(",", f.getTags()));
                    pstmt.setString(7, f.getCreated().toString());
                    pstmt.setString(8, f.getCourseCode());
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
                        readTime(rs.getString("created"))));
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

    public List<ScheduleClass> loadClasses(String profileId) throws IOException {
        List<ScheduleClass> classes = new ArrayList<ScheduleClass>();
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM schedule_class WHERE profile_id = ?")) {
            stmt.setString(1, profileId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    classes.add(new ScheduleClass(
                            rs.getString("id"),
                            rs.getString("course_code"),
                            rs.getString("day"),
                            LocalTime.parse(rs.getString("start_time")),
                            LocalTime.parse(rs.getString("end_time")),
                            rs.getString("room")));
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error loading classes", e);
        }
        return classes;
    }

    public void saveClasses(String profileId, List<ScheduleClass> classes) throws IOException {
        try (Connection conn = connect()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM schedule_class WHERE profile_id = ?")) {
                stmt.setString(1, profileId);
                stmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO schedule_class (id, profile_id, course_code, day, start_time, end_time, room) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                for (int index = 0; index < classes.size(); index++) {
                    ScheduleClass item = classes.get(index);
                    pstmt.setString(1, item.getId());
                    pstmt.setString(2, profileId);
                    pstmt.setString(3, item.getCourseCode());
                    pstmt.setString(4, item.getDay());
                    pstmt.setString(5, item.getStart().toString());
                    pstmt.setString(6, item.getEnd().toString());
                    pstmt.setString(7, item.getRoom());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error saving classes", e);
        }
    }

    public List<Deadline> loadDeadlines(String profileId) throws IOException {
        List<Deadline> deadlines = new ArrayList<Deadline>();
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM deadline WHERE profile_id = ?")) {
            stmt.setString(1, profileId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    boolean done = rs.getInt("done") == 1;
                    boolean reminderOn = rs.getInt("reminder_on") == 1;
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
            }
        } catch (SQLException e) {
            throw new IOException("Error loading deadlines", e);
        }
        return deadlines;
    }

    public void saveDeadlines(String profileId, List<Deadline> deadlines) throws IOException {
        try (Connection conn = connect()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM deadline WHERE profile_id = ?")) {
                stmt.setString(1, profileId);
                stmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO deadline (id, profile_id, course_code, title, due_date, due_time, done, reminder_on) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (int index = 0; index < deadlines.size(); index++) {
                    Deadline item = deadlines.get(index);
                    pstmt.setString(1, item.getId());
                    pstmt.setString(2, profileId);
                    pstmt.setString(3, item.getCourseCode());
                    pstmt.setString(4, item.getTitle());
                    pstmt.setString(5, item.getDueDate().toString());
                    pstmt.setString(6, item.getDueTime().toString());
                    pstmt.setInt(7, item.isDone() ? 1 : 0);
                    pstmt.setInt(8, item.isReminderOn() ? 1 : 0);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error saving deadlines", e);
        }
    }

    public JsonObject exportAll() throws IOException {
        JsonObject root = new JsonObject();
        try (Connection conn = connect()) {
            root.add("profiles", readRows(conn,
                    "SELECT id, name, avatar_color, created FROM profiles"));
            root.add("courses", readRows(conn,
                    "SELECT profile_id, code, title, color_hex FROM courses"));
            root.add("notes", readRows(conn,
                    "SELECT profile_id, id, title, body, course_code, tags, created, edited FROM notes"));
            root.add("flashcards", readRows(conn,
                    "SELECT profile_id, id, deck_title, front, back, tags, created, course_code FROM flashcards"));
            root.add("activity", readRows(conn,
                    "SELECT log_date, profile_id, notes_made, cards_reviewed, quizzes_taken FROM activity"));
            root.add("classes", readRows(conn,
                    "SELECT profile_id, id, course_code, day, start_time, end_time, room FROM schedule_class"));
            root.add("deadlines", readRows(conn,
                    "SELECT profile_id, id, course_code, title, due_date, due_time, done, reminder_on FROM deadline"));
            root.add("settings", readSettings(conn));
        } catch (SQLException e) {
            throw new IOException("Error reading the data", e);
        }
        return root;
    }

    private JsonArray readRows(Connection conn, String sql) throws SQLException {
        JsonArray array = new JsonArray();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int column = 1; column <= count; column++) {
                    row.addProperty(meta.getColumnLabel(column), rs.getString(column));
                }
                array.add(row);
            }
        }
        return array;
    }

    private JsonObject readSettings(Connection conn) throws SQLException {
        JsonObject settings = new JsonObject();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT key, value FROM settings")) {
            while (rs.next()) {
                settings.addProperty(rs.getString("key"), rs.getString("value"));
            }
        }
        return settings;
    }

    public void importAll(JsonObject root) throws IOException {
        if (root == null || root.has("profiles") == false || root.get("profiles").isJsonArray() == false) {
            throw new IOException("This file is not a Study Orbit backup");
        }
        String fallback = backupProfileId(root);
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("DELETE FROM courses");
                    stmt.execute("DELETE FROM notes");
                    stmt.execute("DELETE FROM flashcards");
                    stmt.execute("DELETE FROM profiles");
                    stmt.execute("DELETE FROM settings");
                    stmt.execute("DELETE FROM activity");
                    stmt.execute("DELETE FROM schedule_class");
                    stmt.execute("DELETE FROM deadline");
                }
                insertProfiles(conn, array(root, "profiles"));
                insertSettings(conn, root);
                insertCourses(conn, array(root, "courses"), fallback);
                insertNotes(conn, array(root, "notes"), fallback);
                insertFlashcards(conn, array(root, "flashcards"), fallback);
                insertActivity(conn, array(root, "activity"), fallback);
                insertClasses(conn, array(root, "classes"), fallback);
                insertDeadlines(conn, array(root, "deadlines"), fallback);
                conn.commit();
            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            }
        } catch (SQLException e) {
            throw new IOException("Error writing the backup to the database", e);
        }
    }

    private void insertProfiles(Connection conn, JsonArray rows) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO profiles (id, name, avatar_color, created) VALUES (?, ?, ?, ?)")) {
            for (int index = 0; index < rows.size(); index++) {
                JsonObject row = rows.get(index).getAsJsonObject();
                pstmt.setString(1, text(row, "id", ""));
                pstmt.setString(2, text(row, "name", ""));
                pstmt.setString(3, text(row, "avatar_color", "#6151D9"));
                pstmt.setString(4, text(row, "created", LocalDateTime.now().toString()));
                pstmt.executeUpdate();
            }
        }
    }

    private void insertSettings(Connection conn, JsonObject root) throws SQLException {
        if (root.has("settings") == false) {
            return;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)")) {
            JsonElement settings = root.get("settings");
            if (settings.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : settings.getAsJsonObject().entrySet()) {
                    pstmt.setString(1, entry.getKey());
                    pstmt.setString(2, entry.getValue().isJsonNull() ? "" : entry.getValue().getAsString());
                    pstmt.executeUpdate();
                }
            } else if (settings.isJsonArray()) {
                JsonArray rows = settings.getAsJsonArray();
                for (int index = 0; index < rows.size(); index++) {
                    JsonObject row = rows.get(index).getAsJsonObject();
                    pstmt.setString(1, text(row, "key", ""));
                    pstmt.setString(2, text(row, "value", ""));
                    pstmt.executeUpdate();
                }
            }
        }
    }

    private void insertCourses(Connection conn, JsonArray rows, String fallback) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO courses (profile_id, code, title, color_hex) VALUES (?, ?, ?, ?)")) {
            for (int index = 0; index < rows.size(); index++) {
                JsonObject row = rows.get(index).getAsJsonObject();
                pstmt.setString(1, text(row, "profile_id", fallback));
                pstmt.setString(2, text(row, "code", ""));
                pstmt.setString(3, text(row, "title", ""));
                pstmt.setString(4, text(row, "color_hex", ""));
                pstmt.executeUpdate();
            }
        }
    }

    private void insertNotes(Connection conn, JsonArray rows, String fallback) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO notes (id, profile_id, title, body, course_code, tags, created, edited) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (int index = 0; index < rows.size(); index++) {
                JsonObject row = rows.get(index).getAsJsonObject();
                String created = text(row, "created", LocalDateTime.now().toString());
                pstmt.setString(1, text(row, "id", java.util.UUID.randomUUID().toString()));
                pstmt.setString(2, text(row, "profile_id", fallback));
                pstmt.setString(3, text(row, "title", ""));
                pstmt.setString(4, text(row, "body", ""));
                pstmt.setString(5, text(row, "course_code", ""));
                pstmt.setString(6, text(row, "tags", ""));
                pstmt.setString(7, created);
                pstmt.setString(8, text(row, "edited", created));
                pstmt.executeUpdate();
            }
        }
    }

    private void insertFlashcards(Connection conn, JsonArray rows, String fallback) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO flashcards (id, profile_id, deck_title, front, back, tags, created, course_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (int index = 0; index < rows.size(); index++) {
                JsonObject row = rows.get(index).getAsJsonObject();
                pstmt.setString(1, text(row, "id", java.util.UUID.randomUUID().toString()));
                pstmt.setString(2, text(row, "profile_id", fallback));
                pstmt.setString(3, text(row, "deck_title", "General"));
                pstmt.setString(4, text(row, "front", ""));
                pstmt.setString(5, text(row, "back", ""));
                pstmt.setString(6, text(row, "tags", ""));
                pstmt.setString(7, text(row, "created", LocalDateTime.now().toString()));
                pstmt.setString(8, text(row, "course_code", ""));
                pstmt.executeUpdate();
            }
        }
    }

    private void insertActivity(Connection conn, JsonArray rows, String fallback) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO activity (log_date, profile_id, notes_made, cards_reviewed, quizzes_taken) VALUES (?, ?, ?, ?, ?)")) {
            for (int index = 0; index < rows.size(); index++) {
                JsonObject row = rows.get(index).getAsJsonObject();
                pstmt.setString(1, text(row, "log_date", LocalDate.now().toString()));
                pstmt.setString(2, text(row, "profile_id", fallback));
                pstmt.setInt(3, number(row, "notes_made", 0));
                pstmt.setInt(4, number(row, "cards_reviewed", 0));
                pstmt.setInt(5, number(row, "quizzes_taken", 0));
                pstmt.executeUpdate();
            }
        }
    }

    private void insertClasses(Connection conn, JsonArray rows, String fallback) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO schedule_class (id, profile_id, course_code, day, start_time, end_time, room) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            for (int index = 0; index < rows.size(); index++) {
                JsonObject row = rows.get(index).getAsJsonObject();
                pstmt.setString(1, text(row, "id", java.util.UUID.randomUUID().toString()));
                pstmt.setString(2, text(row, "profile_id", fallback));
                pstmt.setString(3, text(row, "course_code", ""));
                pstmt.setString(4, text(row, "day", "Mon"));
                pstmt.setString(5, text(row, "start_time", "08:00"));
                pstmt.setString(6, text(row, "end_time", "09:00"));
                pstmt.setString(7, text(row, "room", ""));
                pstmt.executeUpdate();
            }
        }
    }

    private void insertDeadlines(Connection conn, JsonArray rows, String fallback) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO deadline (id, profile_id, course_code, title, due_date, due_time, done, reminder_on) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (int index = 0; index < rows.size(); index++) {
                JsonObject row = rows.get(index).getAsJsonObject();
                pstmt.setString(1, text(row, "id", java.util.UUID.randomUUID().toString()));
                pstmt.setString(2, text(row, "profile_id", fallback));
                pstmt.setString(3, text(row, "course_code", ""));
                pstmt.setString(4, text(row, "title", ""));
                pstmt.setString(5, text(row, "due_date", LocalDate.now().toString()));
                pstmt.setString(6, text(row, "due_time", "23:59"));
                pstmt.setInt(7, number(row, "done", 0));
                pstmt.setInt(8, number(row, "reminder_on", 1));
                pstmt.executeUpdate();
            }
        }
    }

    private JsonArray array(JsonObject root, String key) {
        if (root.has(key) == false || root.get(key).isJsonArray() == false) {
            return new JsonArray();
        }
        return root.getAsJsonArray(key);
    }

    private String text(JsonObject row, String key, String fallback) {
        if (row.has(key) == false || row.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return row.get(key).getAsString();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private int number(JsonObject row, String key, int fallback) {
        if (row.has(key) == false || row.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            JsonElement element = row.get(key);
            if (element.getAsJsonPrimitive().isBoolean()) {
                return element.getAsBoolean() ? 1 : 0;
            }
            return element.getAsInt();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private String backupProfileId(JsonObject root) {
        JsonElement settings = root.get("settings");
        if (settings != null && settings.isJsonObject()) {
            JsonElement active = settings.getAsJsonObject().get("active_profile_id");
            if (active != null && active.isJsonNull() == false && active.getAsString().isBlank() == false) {
                return active.getAsString();
            }
        }
        JsonArray profiles = array(root, "profiles");
        if (profiles.size() > 0) {
            String id = text(profiles.get(0).getAsJsonObject(), "id", "");
            if (id.isBlank() == false) {
                return id;
            }
        }
        return "";
    }

    private String activeProfileId(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT value FROM settings WHERE key = 'active_profile_id'")) {
            if (rs.next() && rs.getString(1) != null) {
                return rs.getString(1);
            }
        }
        return "";
    }
}
