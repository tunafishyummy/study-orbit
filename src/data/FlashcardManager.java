package data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Flashcard;

public class FlashcardManager {
    private final List<Flashcard> flashcards;

   public FlashcardManager(DataStore store) {
    this.flashcards = new ArrayList<>();
    for (Object obj : AppData.get().getFlashcards()) {
        if (obj instanceof Flashcard) {
            this.flashcards.add((Flashcard) obj);
        }
    }
}

    public List<Flashcard> getAllFlashcards() {
        return new ArrayList<>(flashcards);
    }

    public Flashcard addFlashcard(String deckTitle, String front, String back, List<String> tags) throws IOException {
        if (front.isBlank() || back.isBlank()) {
            throw new IllegalArgumentException("Front and back cannot be empty.");
        }
        Flashcard f = new Flashcard(deckTitle, front, back, tags);
        flashcards.add(f);
        AppData.get().save();
        return f;
    }

    public void deleteFlashcard(Flashcard f) throws IOException {
        flashcards.remove(f);
        AppData.get().save();
    }
}