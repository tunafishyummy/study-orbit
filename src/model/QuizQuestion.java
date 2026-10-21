package model;

import java.util.ArrayList;
import java.util.List;

public class QuizQuestion {

    public static final String MULTIPLE_CHOICE = "Multiple choice";
    public static final String IDENTIFICATION = "Identification";
    public static final String TRUE_FALSE = "True or false";

    private final String type;
    private final String prompt;
    private final String correctAnswer;
    private final List<String> options;

    public QuizQuestion(String type, String prompt, String correctAnswer, List<String> options) {
        this.type = type;
        this.prompt = prompt;
        this.correctAnswer = correctAnswer;
        this.options = options == null ? new ArrayList<String>() : new ArrayList<String>(options);
    }

    public String getType() {
        return type;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getOptions() {
        return new ArrayList<String>(options);
    }

    public boolean isCorrect(String answer) {
        if (answer == null) {
            return false;
        }
        return answer.trim().equalsIgnoreCase(correctAnswer.trim());
    }
}
