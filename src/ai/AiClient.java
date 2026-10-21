package ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// talks to any OpenAI compatible chat endpoint, using only the JDK
public class AiClient {
    private final AiConfig config;
    private final HttpClient http;

    public AiClient(AiConfig config) {
        this.config = config;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String complete(String systemPrompt, String userPrompt) throws IOException {
        String base = trimmedBaseUrl();
        if (base.isEmpty()) {
            throw new IOException("No AI provider is set. Open Settings and choose one first.");
        }

        String body = "{"
                + "\"model\":\"" + escape(config.getModel()) + "\","
                + "\"temperature\":0.1,"
                + "\"stream\":false,"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escape(systemPrompt) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escape(userPrompt) + "\"}"
                + "]}";

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(base + "/chat/completions"))
                .timeout(Duration.ofSeconds(180))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (config.getApiKey() != null && config.getApiKey().isBlank() == false) {
            request.header("Authorization", "Bearer " + config.getApiKey());
        }

        HttpResponse<String> response;
        try {
            response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("The AI request was interrupted.", exception);
        } catch (IOException exception) {
            throw new IOException("Could not reach the AI provider at " + base
                    + ". If you use the local model, start Ollama first with \"ollama run "
                    + config.getModel() + "\" and try again.", exception);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("The AI provider answered with status " + response.statusCode()
                    + ".\n" + shorten(response.body()));
        }

        String content = extractContent(response.body());
        if (content == null || content.isBlank()) {
            throw new IOException("The AI provider sent an empty answer. Please try again.");
        }
        return content;
    }

    public boolean isAvailable() {
        String base = trimmedBaseUrl();
        if (base.isEmpty()) {
            return false;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(base + "/models"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 500;
        } catch (Exception exception) {
            return false;
        }
    }

    private String trimmedBaseUrl() {
        String base = config.getBaseUrl();
        if (base == null) {
            return "";
        }
        base = base.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    private String shorten(String text) {
        if (text == null) {
            return "";
        }
        text = text.replaceAll("\\s+", " ").trim();
        if (text.length() > 200) {
            return text.substring(0, 200) + "...";
        }
        return text;
    }

    static String extractContent(String json) {
        int message = json.indexOf("\"message\"");
        if (message < 0) {
            return null;
        }
        int content = json.indexOf("\"content\"", message);
        if (content < 0) {
            return null;
        }
        int colon = json.indexOf(':', content);
        if (colon < 0) {
            return null;
        }
        int quote = json.indexOf('"', colon + 1);
        if (quote < 0) {
            return null;
        }
        return parseJsonString(json, quote);
    }

    static String parseJsonString(String json, int startQuote) {
        StringBuilder out = new StringBuilder();
        for (int index = startQuote + 1; index < json.length(); index++) {
            char current = json.charAt(index);
            if (current == '"') {
                return out.toString();
            }
            if (current != '\\') {
                out.append(current);
                continue;
            }
            index++;
            if (index >= json.length()) {
                break;
            }
            char escaped = json.charAt(index);
            switch (escaped) {
                case 'n':
                    out.append('\n');
                    break;
                case 't':
                    out.append('\t');
                    break;
                case 'r':
                    out.append('\r');
                    break;
                case 'b':
                    out.append('\b');
                    break;
                case 'f':
                    out.append('\f');
                    break;
                case '"':
                    out.append('"');
                    break;
                case '\\':
                    out.append('\\');
                    break;
                case '/':
                    out.append('/');
                    break;
                case 'u':
                    if (index + 4 < json.length()) {
                        try {
                            out.append((char) Integer.parseInt(json.substring(index + 1, index + 5), 16));
                        } catch (NumberFormatException exception) {
                            out.append("\\u");
                        }
                        index += 4;
                    }
                    break;
                default:
                    out.append(escaped);
            }
        }
        return out.toString();
    }

    static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            switch (current) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (current < 0x20) {
                        out.append(String.format("\\u%04x", (int) current));
                    } else {
                        out.append(current);
                    }
            }
        }
        return out.toString();
    }
}
