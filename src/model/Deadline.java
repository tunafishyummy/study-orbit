package model;

import java.time.LocalDate;
import java.util.UUID;

public class Deadline {

    private final String id;
    private String courseCode;
    private String title;
    private LocalDate dueDate;
    private boolean done;
    private boolean reminderOn;

    public Deadline(String courseCode, String title, LocalDate dueDate) {
        this(UUID.randomUUID().toString(), courseCode, title, dueDate, false, true);
    }

    public Deadline(String id, String courseCode, String title, LocalDate dueDate,
                    boolean done, boolean reminderOn) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.courseCode = courseCode == null ? "" : courseCode;
        this.title = title == null ? "" : title;
        this.dueDate = dueDate == null ? LocalDate.now() : dueDate;
        this.done = done;
        this.reminderOn = reminderOn;
    }

    public String getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode == null ? "" : courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isReminderOn() {
        return reminderOn;
    }

    public void setReminderOn(boolean reminderOn) {
        this.reminderOn = reminderOn;
    }

    @Override
    public String toString() {
        return title + " (" + dueDate + ")";
    }
}
