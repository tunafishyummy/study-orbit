package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Note {
    private String id;
    private String title;
    private String body;
    private String courseCode;
    private List<String> tags;
    private LocalDateTime created;
    private LocalDateTime edited;

    public Note(String title, String body, String courseCode, List<String> tags) {
        this(UUID.randomUUID().toString(), title, body, courseCode, tags, LocalDateTime.now(), LocalDateTime.now());
    }

    public Note(String id, String title, String body, String courseCode, List<String> tags,
                LocalDateTime created, LocalDateTime edited) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.title = title == null ? "" : title;
        this.body = body == null ? "" : body;
        this.courseCode = courseCode == null ? "" : courseCode;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.created = created == null ? LocalDateTime.now() : created;
        this.edited = edited == null ? this.created : edited;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getCourseCode() { return courseCode; }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getEdited() { return edited; }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode == null ? "" : courseCode;
        this.edited = LocalDateTime.now();
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.edited = LocalDateTime.now();
    }

    public void update(String title, String body, String courseCode, List<String> tags) {
        this.title = title == null ? "" : title;
        this.body = body == null ? "" : body;
        this.courseCode = courseCode == null ? "" : courseCode;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.edited = LocalDateTime.now();
    }

    @Override
    public String toString() { return title; }
}
