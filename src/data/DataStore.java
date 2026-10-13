package data;

import model.Course;
import model.Note;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DataStore {
    private final Path file;

    public DataStore(Path file) { this.file = file; }

    public void save(List<Course> courses, List<Note> notes) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Course c : courses) {
            lines.add(String.join("\t", "COURSE", esc(c.getCode()), esc(c.getTitle()), esc(c.getColorHex())));
        }
        for (Note n : notes) {
            lines.add(String.join("\t", "NOTE", esc(n.getId()), esc(n.getTitle()), esc(n.getCourseCode()),
                    esc(String.join(",", n.getTags())), n.getCreated().toString(),
                    n.getEdited().toString(), esc(n.getBody())));
        }
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public void load(List<Course> courses, List<Note> notes) throws IOException {
        courses.clear();
        notes.clear();
        if (!Files.exists(file)) return;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            String[] f = line.split("\t", -1);
            if (f[0].equals("COURSE") && f.length >= 4) {
                courses.add(new Course(unesc(f[1]), unesc(f[2]), unesc(f[3])));
            } else if (f[0].equals("NOTE") && f.length >= 8) {
                String tagText = unesc(f[4]);
                List<String> tags = new ArrayList<>();
                for (String t : tagText.split(",")) if (!t.isBlank()) tags.add(t.trim());
                notes.add(new Note(unesc(f[1]), unesc(f[2]), unesc(f[7]), unesc(f[3]), tags,
                        LocalDateTime.parse(f[5]), LocalDateTime.parse(f[6])));
            }
        }
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "").replace("\n", "\\n");
    }

    private static String unesc(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                sb.append(next == 'n' ? '\n' : next == 't' ? '\t' : next);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
