package data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Course;
import model.Flashcard;
import model.Note;

public class NoteManager {
    private final List<Course> courses = new ArrayList<>();
    private final List<Note> notes = new ArrayList<>();
    private final List<Flashcard> flashcards = new ArrayList<>();
    private final DataStore store;

    public NoteManager(DataStore store) throws IOException {
        this.store = store;
        store.load(courses, notes, flashcards);
    }

    public List<Course> getCourses() { return new ArrayList<>(courses); }

    public Course findCourse(String code) {
        for (Course c : courses) if (c.getCode().equalsIgnoreCase(code)) return c;
        return null;
    }

    public void addCourse(String code, String title, String colorHex) throws IOException {
        if (code.isBlank() || title.isBlank()) throw new IllegalArgumentException("Course code and title are required.");
        if (findCourse(code) != null) throw new IllegalArgumentException("Course code already exists.");
        courses.add(new Course(code.trim(), title.trim(), colorHex));
        save();
    }

    public void updateCourse(String oldCode, String code, String title, String colorHex) throws IOException {
        Course c = findCourse(oldCode);
        if (c == null) return;
        if (code.isBlank() || title.isBlank()) throw new IllegalArgumentException("Course code and title are required.");
        Course other = findCourse(code);
        if (other != null && other != c) throw new IllegalArgumentException("Course code already exists.");
        for (Note n : notes) if (n.getCourseCode().equals(c.getCode())) n.setCourseCode(code.trim());
        for (Flashcard f : flashcards) if (f.getCourseCode().equals(c.getCode())) f.setCourseCode(code.trim());
        c.setCode(code.trim());
        c.setTitle(title.trim());
        c.setColorHex(colorHex);
        save();
    }

    public void deleteCourse(String code) throws IOException {
        Course c = findCourse(code);
        if (c == null) return;
        for (Note n : notes) if (n.getCourseCode().equals(c.getCode())) n.setCourseCode("");
        for (Flashcard f : flashcards) if (f.getCourseCode().equals(c.getCode())) f.setCourseCode("");
        courses.remove(c);
        save();
    }

    public List<Note> getAllNotes() { return new ArrayList<>(notes); }

    public List<Note> getNotesByCourse(String courseCode) {
        List<Note> result = new ArrayList<>();
        for (Note n : notes) if (courseCode == null || n.getCourseCode().equals(courseCode)) result.add(n);
        return result;
    }

    public Note addNote(String title, String body, String courseCode, List<String> tags) throws IOException {
        Note n = new Note(title, body, courseCode, tags);
        notes.add(n);
        save();
        return n;
    }

    public void updateNote(Note n, String title, String body, String courseCode, List<String> tags) throws IOException {
        n.update(title, body, courseCode, tags);
        save();
    }

    public void moveNote(Note n, String newCourseCode) throws IOException {
        n.setCourseCode(newCourseCode);
        save();
    }

    public void deleteNote(Note n) throws IOException {
        notes.remove(n);
        save();
    }

    public void addFlashcard(String deckTitle, String front, String back, List<String> tags) throws IOException {
        if (deckTitle == null || deckTitle.isBlank()) {
            throw new IllegalArgumentException("Deck title is required.");
        }
        if (front == null || front.isBlank() || back == null || back.isBlank()) {
            throw new IllegalArgumentException("Front and back are required.");
        }
        flashcards.add(new Flashcard(deckTitle.trim(), front.trim(), back.trim(), tags));
        save();
    }

    public void deleteFlashcard(Flashcard f) throws IOException {
        flashcards.remove(f);
        save();
    }

    public void exportNote(Note n, Path target) throws IOException {
        String text = n.getTitle() + "\n"
                + "Course: " + (n.getCourseCode().isEmpty() ? "None" : n.getCourseCode()) + "\n"
                + "Tags: " + String.join(", ", n.getTags()) + "\n"
                + "Last edited: " + n.getEdited().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n"
                + "----------------------------------------\n"
                + n.getBody() + "\n";
        Files.writeString(target, text, StandardCharsets.UTF_8);
    }

    public DataStore getStoreReference() { return store; }
    public List<Flashcard> getFlashcards() { return flashcards; }
    public void save() throws IOException { store.save(courses, notes, flashcards); }
}