package data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.QuizAttempt;
import model.QuizQuestion;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizHistoryManager {

    private final DataStore store;
    private final String profileId;
    private final List<QuizAttempt> attempts = new ArrayList<QuizAttempt>();

    public QuizHistoryManager(DataStore store, String profileId) throws IOException {
        this.store = store;
        this.profileId = profileId == null ? "" : profileId;
        attempts.addAll(store.loadQuizAttempts(this.profileId));
    }

    public List<QuizAttempt> getAttempts() {
        return new ArrayList<QuizAttempt>(attempts);
    }

    public String loadDraft() throws IOException {
        return store.loadQuizDraft(profileId);
    }

    public void saveDraft(String state) throws IOException {
        store.saveQuizDraft(profileId, state);
    }

    public void clearDraft() throws IOException {
        store.deleteQuizDraft(profileId);
    }

    public void record(String source, List<QuizQuestion> questions, boolean[] correct,
                       int[] picks, String[] given) throws IOException {
        int correctCount = 0;
        JsonArray items = new JsonArray();
        for (int index = 0; index < questions.size(); index++) {
            QuizQuestion question = questions.get(index);
            JsonObject item = new JsonObject();
            item.addProperty("question", question.getPrompt());
            item.addProperty("type", question.getType());
            JsonArray choices = new JsonArray();
            for (String choice : question.getChoices()) {
                choices.add(choice);
            }
            item.add("choices", choices);
            item.addProperty("answer", question.getAnswer());
            item.addProperty("given",
                    given != null && index < given.length && given[index] != null ? given[index] : "");
            item.addProperty("pick", picks != null && index < picks.length ? picks[index] : -1);
            boolean right = correct != null && index < correct.length && correct[index];
            item.addProperty("correct", right);
            item.addProperty("explanation", question.getExplanation());
            items.add(item);
            if (right) {
                correctCount++;
            }
        }
        JsonObject details = new JsonObject();
        details.add("questions", items);
        String type = questions.isEmpty() ? "" : questions.get(0).getType();
        attempts.add(0, new QuizAttempt(null, profileId, LocalDateTime.now(), source, type,
                questions.size(), correctCount, details.toString()));
        store.saveQuizAttempts(profileId, attempts);
    }
}
