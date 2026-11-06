package data;

import ai.AiConfig;
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
import java.util.Base64;

// reads text from an image with the gemini vision endpoint
public class AIFlashcardService {
    private static final String OCR_PROMPT =
            "Extract all readable text from this image accurately and format it cleanly as structured notes.";
    private final String apiKey;
    private final String model;

    public AIFlashcardService(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model == null || model.isBlank() ? AiConfig.defaultModel(AiConfig.GEMINI) : model.trim();
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
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model
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
