package data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

// reads text from an image with the gemini vision endpoint
public class AIFlashcardService {
    private static final String MODEL = "gemini-3.8-flash";
    private static final String OCR_PROMPT =
            "Extract all readable text from this image accurately and format it cleanly as structured notes.";
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

        String prompt = "Extract 3 to 5 key flashcard questions and answers from the following note titled '"
                + noteTitle.replace("\"", "'") + "'.\n\n"
                + "Note Content:\n" + noteBody.replace("\"", "'") + "\n\n"
                + "Respond STRICTLY with a raw JSON array of objects. Do not include Markdown formatting or code blocks. "
                + "Each object must have 'front' (question/prompt) and 'back' (definition/answer) keys.";

        String rawText = stripCodeFence(send(prompt, null, null));
        JsonArray cardsArray = JsonParser.parseString(rawText).getAsJsonArray();

        List<SuggestedCard> suggestions = new ArrayList<SuggestedCard>();
        for (int index = 0; index < cardsArray.size(); index++) {
            JsonObject object = cardsArray.get(index).getAsJsonObject();
            if (object.has("front") && object.has("back")) {
                suggestions.add(new SuggestedCard(
                        object.get("front").getAsString(),
                        object.get("back").getAsString()));
            }
        }
        if (suggestions.isEmpty()) {
            throw new IOException("The AI returned no suggestions. Check the API key and try again.");
        }
        return suggestions;
    }

    public String extractTextFromImage(byte[] imageBytes, String mimeType) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key is missing.");
        }
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("The image is empty.");
        }
        String type = mimeType == null || mimeType.isBlank() ? "image/jpeg" : mimeType;
        return send(OCR_PROMPT, imageBytes, type);
    }

    private String send(String prompt, byte[] imageBytes, String mimeType) throws Exception {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL
                + ":generateContent?key=" + apiKey;

        JsonArray partsArray = new JsonArray();
        if (imageBytes != null) {
            JsonObject inlineData = new JsonObject();
            inlineData.addProperty("mime_type", mimeType);
            inlineData.addProperty("data", Base64.getEncoder().encodeToString(imageBytes));

            JsonObject filePart = new JsonObject();
            filePart.add("inline_data", inlineData);
            partsArray.add(filePart);
        }

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        partsArray.add(textPart);

        JsonObject contentObject = new JsonObject();
        contentObject.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contentObject);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contentsArray);

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("The AI request was interrupted.", exception);
        } catch (IOException exception) {
            throw new IOException("Could not reach the AI service. Check the internet connection and try again.",
                    exception);
        }

        if (response.statusCode() != 200) {
            throw new IOException("The AI service answered HTTP " + response.statusCode() + ". "
                    + shorten(response.body()));
        }

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        if (jsonResponse.has("candidates") == false || jsonResponse.getAsJsonArray("candidates").size() == 0) {
            throw new IOException("The AI returned no text. Check the API key and try again.");
        }
        JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
        if (candidate.has("content") == false) {
            throw new IOException("The AI response did not include any content.");
        }
        JsonObject content = candidate.getAsJsonObject("content");
        if (content.has("parts") == false || content.getAsJsonArray("parts").size() == 0) {
            throw new IOException("The AI response did not include any text.");
        }
        JsonElement part = content.getAsJsonArray("parts").get(0);
        if (part.getAsJsonObject().has("text") == false) {
            throw new IOException("The AI response did not include any text.");
        }
        String text = part.getAsJsonObject().get("text").getAsString().trim();
        if (text.isEmpty()) {
            throw new IOException("The AI returned no text. Check the API key and try again.");
        }
        return text;
    }

    private String stripCodeFence(String rawText) {
        String text = rawText == null ? "" : rawText.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        }
        if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    private String shorten(String body) {
        if (body == null) {
            return "";
        }
        String clean = body.replaceAll("\\s+", " ").trim();
        if (clean.length() > 300) {
            return clean.substring(0, 300) + "...";
        }
        return clean;
    }
}
