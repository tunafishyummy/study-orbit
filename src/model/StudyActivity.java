package model;

import java.time.LocalDate;

public class StudyActivity {

    private final LocalDate date;
    private final String profileId;
    private int notesMade;
    private int cardsReviewed;
    private int quizzesTaken;

    public StudyActivity(LocalDate date, String profileId, int notesMade, int cardsReviewed, int quizzesTaken) {
        this.date = date;
        this.profileId = profileId == null ? "" : profileId;
        this.notesMade = notesMade;
        this.cardsReviewed = cardsReviewed;
        this.quizzesTaken = quizzesTaken;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getProfileId() {
        return profileId;
    }

    public int getNotesMade() {
        return notesMade;
    }

    public int getCardsReviewed() {
        return cardsReviewed;
    }

    public int getQuizzesTaken() {
        return quizzesTaken;
    }

    public void addNote() {
        notesMade = notesMade + 1;
    }

    public void addCardsReviewed(int count) {
        cardsReviewed = cardsReviewed + count;
    }

    public void addQuiz() {
        quizzesTaken = quizzesTaken + 1;
    }

    public boolean isEmpty() {
        return notesMade == 0 && cardsReviewed == 0 && quizzesTaken == 0;
    }
}
