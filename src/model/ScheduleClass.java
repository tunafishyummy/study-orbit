package model;

import java.time.LocalTime;
import java.util.UUID;

public class ScheduleClass {

    private final String id;
    private String courseCode;
    private String day;
    private LocalTime start;
    private LocalTime end;
    private String room;

    public ScheduleClass(String courseCode, String day, LocalTime start, LocalTime end, String room) {
        this(UUID.randomUUID().toString(), courseCode, day, start, end, room);
    }

    public ScheduleClass(String id, String courseCode, String day, LocalTime start, LocalTime end, String room) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.courseCode = courseCode == null ? "" : courseCode;
        this.day = day == null ? "Mon" : day;
        this.start = start == null ? LocalTime.of(8, 0) : start;
        this.end = end == null ? LocalTime.of(9, 0) : end;
        this.room = room == null ? "" : room;
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

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day == null ? "Mon" : day;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room == null ? "" : room;
    }

    @Override
    public String toString() {
        return day + " " + start + " " + courseCode;
    }
}
