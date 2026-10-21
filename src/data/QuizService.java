package data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import model.Note;
import model.QuizQuestion;

// asks Gemini to turn notes into quiz questions
public class QuizService {
    private static final String MODEL = "gemini-3.8-flash";
    private static final int MAX_NOTE_CHARS = 12000;
    private final String apiKey;

    public QuizService(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<QuizQuestion> generate(List<Note> notes, int count, String ambiguity, String type) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key is missing.");
        }

        StringBuilder material = new StringBuilder();
        for (Note n : notes) {
            material.append("## ").append(n.getTitle()).append("\n").append(n.getBody()).append("\n\n");
        }
        String text = material.toString();
        if (text.length() > MAX_NOTE_CHARS) {
            text = text.substring(0, MAX_NOTE_CHARS);
        }

        String prompt = "Write a quiz of exactly " + count + " questions based ONLY on the notes below.\n"
                + "Question type: " + type + ".\n"
                + "Difficulty / ambiguity: " + ambiguityRule(ambiguity) + "\n\n"
                + formatRule(type) + "\n"
                + "Every object also has an 'explanation' key: one short sentence on why the answer is right.\n"
                + "Respond STRICTLY with a raw JSON array. No Markdown, no code blocks.\n\n"
                + "NOTES:\n" + text;

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        JsonArray parts = new JsonArray();
        parts.add(textPart);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject config = new JsonObject();
        config.addProperty("responseMimeType", "application/json");
        JsonObject body = new JsonObject();
        body.add("contents", contents);
        body.add("generationConfig", config);

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL
                + ":generateContent?key=" + apiKey;
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            String reply = response.body() == null ? "" : response.body();
            if (reply.length() > 200) reply = reply.substring(0, 200) + "...";
            throw new RuntimeException("The AI service answered HTTP " + response.statusCode() + ". " + reply);
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        String raw = json.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();

        // the model sometimes wraps the array in text or code fences, so cut out the array itself
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start < 0 || end <= start) {
            throw new RuntimeException("The AI reply was not a quiz. Try again.");
        }
        JsonArray array = JsonParser.parseString(raw.substring(start, end + 1)).getAsJsonArray();

        List<QuizQuestion> result = new ArrayList<>();
        for (JsonElement element : array) {
            try {
                QuizQuestion q = parseQuestion(element.getAsJsonObject(), type);
                if (q != null) result.add(q);
            } catch (RuntimeException skipBadQuestion) {
                // ignore one malformed question and keep the rest
            }
        }
        if (result.isEmpty()) {
            throw new RuntimeException("No usable questions came back. Try again.");
        }
        if (result.size() > count) {
            result = new ArrayList<>(result.subList(0, count));
        }
        return result;
    }

    private String ambiguityRule(String ambiguity) {
        if ("High".equals(ambiguity)) {
            return "HIGH: tricky wording and closely related, plausible wrong answers, but exactly one answer "
                    + "must still be correct according to the notes.";
        }
        if ("Medium".equals(ambiguity)) {
            return "MEDIUM: needs some thought, with plausible wrong answers.";
        }
        return "LOW: direct and unambiguous, one clearly correct answer, obviously wrong alternatives.";
    }

    private String formatRule(String type) {
        if (QuizQuestion.IDENTIFICATION.equals(type)) {
            return "Each object has: 'question' (string) and 'answer' (a short word or phrase, 1-4 words).";
        }
        if (QuizQuestion.TRUE_FALSE.equals(type)) {
            return "Each object has: 'question' (a statement to judge) and 'answerIndex' (0 if the statement is "
                    + "true, 1 if it is false). Make about half of them false.";
        }
        return "Each object has: 'question' (string), 'choices' (array of exactly 4 strings) and 'answerIndex' "
                + "(0-3, the position of the correct choice). Spread the correct answer across positions.";
    }

    private QuizQuestion parseQuestion(JsonObject o, String type) {
        String question = str(o, "question");
        if (question.isEmpty()) return null;
        String explanation = str(o, "explanation");

        if (QuizQuestion.IDENTIFICATION.equals(type)) {
            String answer = str(o, "answer");
            if (answer.isEmpty()) return null;
            return new QuizQuestion(type, question, new ArrayList<>(), -1, answer, explanation);
        }

        List<String> choices = new ArrayList<>();
        int index = o.has("answerIndex") && !o.get("answerIndex").isJsonNull() ? o.get("answerIndex").getAsInt() : -1;
        if (QuizQuestion.TRUE_FALSE.equals(type)) {
            choices.add("True");
            choices.add("False");
            if (index < 0 || index > 1) {
                String answer = str(o, "answer").toLowerCase();
                index = answer.startsWith("t") ? 0 : answer.startsWith("f") ? 1 : -1;
            }
        } else {
            if (!o.has("choices") || !o.get("choices").isJsonArray()) return null;
            for (JsonElement c : o.getAsJsonArray("choices")) {
                choices.add(c.getAsString().trim());
            }
            if (choices.size() < 2) return null;
        }
        if (index < 0 || index >= choices.size()) return null;
        return new QuizQuestion(type, question, choices, index, "", explanation);
    }

    private String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString().trim() : "";
    }
}