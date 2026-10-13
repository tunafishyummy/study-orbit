package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Flashcard {
    private String id;
    private String deckTitle;
    private String front;
    private String back;
    private List<String> tags;
    private LocalDateTime created;

    public Flashcard(String deckTitle, String front, String back, List<String> tags) {
        this(UUID.randomUUID().toString(), deckTitle, front, back, tags, LocalDateTime.now());
    }

    public Flashcard(String id, String deckTitle, String front, String back, List<String> tags, LocalDateTime created) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.deckTitle = deckTitle == null ? "General" : deckTitle;
        this.front = front == null ? "" : front;
        this.back = back == null ? "" : back;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.created = created == null ? LocalDateTime.now() : created;
    }

    public String getId() { return id; }
    public String getDeckTitle() { return deckTitle; }
    public String getFront() { return front; }
    public String getBack() { return back; }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public LocalDateTime getCreated() { return created; }

    @Override
    public String toString() { return deckTitle + " - " + front; }
}