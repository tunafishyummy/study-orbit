package data;

import model.Deadline;
import model.ScheduleClass;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScheduleManager {

    private final DataStore store;
    private final List<ScheduleClass> classes = new ArrayList<ScheduleClass>();
    private final List<Deadline> deadlines = new ArrayList<Deadline>();

    public ScheduleManager(DataStore store) throws IOException {
        this.store = store;
        classes.addAll(store.loadClasses());
        deadlines.addAll(store.loadDeadlines());
    }

    public List<ScheduleClass> getClasses() {
        return new ArrayList<ScheduleClass>(classes);
    }

    public List<Deadline> getDeadlines() {
        return new ArrayList<Deadline>(deadlines);
    }

    public List<ScheduleClass> getClassesForDay(String day) {
        List<ScheduleClass> result = new ArrayList<ScheduleClass>();
        for (int index = 0; index < classes.size(); index++) {
            if (classes.get(index).getDay().equalsIgnoreCase(day)) {
                result.add(classes.get(index));
            }
        }
        Collections.sort(result, Comparator.comparing(ScheduleClass::getStart));
        return result;
    }

    public List<Deadline> getUpcoming(int limit) {
        List<Deadline> result = new ArrayList<Deadline>();
        for (int index = 0; index < deadlines.size(); index++) {
            if (deadlines.get(index).isDone() == false) {
                result.add(deadlines.get(index));
            }
        }
        Collections.sort(result, Comparator.comparing(Deadline::getDueDate));
        if (result.size() > limit) {
            return new ArrayList<Deadline>(result.subList(0, limit));
        }
        return result;
    }

    public List<Deadline> getDueSoon(int days) {
        List<Deadline> result = new ArrayList<Deadline>();
        LocalDate limit = LocalDate.now().plusDays(days);
        for (int index = 0; index < deadlines.size(); index++) {
            Deadline deadline = deadlines.get(index);
            if (deadline.isDone() || deadline.isReminderOn() == false) {
                continue;
            }
            if (deadline.getDueDate().isBefore(LocalDate.now())) {
                continue;
            }
            if (deadline.getDueDate().isAfter(limit)) {
                continue;
            }
            result.add(deadline);
        }
        Collections.sort(result, Comparator.comparing(Deadline::getDueDate));
        return result;
    }

    public void addClass(String courseCode, String day, LocalTime start, LocalTime end, String room) throws IOException {
        classes.add(new ScheduleClass(courseCode, day, start, end, room));
        store.saveClasses(classes);
    }

    public void deleteClass(ScheduleClass item) throws IOException {
        classes.remove(item);
        store.saveClasses(classes);
    }

    public void addDeadline(String courseCode, String title, LocalDate dueDate) throws IOException {
        deadlines.add(new Deadline(courseCode, title, dueDate));
        store.saveDeadlines(deadlines);
    }

    public void updateDeadline(Deadline deadline) throws IOException {
        store.saveDeadlines(deadlines);
    }

    public void deleteDeadline(Deadline deadline) throws IOException {
        deadlines.remove(deadline);
        store.saveDeadlines(deadlines);
    }
}
