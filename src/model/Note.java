package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Note {
    private final String id;
    private String title;
    private String body;
    private String courseCode; // "" means no course
    private List<String> tags;
    private final LocalDateTime created;
    private LocalDateTime edited;

    public Note(String title, String body, String courseCode, List<String> tags) {
        this(UUID.randomUUID().toString(), title, body, courseCode, tags,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public Note(String id, String title, String body, String courseCode, List<String> tags,
                LocalDateTime created, LocalDateTime edited) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.courseCode = courseCode == null ? "" : courseCode;
        this.tags = new ArrayList<>(tags);
        this.created = created;
        this.edited = edited;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getCourseCode() { return courseCode; }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getEdited() { return edited; }

    public void update(String title, String body, String courseCode, List<String> tags) {
        this.title = title;
        this.body = body;
        this.courseCode = courseCode == null ? "" : courseCode;
        this.tags = new ArrayList<>(tags);
        this.edited = LocalDateTime.now();
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode == null ? "" : courseCode;
        this.edited = LocalDateTime.now();
    }

    @Override
    public String toString() { return title.isBlank() ? "(untitled)" : title; }
}
