package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class QuizAttempt {

    private final String id;
    private final String profileId;
    private final LocalDateTime created;
    private final String source;
    private final String type;
    private final int questionCount;
    private final int correctCount;
    private final String details;

    public QuizAttempt(String id, String profileId, LocalDateTime created, String source, String type,
                       int questionCount, int correctCount, String details) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.profileId = profileId == null ? "" : profileId;
        this.created = created == null ? LocalDateTime.now() : created;
        this.source = source == null ? "" : source;
        this.type = type == null ? "" : type;
        this.questionCount = questionCount;
        this.correctCount = correctCount;
        this.details = details == null ? "" : details;
    }

    public String getId() {
        return id;
    }

    public String getProfileId() {
        return profileId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return correctCount + " / " + questionCount + "  " + type;
    }
}
