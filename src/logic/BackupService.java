package logic;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import data.AppData;
import model.Course;
import model.Deadline;
import model.Flashcard;
import model.Note;
import model.Profile;
import model.ScheduleClass;
import model.StudyActivity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class BackupService {

    public void export(File file) throws IOException {
        JsonObject root = new JsonObject();
        root.add("profiles", profiles());
        root.add("courses", courses());
        root.add("notes", notes());
        root.add("flashcards", flashcards());
        root.add("activity", activity());
        root.add("classes", classes());
        root.add("deadlines", deadlines());
        root.add("settings", settings());

        String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
        Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
    }

    private JsonArray profiles() {
        JsonArray array = new JsonArray();
        List<Profile> profiles = AppData.profiles().getProfiles();
        for (int index = 0; index < profiles.size(); index++) {
            Profile profile = profiles.get(index);
            JsonObject item = new JsonObject();
            item.addProperty("id", profile.getId());
            item.addProperty("name", profile.getName());
            item.addProperty("avatar_color", profile.getAvatarColor());
            item.addProperty("created", profile.getCreated().toString());
            array.add(item);
        }
        return array;
    }

    private JsonArray courses() {
        JsonArray array = new JsonArray();
        List<Course> courses = AppData.get().getCourses();
        for (int index = 0; index < courses.size(); index++) {
            Course course = courses.get(index);
            JsonObject item = new JsonObject();
            item.addProperty("code", course.getCode());
            item.addProperty("title", course.getTitle());
            item.addProperty("color_hex", course.getColorHex());
            array.add(item);
        }
        return array;
    }

    private JsonArray notes() {
        JsonArray array = new JsonArray();
        List<Note> notes = AppData.get().getAllNotes();
        for (int index = 0; index < notes.size(); index++) {
            Note note = notes.get(index);
            JsonObject item = new JsonObject();
            item.addProperty("id", note.getId());
            item.addProperty("title", note.getTitle());
            item.addProperty("body", note.getBody());
            item.addProperty("course_code", note.getCourseCode());
            item.addProperty("tags", String.join(",", note.getTags()));
            item.addProperty("created", note.getCreated().toString());
            item.addProperty("edited", note.getEdited().toString());
            array.add(item);
        }
        return array;
    }

    private JsonArray flashcards() {
        JsonArray array = new JsonArray();
        List<Flashcard> flashcards = AppData.get().getFlashcards();
        for (int index = 0; index < flashcards.size(); index++) {
            Flashcard card = flashcards.get(index);
            JsonObject item = new JsonObject();
            item.addProperty("id", card.getId());
            item.addProperty("deck_title", card.getDeckTitle());
            item.addProperty("course_code", card.getCourseCode());
            item.addProperty("front", card.getFront());
            item.addProperty("back", card.getBack());
            item.addProperty("tags", String.join(",", card.getTags()));
            item.addProperty("created", card.getCreated().toString());
            array.add(item);
        }
        return array;
    }

    private JsonArray activity() {
        JsonArray array = new JsonArray();
        List<StudyActivity> entries = AppData.activity().getAll();
        for (int index = 0; index < entries.size(); index++) {
            StudyActivity entry = entries.get(index);
            JsonObject item = new JsonObject();
            item.addProperty("log_date", entry.getDate().toString());
            item.addProperty("profile_id", entry.getProfileId());
            item.addProperty("notes_made", entry.getNotesMade());
            item.addProperty("cards_reviewed", entry.getCardsReviewed());
            item.addProperty("quizzes_taken", entry.getQuizzesTaken());
            array.add(item);
        }
        return array;
    }

    private JsonArray classes() {
        JsonArray array = new JsonArray();
        List<ScheduleClass> classes = AppData.schedule().getClasses();
        for (int index = 0; index < classes.size(); index++) {
            ScheduleClass item = classes.get(index);
            JsonObject json = new JsonObject();
            json.addProperty("id", item.getId());
            json.addProperty("course_code", item.getCourseCode());
            json.addProperty("day", item.getDay());
            json.addProperty("start_time", item.getStart().toString());
            json.addProperty("end_time", item.getEnd().toString());
            json.addProperty("room", item.getRoom());
            array.add(json);
        }
        return array;
    }

    private JsonArray deadlines() {
        JsonArray array = new JsonArray();
        List<Deadline> deadlines = AppData.schedule().getDeadlines();
        for (int index = 0; index < deadlines.size(); index++) {
            Deadline deadline = deadlines.get(index);
            JsonObject item = new JsonObject();
            item.addProperty("id", deadline.getId());
            item.addProperty("course_code", deadline.getCourseCode());
            item.addProperty("title", deadline.getTitle());
            item.addProperty("due_date", deadline.getDueDate().toString());
            item.addProperty("done", deadline.isDone());
            item.addProperty("reminder_on", deadline.isReminderOn());
            array.add(item);
        }
        return array;
    }

    private JsonObject settings() {
        JsonObject result = new JsonObject();
        Map<String, String> values = AppData.settings().getAll();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result.addProperty(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
