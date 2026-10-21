package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// one generated quiz question: multiple choice, true or false, or identification
public class QuizQuestion {
    public static final String MULTIPLE_CHOICE = "Multiple choice";
    public static final String TRUE_FALSE = "True or false";
    public static final String IDENTIFICATION = "Identification";

    private final String type;
    private final String prompt;
    private final List<String> choices;
    private final int correctIndex;      // -1 for identification
    private final String answer;         // the correct answer as text
    private final String explanation;

    public QuizQuestion(String type, String prompt, List<String> choices, int correctIndex,
                        String answer, String explanation) {
        this.type = type == null ? MULTIPLE_CHOICE : type;
        this.prompt = prompt == null ? "" : prompt.trim();
        this.choices = choices == null ? new ArrayList<>() : new ArrayList<>(choices);
        this.correctIndex = correctIndex;
        this.explanation = explanation == null ? "" : explanation.trim();
        if (correctIndex >= 0 && correctIndex < this.choices.size()) {
            this.answer = this.choices.get(correctIndex);
        } else {
            this.answer = answer == null ? "" : answer.trim();
        }
    }

    public String getType() { return type; }
    public String getPrompt() { return prompt; }
    public List<String> getChoices() { return new ArrayList<>(choices); }
    public String getAnswer() { return answer; }
    public String getExplanation() { return explanation; }
    public boolean hasChoices() { return !choices.isEmpty(); }

    public boolean isCorrectChoice(int index) {
        return index == correctIndex;
    }

    // identification answers ignore case, punctuation and a leading "the / a / an"
    public boolean isCorrectText(String given) {
        return normalize(given).equals(normalize(answer));
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String t = s.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        for (String article : new String[]{"the ", "a ", "an "}) {
            if (t.startsWith(article)) {
                t = t.substring(article.length());
                break;
            }
        }
        return t;
    }
}