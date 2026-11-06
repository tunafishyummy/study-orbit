package ai;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// provider profile for the AI features, saved to data/ai.properties
public class AiConfig {
    public static final String LOCAL = "local";
    public static final String GROQ = "groq";
    public static final String GEMINI = "gemini";
    public static final String DEEPSEEK = "deepseek";
    public static final String CUSTOM = "custom";

    private static final Path FILE = Path.of("data", "ai.properties");

    private String provider = LOCAL;
    private String baseUrl = defaultBaseUrl(LOCAL);
    private String model = defaultModel(LOCAL);
    private String apiKey = "";
    private boolean demoMode = false;

    public static AiConfig load() {
        AiConfig config = new AiConfig();
        if (!Files.exists(FILE)) {
            return config;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            props.load(in);
        } catch (IOException exception) {
            return config;
        }
        String providerValue = props.getProperty("provider");
        if (providerValue != null && providerValue.isBlank() == false) {
            config.provider = providerValue.trim();
        }
        config.baseUrl = normalizeBaseUrl(config.provider, props.getProperty("baseUrl"));
        String modelValue = props.getProperty("model");
        if (modelValue != null && modelValue.isBlank() == false) {
            config.model = modelValue.trim();
        }
        String keyValue = props.getProperty("apiKey");
        if (keyValue != null) {
            config.apiKey = keyValue.trim();
        }
        config.demoMode = Boolean.parseBoolean(props.getProperty("demoMode", "false"));
        return config;
    }

    public void save() throws IOException {
        Files.createDirectories(FILE.getParent());
        Properties props = new Properties();
        props.setProperty("provider", provider == null ? "" : provider);
        props.setProperty("baseUrl", baseUrl == null ? "" : baseUrl);
        props.setProperty("model", model == null ? "" : model);
        props.setProperty("apiKey", apiKey == null ? "" : apiKey);
        props.setProperty("demoMode", Boolean.toString(demoMode));
        try (OutputStream out = Files.newOutputStream(FILE)) {
            props.store(out, "Study Orbit AI settings, this file stays on this computer");
        }
    }

    public static String defaultBaseUrl(String provider) {
        switch (provider) {
            case GROQ:
                return "https://api.groq.com/openai/v1";
            case GEMINI:
                return "https://generativelanguage.googleapis.com/v1beta/openai";
            case DEEPSEEK:
                return "https://api.deepseek.com/v1";
            case LOCAL:
                return "http://localhost:11434/v1";
            default:
                return "";
        }
    }

    public static String normalizeBaseUrl(String provider, String url) {
        String value = url == null ? "" : url.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.isEmpty()) {
            return defaultBaseUrl(provider);
        }
        if (CUSTOM.equals(provider)) {
            return value;
        }
        int scheme = value.indexOf("://");
        int firstSlash = value.indexOf('/', scheme < 0 ? 0 : scheme + 3);
        if (firstSlash < 0) {
            return defaultBaseUrl(provider);
        }
        return value;
    }

    public static String defaultModel(String provider) {
        switch (provider) {
            case GROQ:
                return "openai/gpt-oss-120b";
            case GEMINI:
                return "gemini-2.0-flash";
            case DEEPSEEK:
                return "deepseek-chat";
            case LOCAL:
                return "qwen2.5:7b";
            default:
                return "";
        }
    }

    public boolean supportsVision() {
        if (demoMode) {
            return false;
        }
        String current = model == null ? "" : model.toLowerCase();
        if (GEMINI.equals(provider)) {
            return true;
        }
        if (DEEPSEEK.equals(provider)) {
            return false;
        }
        if (LOCAL.equals(provider)) {
            return current.contains("vision")
                    || current.contains("vl")
                    || current.contains("llava")
                    || current.contains("minicpm-v")
                    || current.contains("moondream")
                    || current.contains("gemma3")
                    || current.contains("llama4")
                    || current.contains("llama-4");
        }
        if (GROQ.equals(provider)) {
            return current.contains("vision")
                    || current.contains("llama-4")
                    || current.contains("llama4")
                    || current.contains("scout")
                    || current.contains("maverick");
        }
        return CUSTOM.equals(provider);
    }

    public String visionHint() {
        if (GEMINI.equals(provider)) {
            return "Gemini can read images.";
        }
        if (DEEPSEEK.equals(provider)) {
            return "DeepSeek chat cannot read images. Use Local Ollama with qwen2.5vl:7b or Gemini.";
        }
        if (LOCAL.equals(provider)) {
            return "Pull a vision model first: ollama pull qwen2.5vl:7b, then set the model above.";
        }
        if (GROQ.equals(provider)) {
            return "Use a vision model that Groq offers in your account.";
        }
        return "Use a model that accepts image input.";
    }

    public boolean isLocal() {
        return LOCAL.equals(provider);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider == null || provider.isBlank() ? LOCAL : provider;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }
}
