package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class QuizQuestion {

    public static final String MULTIPLE_CHOICE = "Multiple choice";
    public static final String IDENTIFICATION = "Identification";
    public static final String TRUE_FALSE = "True or false";

    private final String id;
    private final String type;
    private final String question;
    private final List<String> options;
    private final int correctIndex;
    private final String answer;
    private final String source;
    private final String explanation;

    public QuizQuestion(String type, String prompt, String correctAnswer, List<String> options) {
        this(UUID.randomUUID().toString(), type, prompt, options, matching(options, correctAnswer),
                correctAnswer, "", "");
    }

    public QuizQuestion(String question, List<String> options, String answer, String source) {
        this(UUID.randomUUID().toString(), MULTIPLE_CHOICE, question, options, matching(options, answer),
                answer, source, "");
    }

    public QuizQuestion(String type, String prompt, List<String> options, int correctIndex,
                        String answer, String explanation) {
        this(UUID.randomUUID().toString(), type, prompt, options, correctIndex, answer, "", explanation);
    }

    public QuizQuestion(String id, String question, List<String> options, String answer, String source) {
        this(id, MULTIPLE_CHOICE, question, options, matching(options, answer), answer, source, "");
    }

    private QuizQuestion(String id, String type, String question, List<String> options, int correctIndex,
                         String answer, String source, String explanation) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.type = type == null ? MULTIPLE_CHOICE : type;
        this.question = question == null ? "" : question;
        this.options = options == null ? new ArrayList<String>() : new ArrayList<String>(options);
        this.source = source == null ? "" : source;
        this.explanation = explanation == null ? "" : explanation;
        if (correctIndex >= 0 && correctIndex < this.options.size()) {
            this.correctIndex = correctIndex;
            this.answer = this.options.get(correctIndex);
        } else {
            this.correctIndex = -1;
            this.answer = answer == null ? "" : answer;
        }
    }

    private static int matching(List<String> options, String answer) {
        if (options == null || answer == null) {
            return -1;
        }
        for (int index = 0; index < options.size(); index++) {
            if (options.get(index).equalsIgnoreCase(answer.trim())) {
                return index;
            }
        }
        return -1;
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

    public List<String> getChoices() {
        return new ArrayList<String>(options);
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public String getSource() {
        return source;
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean hasChoices() {
        return options.isEmpty() == false;
    }

    public boolean isCorrectChoice(int index) {
        return index == correctIndex;
    }

    public boolean isCorrectText(String given) {
        return normalize(given).equals(normalize(answer));
    }

    public boolean isCorrect(String response) {
        if (response == null) {
            return false;
        }
        return response.trim().equalsIgnoreCase(answer.trim());
    }

    public int matchingOptionIndex() {
        if (correctIndex >= 0) {
            return correctIndex;
        }
        for (int index = 0; index < options.size(); index++) {
            if (options.get(index).equalsIgnoreCase(answer.trim())) {
                return index;
            }
        }
        return -1;
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String clean = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        String[] articles = { "the ", "a ", "an " };
        for (int index = 0; index < articles.length; index++) {
            if (clean.startsWith(articles[index])) {
                clean = clean.substring(articles[index].length());
                break;
            }
        }
        return clean;
    }

    @Override
    public String toString() {
        return question;
    }
}
