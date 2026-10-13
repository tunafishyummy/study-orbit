package data;

import java.io.IOException;
import java.nio.file.Path;

public final class AppData {
    private static NoteManager instance;

    private AppData() {}

    public static synchronized NoteManager get() {
        if (instance == null) {
            try {
                instance = new NoteManager(new DataStore(Path.of("data", "studyorbit.dat")));
            } catch (IOException e) {
                throw new RuntimeException("Could not load Study Orbit data", e);
            }
        }
        return instance;
    }
}
