package ui.pages;

import ai.AiClient;
import ai.AiConfig;
import data.AppData;
import data.SettingsManager;
import logic.BackupService;
import model.Profile;
import ui.StudyOrbitApp;
import ui.Theme;
import ui.components.Avatar;
import ui.components.CardIcons;
import ui.components.NativeFileDialog;
import ui.components.RoundedComboBox;
import ui.components.RoundedPanel;
import ui.components.RoundedTextField;
import ui.components.ToggleSwitch;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Screen 11, settings and accessibility
public class SettingsPage extends JPanel {

    private JComboBox<String> providerBox;
    private JTextField baseUrlField;
    private JTextField modelField;
    private JPasswordField apiKeyField;
    private ToggleSwitch demoToggle;
    private JButton testButton;
    private JLabel aiStatusLabel;
    private boolean loadingAiFields;

    public SettingsPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));

        add(Theme.header("Settings"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.42;
        c.weighty = 0.4;
        c.insets = new Insets(0, 0, 16, 16);
        grid.add(profileCard(), c);

        c.gridx = 1;
        c.weightx = 0.58;
        c.insets = new Insets(0, 0, 16, 0);
        grid.add(appearanceCard(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.42;
        c.weighty = 0.25;
        c.insets = new Insets(0, 0, 16, 16);
        grid.add(notificationsCard(), c);

        c.gridx = 1;
        c.weightx = 0.58;
        c.insets = new Insets(0, 0, 16, 0);
        grid.add(studyCard(), c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.35;
        c.insets = new Insets(0, 0, 16, 0);
        grid.add(aiAssistantCard(), c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.weightx = 0.42;
        c.weighty = 0.15;
        c.insets = new Insets(0, 0, 0, 16);
        grid.add(dataCard(), c);

        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = 0.58;
        c.insets = new Insets(0, 0, 0, 0);
        grid.add(helpCard(), c);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel profileCard() {
        JPanel card = card("Profile");

        Profile profile = AppData.profiles().getActiveProfile();
        String name = "No profile";
        Color color = Theme.PRIMARY;
        if (profile != null) {
            name = profile.getName();
            color = Theme.parse(profile.getAvatarColor());
        }

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel who = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        who.setOpaque(false);
        who.add(new Avatar(color));

        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(name);
        label.setFont(Theme.HEADING);
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = Theme.muted("Local profile");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        names.add(label);
        names.add(sub);
        who.add(names);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setOpaque(false);
        JButton switchButton = Theme.button("Switch profile");
        switchButton.addActionListener(e -> switchProfile());
        JButton newButton = Theme.button("New profile");
        newButton.addActionListener(e -> newProfile());
        JButton renameButton = Theme.button("Rename");
        renameButton.addActionListener(e -> renameProfile());
        JButton deleteButton = Theme.button("Delete");
        deleteButton.addActionListener(e -> deleteProfile());
        buttons.add(switchButton);
        buttons.add(newButton);
        buttons.add(renameButton);
        buttons.add(deleteButton);

        body.add(who, BorderLayout.CENTER);
        body.add(buttons, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel appearanceCard() {
        JPanel card = card("Appearance");

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        ToggleSwitch largeToggle = new ToggleSwitch(
                AppData.settings().getBoolean(SettingsManager.LARGE_TEXT, false));
        largeToggle.setOnChange(() -> {
            try {
                boolean value = largeToggle.isOn();
                AppData.settings().setBoolean(SettingsManager.LARGE_TEXT, value);
                Theme.setLargeText(value);
                rebuild();
            } catch (IOException exception) {
                showError(exception);
            }
        });

        JPanel row = row("Large Text", "Make the app easier to read");
        row.add(switchHolder(largeToggle), BorderLayout.EAST);
        body.add(row);
        body.add(Box.createVerticalStrut(8));
        body.add(line());
        body.add(Box.createVerticalStrut(8));

        body.add(row("Time format", "12 hour or 24 hour clock"));
        body.add(Box.createVerticalStrut(6));

        JComboBox<String> timeBox = new RoundedComboBox<>(new String[] { "12-hour", "24-hour" });
        if ("24".equals(AppData.settings().get(SettingsManager.TIME_FORMAT, "12"))) {
            timeBox.setSelectedItem("24-hour");
        }
        timeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        timeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeBox.addActionListener(e -> {
            String value = "12";
            if ("24-hour".equals(timeBox.getSelectedItem())) {
                value = "24";
            }
            try {
                AppData.settings().set(SettingsManager.TIME_FORMAT, value);
                Theme.setTime24("24".equals(value));
                rebuild();
            } catch (IOException exception) {
                showError(exception);
            }
        });
        body.add(timeBox);
        body.add(Box.createVerticalGlue());

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel notificationsCard() {
        JPanel card = card("Notifications");

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        ToggleSwitch deadlineToggle = new ToggleSwitch(
                AppData.settings().getBoolean(SettingsManager.DEADLINE_REMINDERS, true));
        deadlineToggle.setOnChange(() -> saveToggle(SettingsManager.DEADLINE_REMINDERS, deadlineToggle.isOn()));

        ToggleSwitch reviewToggle = new ToggleSwitch(
                AppData.settings().getBoolean(SettingsManager.DAILY_REVIEW_REMINDERS, true));
        reviewToggle.setOnChange(() -> saveToggle(SettingsManager.DAILY_REVIEW_REMINDERS, reviewToggle.isOn()));

        JPanel deadlineRow = row("Deadline reminders", "Get notified on upcoming deadlines");
        deadlineRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        deadlineRow.add(switchHolder(deadlineToggle), BorderLayout.EAST);

        JPanel reviewRow = row("Daily review reminder", "Get notified when you have not studied today");
        reviewRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        reviewRow.add(switchHolder(reviewToggle), BorderLayout.EAST);

        body.add(deadlineRow);
        body.add(line());
        body.add(reviewRow);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel aiAssistantCard() {
        JPanel card = card("AI Assistant", Theme.HEADER_BLUE, CardIcons.spark());

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(14, 24, 12, 24));

        AiConfig config = AiConfig.load();

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints f = new GridBagConstraints();
        f.fill = GridBagConstraints.HORIZONTAL;
        f.insets = new Insets(0, 0, 8, 8);

        providerBox = new RoundedComboBox<>(new String[] { "Local Ollama", "Groq", "Gemini", "DeepSeek", "Custom" });
        providerBox.setFont(Theme.BODY);
        providerBox.setSelectedItem(providerLabel(config.getProvider()));

        baseUrlField = new RoundedTextField();
        baseUrlField.setText(config.getBaseUrl());
        baseUrlField.setFont(Theme.BODY);

        modelField = new RoundedTextField();
        modelField.setText(config.getModel());
        modelField.setFont(Theme.BODY);

        apiKeyField = new JPasswordField(config.getApiKey());
        apiKeyField.setFont(Theme.BODY);
        apiKeyField.setForeground(Theme.TEXT);
        apiKeyField.setBackground(Theme.SURFACE);
        apiKeyField.setCaretColor(Theme.TEXT);
        apiKeyField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        apiKeyField.setEnabled(config.isLocal() == false);

        loadingAiFields = true;
        providerBox.addActionListener(e -> {
            if (loadingAiFields) {
                return;
            }
            String key = providerKey((String) providerBox.getSelectedItem());
            baseUrlField.setText(AiConfig.defaultBaseUrl(key));
            modelField.setText(AiConfig.defaultModel(key));
            apiKeyField.setEnabled(AiConfig.LOCAL.equals(key) == false);
        });
        loadingAiFields = false;

        f.gridx = 0;
        f.gridy = 0;
        f.weightx = 0;
        f.gridwidth = 1;
        form.add(fieldLabel("Provider"), f);
        f.gridx = 1;
        f.weightx = 0.4;
        form.add(providerBox, f);
        f.gridx = 2;
        f.weightx = 0;
        form.add(fieldLabel("Model"), f);
        f.gridx = 3;
        f.weightx = 0.6;
        form.add(modelField, f);

        f.gridx = 0;
        f.gridy = 1;
        f.weightx = 0;
        f.gridwidth = 1;
        form.add(fieldLabel("Base URL"), f);
        f.gridx = 1;
        f.gridwidth = 3;
        f.weightx = 1;
        form.add(baseUrlField, f);

        f.gridx = 0;
        f.gridy = 2;
        f.weightx = 0;
        f.gridwidth = 1;
        form.add(fieldLabel("API Key"), f);
        f.gridx = 1;
        f.gridwidth = 3;
        form.add(apiKeyField, f);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);

        JPanel demoRow = new JPanel(new BorderLayout(10, 0));
        demoRow.setOpaque(false);
        JPanel demoTexts = new JPanel();
        demoTexts.setOpaque(false);
        demoTexts.setLayout(new BoxLayout(demoTexts, BoxLayout.Y_AXIS));
        JLabel demoTitle = new JLabel("Demo mode");
        demoTitle.setFont(Theme.HEADING);
        demoTitle.setForeground(Theme.TEXT);
        demoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel demoCaption = Theme.muted("Uses sample content instead of a model, for presentations without wifi.");
        demoCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        demoTexts.add(demoTitle);
        demoTexts.add(demoCaption);
        demoToggle = new ToggleSwitch(config.isDemoMode());
        demoRow.add(demoTexts, BorderLayout.CENTER);
        demoRow.add(switchHolder(demoToggle), BorderLayout.EAST);
        center.add(demoRow, BorderLayout.CENTER);

        body.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton saveButton = Theme.primaryButton("Save Settings");
        saveButton.addActionListener(e -> saveAiSettings());
        testButton = Theme.button("Test Connection");
        testButton.addActionListener(e -> testAiConnection());
        aiStatusLabel = Theme.muted("The key stays on this computer in data/ai.properties.");
        actions.add(saveButton);
        actions.add(testButton);
        actions.add(aiStatusLabel);
        body.add(actions, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.MUTED);
        return label;
    }

    private String providerKey(String label) {
        if ("Groq".equals(label)) {
            return AiConfig.GROQ;
        }
        if ("Gemini".equals(label)) {
            return AiConfig.GEMINI;
        }
        if ("DeepSeek".equals(label)) {
            return AiConfig.DEEPSEEK;
        }
        if ("Custom".equals(label)) {
            return AiConfig.CUSTOM;
        }
        return AiConfig.LOCAL;
    }

    private String providerLabel(String key) {
        if (AiConfig.GROQ.equals(key)) {
            return "Groq";
        }
        if (AiConfig.GEMINI.equals(key)) {
            return "Gemini";
        }
        if (AiConfig.DEEPSEEK.equals(key)) {
            return "DeepSeek";
        }
        if (AiConfig.CUSTOM.equals(key)) {
            return "Custom";
        }
        return "Local Ollama";
    }

    private AiConfig configFromFields() {
        AiConfig config = AiConfig.load();
        config.setProvider(providerKey((String) providerBox.getSelectedItem()));
        config.setBaseUrl(baseUrlField.getText().trim());
        config.setModel(modelField.getText().trim());
        config.setApiKey(new String(apiKeyField.getPassword()).trim());
        return config;
    }

    private void saveAiSettings() {
        try {
            AiConfig config = configFromFields();
            config.setDemoMode(demoToggle != null && demoToggle.isOn());
            config.save();
            aiStatusLabel.setText(config.isDemoMode() ? "Saved with demo mode on." : "Saved.");
        } catch (IOException exception) {
            aiStatusLabel.setText("Could not save: " + exception.getMessage());
        }
    }

    private void testAiConnection() {
        testButton.setEnabled(false);
        aiStatusLabel.setText("Checking " + baseUrlField.getText().trim() + "...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return new AiClient(configFromFields()).isAvailable();
            }

            @Override
            protected void done() {
                testButton.setEnabled(true);
                try {
                    boolean reachable = get();
                    aiStatusLabel.setText(reachable
                            ? "Reachable. The AI features are ready."
                            : "Not reachable. Start the local model or check the address and key.");
                } catch (Exception exception) {
                    aiStatusLabel.setText("Could not test: " + exception.getMessage());
                }
            }
        }.execute();
    }

    private JPanel helpCard() {
        JPanel card = card("Getting started");

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        body.add(Theme.muted("1. Add your courses in Courses."));
        body.add(Box.createVerticalStrut(6));
        body.add(Theme.muted("2. Write notes and turn them into flashcards."));
        body.add(Box.createVerticalStrut(6));
        body.add(Theme.muted("3. Take a quiz to review, and finish it to grow your streak."));
        body.add(Box.createVerticalStrut(6));
        body.add(Theme.muted("4. Add your class times and deadlines in Schedule."));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel studyCard() {
        JPanel card = card("Study");

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        body.add(row("Weekly goal", "Days you want to study each week"));
        body.add(Box.createVerticalStrut(8));

        JComboBox<Integer> goalBox = new RoundedComboBox<>(new Integer[] { 3, 4, 5, 6, 7 });
        goalBox.setSelectedItem(AppData.settings().getInt(SettingsManager.WEEKLY_GOAL, 5));
        goalBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        goalBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        goalBox.addActionListener(e -> {
            Integer value = (Integer) goalBox.getSelectedItem();
            if (value == null) {
                return;
            }
            try {
                AppData.settings().setInt(SettingsManager.WEEKLY_GOAL, value);
            } catch (IOException exception) {
                showError(exception);
            }
        });
        body.add(goalBox);
        body.add(Box.createVerticalGlue());

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel dataCard() {
        JPanel card = card("Data");

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        body.add(row("Export backup", "Save all data to a json file"));
        body.add(Box.createVerticalStrut(10));

        JButton export = Theme.primaryButton("Export all data");
        export.setAlignmentX(Component.LEFT_ALIGNMENT);
        export.addActionListener(e -> exportBackup());
        body.add(export);
        body.add(Box.createVerticalGlue());

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void exportBackup() {
        NativeFileDialog fileDialog = new NativeFileDialog();
        File file = fileDialog.save("Export backup", "JSON file", new String[] { "json" },
                "study-orbit-backup.json");
        if (file == null) {
            return;
        }
        try {
            new BackupService().export(file);
            JOptionPane.showMessageDialog(this, "Backup saved to " + file.getName());
        } catch (IOException exception) {
            showError(exception);
        }
    }

    private void renameProfile() {
        Profile profile = AppData.profiles().getActiveProfile();
        if (profile == null) {
            newProfile();
            return;
        }
        String name = JOptionPane.showInputDialog(this, "Profile name", profile.getName());
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        try {
            AppData.profiles().updateProfile(profile, name.trim(), profile.getAvatarColor());
            rebuild();
        } catch (IOException exception) {
            showError(exception);
        }
    }

    private void deleteProfile() {
        Profile profile = AppData.profiles().getActiveProfile();
        if (profile == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete " + profile.getName() + "?",
                "Delete profile", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            AppData.profiles().deleteProfile(profile);
            rebuild();
        } catch (IOException exception) {
            showError(exception);
        }
    }

    private void switchProfile() {
        List<Profile> profiles = AppData.profiles().getProfiles();
        if (profiles.isEmpty()) {
            newProfile();
            return;
        }

        String[] names = new String[profiles.size()];
        for (int index = 0; index < profiles.size(); index++) {
            names[index] = profiles.get(index).getName();
        }

        String choice = (String) JOptionPane.showInputDialog(this, "Choose a profile", "Switch profile",
                JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
        if (choice == null) {
            return;
        }

        for (int index = 0; index < profiles.size(); index++) {
            Profile profile = profiles.get(index);
            if (profile.getName().equals(choice)) {
                try {
                    AppData.profiles().setActiveProfile(profile);
                    rebuild();
                } catch (IOException exception) {
                    showError(exception);
                }
                return;
            }
        }
    }

    private void newProfile() {
        String name = JOptionPane.showInputDialog(this, "Profile name");
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        List<String> usedColors = new ArrayList<String>();
        List<Profile> existing = AppData.profiles().getProfiles();
        for (int index = 0; index < existing.size(); index++) {
            usedColors.add(existing.get(index).getAvatarColor());
        }
        Color color = Theme.nextCourseColor(usedColors);

        try {
            Profile profile = AppData.profiles().addProfile(name.trim(), Theme.toHex(color));
            AppData.profiles().setActiveProfile(profile);
            rebuild();
        } catch (IOException exception) {
            showError(exception);
        }
    }

    private void saveToggle(String key, boolean value) {
        try {
            AppData.settings().setBoolean(key, value);
        } catch (IOException exception) {
            showError(exception);
        }
    }

    private void rebuild() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof StudyOrbitApp) {
            ((StudyOrbitApp) window).rebuild();
        }
    }

    private void showError(IOException exception) {
        JOptionPane.showMessageDialog(this, "Could not save: " + exception.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel card(String title) {
        JPanel card = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JPanel header = new RoundedTopPanel(Theme.PRIMARY);
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.HEADING);
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel);

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JPanel card(String title, Color headerColor, Icon icon) {
        JPanel card = new RoundedPanel(Theme.SURFACE, Theme.CORNER_RADIUS, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JPanel header = new RoundedTopPanel(headerColor);
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        if (icon != null) {
            header.add(new JLabel(icon));
        }
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.HEADING);
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel);

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private static class RoundedTopPanel extends JPanel {
        private final Color fill;

        RoundedTopPanel(Color fill) {
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            int corner = Theme.CORNER_RADIUS / 2;
            g2.fillRoundRect(0, 0, getWidth(), getHeight() + Theme.CORNER_RADIUS,
                    Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
            g2.fillRect(0, corner, getWidth(), getHeight() - corner);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel row(String title, String caption) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(title);
        name.setFont(Theme.HEADING);
        name.setForeground(Theme.TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        texts.add(name);
        if (caption != null) {
            JLabel desc = Theme.muted(caption);
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            texts.add(desc);
        }
        row.add(texts, BorderLayout.CENTER);
        return row;
    }

    private JPanel switchHolder(ToggleSwitch toggle) {
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        holder.setOpaque(false);
        holder.add(toggle);
        return holder;
    }

    private JPanel line() {
        JPanel line = new JPanel();
        line.setBackground(Theme.BORDER);
        line.setPreferredSize(new Dimension(1, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return line;
    }
}
