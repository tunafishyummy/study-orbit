package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Deadline {

    private final String id;
    private String courseCode;
    private String title;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private boolean done;
    private boolean reminderOn;

    public Deadline(String courseCode, String title, LocalDate dueDate) {
        this(UUID.randomUUID().toString(), courseCode, title, dueDate,
                LocalTime.of(23, 59), false, true);
    }

    public Deadline(String courseCode, String title, LocalDate dueDate, LocalTime dueTime) {
        this(UUID.randomUUID().toString(), courseCode, title, dueDate, dueTime, false, true);
    }

    public Deadline(String id, String courseCode, String title, LocalDate dueDate,
                    boolean done, boolean reminderOn) {
        this(id, courseCode, title, dueDate, LocalTime.of(23, 59), done, reminderOn);
    }

    public Deadline(String id, String courseCode, String title, LocalDate dueDate, LocalTime dueTime,
                    boolean done, boolean reminderOn) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.courseCode = courseCode == null ? "" : courseCode;
        this.title = title == null ? "" : title;
        this.dueDate = dueDate == null ? LocalDate.now() : dueDate;
        this.dueTime = dueTime == null ? LocalTime.of(23, 59) : dueTime;
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

    public LocalTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalTime dueTime) {
        this.dueTime = dueTime == null ? LocalTime.of(23, 59) : dueTime;
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
