package data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class AIFlashcardService {
    private final String apiKey;

    public static class SuggestedCard {
        public String front;
        public String back;

        public SuggestedCard(String front, String back) {
            this.front = front;
            this.back = back;
        }
    }

    public AIFlashcardService(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<SuggestedCard> generateSuggestions(String noteTitle, String noteBody) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key is missing.");
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=" + apiKey;
        
        String prompt = "Extract 3 to 5 key flashcard questions and answers from the following note titled '" 
                + noteTitle.replace("\"", "'") + "'.\n\n"
                + "Note Content:\n" + noteBody.replace("\"", "'") + "\n\n"
                + "Respond STRICTLY with a raw JSON array of objects. Do not include Markdown formatting or code blocks. "
                + "Each object must have 'front' (question/prompt) and 'back' (definition/answer) keys.";

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray partsArray = new JsonArray();
        partsArray.add(textPart);

        JsonObject contentObject = new JsonObject();
        contentObject.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contentObject);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contentsArray);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Request failed with HTTP " + response.statusCode() + ": " + response.body());
        }

        List<SuggestedCard> suggestions = new ArrayList<>();
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        String rawText = jsonResponse.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString().trim();

        // Strip Markdown code block wrappers if the model returns them
        if (rawText.startsWith("```json")) {
            rawText = rawText.substring(7);
        }
        if (rawText.startsWith("```")) {
            rawText = rawText.substring(3);
        }
        if (rawText.endsWith("```")) {
            rawText = rawText.substring(0, rawText.length() - 3);
        }

        JsonArray cardsArray = JsonParser.parseString(rawText.trim()).getAsJsonArray();
        for (int i = 0; i < cardsArray.size(); i++) {
            JsonObject obj = cardsArray.get(i).getAsJsonObject();
            suggestions.add(new SuggestedCard(
                    obj.get("front").getAsString(),
                    obj.get("back").getAsString()
            ));
        }

        return suggestions;
    }
}