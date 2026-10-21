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
        String baseValue = props.getProperty("baseUrl");
        if (baseValue != null && baseValue.isBlank() == false) {
            config.baseUrl = baseValue.trim();
        }
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

    public static String defaultModel(String provider) {
        switch (provider) {
            case GROQ:
                return "llama-3.3-70b-versatile";
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
