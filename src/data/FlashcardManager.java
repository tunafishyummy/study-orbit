package data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Flashcard;

public class FlashcardManager {
    private final List<Flashcard> flashcards;

    @SuppressWarnings("unchecked")
    public FlashcardManager(DataStore store) {
        this.flashcards = (List<Flashcard>) (List<?>) AppData.get().getFlashcards();
    }

    public List<Flashcard> getAllFlashcards() {
        return new ArrayList<>(flashcards);
    }

    public Flashcard addFlashcard(String deckTitle, String front, String back, List<String> tags) throws IOException {
        return addFlashcard(deckTitle, "", front, back, tags);
    }

    public Flashcard addFlashcard(String deckTitle, String courseCode, String front, String back, List<String> tags) throws IOException {
        if (front.isBlank() || back.isBlank()) {
            throw new IllegalArgumentException("Front and back cannot be empty.");
        }
        Flashcard f = new Flashcard(deckTitle, courseCode, front, back, tags);
        flashcards.add(f);
        AppData.get().save(); // Persists live list changes to SQLite database
        return f;
    }

    public void deleteFlashcard(Flashcard f) throws IOException {
        flashcards.remove(f);
        AppData.get().save();
    }

    public void updateFlashcard(Flashcard card, String deckTitle, String front, String back, List<String> tags)
            throws IOException {
        if (deckTitle == null || deckTitle.isBlank()) {
            throw new IllegalArgumentException("Deck title cannot be empty.");
        }
        if (front == null || front.isBlank() || back == null || back.isBlank()) {
            throw new IllegalArgumentException("Front and back cannot be empty.");
        }
        card.update(deckTitle.trim(), front.trim(), back.trim(), tags);
        AppData.get().save();
    }

    public void renameDeck(String oldDeck, String newDeck) throws IOException {
        if (newDeck == null || newDeck.isBlank()) {
            throw new IllegalArgumentException("Deck title cannot be empty.");
        }
        boolean changed = false;
        for (Flashcard card : flashcards) {
            if (card.getDeckTitle().equalsIgnoreCase(oldDeck)) {
                card.setDeckTitle(newDeck.trim());
                changed = true;
            }
        }
        if (changed) {
            AppData.get().save();
        }
    }

    public void deleteDeck(String deckTitle) throws IOException {
        if (deckTitle == null) {
            return;
        }
        boolean changed = false;
        for (int index = flashcards.size() - 1; index >= 0; index--) {
            if (flashcards.get(index).getDeckTitle().equalsIgnoreCase(deckTitle)) {
                flashcards.remove(index);
                changed = true;
            }
        }
        if (changed) {
            AppData.get().save();
        }
    }
}