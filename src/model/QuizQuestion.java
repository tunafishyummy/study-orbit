package model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuizQuestion {

    public static final String MULTIPLE_CHOICE = "Multiple choice";
    public static final String IDENTIFICATION = "Identification";
    public static final String TRUE_FALSE = "True or false";

    private final String id;
    private final String type;
    private final String question;
    private final List<String> options;
    private final String answer;
    private final String source;

    public QuizQuestion(String type, String prompt, String correctAnswer, List<String> options) {
        this(UUID.randomUUID().toString(), type, prompt, correctAnswer, options, "");
    }

    public QuizQuestion(String question, List<String> options, String answer, String source) {
        this(UUID.randomUUID().toString(), MULTIPLE_CHOICE, question, answer, options, source);
    }

    public QuizQuestion(String id, String question, List<String> options, String answer, String source) {
        this(id, MULTIPLE_CHOICE, question, answer, options, source);
    }

    private QuizQuestion(String id, String type, String question, String answer, List<String> options, String source) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.type = type == null ? MULTIPLE_CHOICE : type;
        this.question = question == null ? "" : question;
        this.options = options == null ? new ArrayList<String>() : new ArrayList<String>(options);
        this.answer = answer == null ? "" : answer;
        this.source = source == null ? "" : source;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPrompt() {
        return question;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return answer;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getOptions() {
        return new ArrayList<String>(options);
    }

    public String getSource() {
        return source;
    }

    public boolean isCorrect(String response) {
        if (response == null) {
            return false;
        }
        return response.trim().equalsIgnoreCase(answer.trim());
    }

    public int matchingOptionIndex() {
        for (int index = 0; index < options.size(); index++) {
            if (options.get(index).equalsIgnoreCase(answer.trim())) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return question;
    }
}
