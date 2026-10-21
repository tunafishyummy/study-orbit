package ui.pages;

import ai.AiClient;
import ai.AiConfig;
import ai.StudyGenerator;
import data.AppData;
import data.NoteManager;
import data.QuizBuilder;
import model.Course;
import model.Flashcard;
import model.QuizQuestion;
import ui.ImportDocumentDialog;
import ui.Theme;
import ui.components.ProcessingCard;
import ui.components.RoundedComboBox;
import ui.components.RoundedTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Screen 7, build and take a quiz from the flashcards
public class QuizzesPage extends JPanel {

    private static final String ALL_COURSES = "All courses";

    private final NoteManager manager = AppData.get();
    private final QuizBuilder builder = new QuizBuilder();

    private final JComboBox<String> courseBox = new RoundedComboBox<>();
    private final JComboBox<Integer> countBox = new RoundedComboBox<>(new Integer[] { 5, 10, 15 });
    private final JComboBox<String> typeBox = new RoundedComboBox<>(new String[] {
            QuizQuestion.MULTIPLE_CHOICE, QuizQuestion.IDENTIFICATION, QuizQuestion.TRUE_FALSE
    });

    private final CardLayout viewLayout = new CardLayout();
    private final JPanel viewPanel = new JPanel(viewLayout);

    private final JLabel progressLabel = new JLabel();
    private final JTextArea promptArea = new JTextArea();
    private final JPanel optionsPanel = new JPanel();
    private final JTextField answerField = new RoundedTextField();
    private final JLabel feedbackLabel = new JLabel(" ");
    private final JButton actionButton = Theme.primaryButton("Check answer");
    private final JLabel resultLabel = new JLabel();
    private JLabel aiStatusLabel;
    private ProcessingCard processingCard;

    private List<QuizQuestion> questions = new ArrayList<QuizQuestion>();
    private int index;
    private int score;
    private boolean answered;
    private String chosenAnswer;

    public QuizzesPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(0, 24, 24, 24));

        add(Theme.header("Quizzes"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 16, 16));
        body.setOpaque(false);
        body.add(buildSetupCard());
        body.add(buildQuizCard());
        add(body, BorderLayout.CENTER);

        reload();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                reload();
            }
        });
    }

    private JPanel buildSetupCard() {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Create a quiz");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(16));

        card.add(label("Course"));
        card.add(Box.createVerticalStrut(4));
        courseBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        courseBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(courseBox);
        card.add(Box.createVerticalStrut(12));

        card.add(label("Number of questions"));
        card.add(Box.createVerticalStrut(4));
        countBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        countBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(countBox);
        card.add(Box.createVerticalStrut(12));

        card.add(label("Question type"));
        card.add(Box.createVerticalStrut(4));
        typeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(typeBox);
        card.add(Box.createVerticalStrut(20));

        JButton start = Theme.primaryButton("Start quiz");
        start.setAlignmentX(Component.LEFT_ALIGNMENT);
        start.addActionListener(e -> startQuiz());
        card.add(start);
        card.add(Box.createVerticalStrut(8));

        JButton aiButton = Theme.button("Generate with AI");
        aiButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiButton.addActionListener(e -> generateWithAi(aiButton));
        card.add(aiButton);
        card.add(Box.createVerticalStrut(6));

        aiStatusLabel = Theme.muted("Or import a document and let the AI write the questions.");
        aiStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(aiStatusLabel);

        processingCard = new ProcessingCard("Writing quiz questions");
        processingCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        processingCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        card.add(processingCard);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildQuizCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());

        viewPanel.setOpaque(false);
        viewPanel.add(buildIntro(), "intro");
        viewPanel.add(buildQuestion(), "question");
        viewPanel.add(buildResult(), "result");
        card.add(viewPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildIntro() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.add(Theme.muted("Choose your settings and start a quiz."));
        return panel;
    }

    private JPanel buildQuestion() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        progressLabel.setFont(Theme.BODY);
        progressLabel.setForeground(Theme.MUTED);
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(progressLabel);
        panel.add(Box.createVerticalStrut(12));

        promptArea.setEditable(false);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setOpaque(false);
        promptArea.setFont(Theme.font(Font.BOLD, 20));
        promptArea.setForeground(Theme.TEXT);
        promptArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        promptArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        panel.add(promptArea);
        panel.add(Box.createVerticalStrut(14));

        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(optionsPanel);
        panel.add(Box.createVerticalStrut(10));

        answerField.setAlignmentX(Component.LEFT_ALIGNMENT);
        answerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(answerField);
        panel.add(Box.createVerticalStrut(10));

        feedbackLabel.setFont(Theme.BODY);
        feedbackLabel.setForeground(Theme.MUTED);
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(feedbackLabel);
        panel.add(Box.createVerticalStrut(10));

        actionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButton.addActionListener(e -> handleAction());
        panel.add(actionButton);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildResult() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        resultLabel.setFont(Theme.font(Font.BOLD, 28));
        resultLabel.setForeground(Theme.TEXT);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(resultLabel);
        content.add(Box.createVerticalStrut(16));

        JButton again = Theme.primaryButton("Try again");
        again.setAlignmentX(Component.CENTER_ALIGNMENT);
        again.addActionListener(e -> startQuiz());
        content.add(again);

        panel.add(content);
        return panel;
    }

    private void reload() {
        Object previous = courseBox.getSelectedItem();
        courseBox.removeAllItems();
        courseBox.addItem(ALL_COURSES);
        for (int index = 0; index < manager.getCourses().size(); index++) {
            Course course = manager.getCourses().get(index);
            courseBox.addItem(course.getCode());
        }
        if (previous != null) {
            courseBox.setSelectedItem(previous);
        }
    }

    private void generateWithAi(JButton source) {
        String document = ImportDocumentDialog.show(this);
        if (document == null) {
            return;
        }
        source.setEnabled(false);
        processingCard.start("Reading your document...");

        new SwingWorker<List<QuizQuestion>, String>() {
            @Override
            protected List<QuizQuestion> doInBackground() throws Exception {
                AiConfig config = AiConfig.load();
                AiClient client = new AiClient(config);
                if (config.isDemoMode() == false && client.isAvailable() == false) {
                    if (config.isLocal()) {
                        throw new IOException("The local AI is not running. Start Ollama first with \"ollama run "
                                + config.getModel() + "\", or pick another provider in Settings.");
                    }
                    throw new IOException("Could not reach " + config.getBaseUrl()
                            + ". Check the internet connection and the key in Settings.");
                }
                StudyGenerator generator = new StudyGenerator(client, config);
                return generator.suggestQuiz(document, "Imported document", message -> publish(message));
            }

            @Override
            protected void process(List<String> updates) {
                processingCard.update(updates.get(updates.size() - 1));
            }

            @Override
            protected void done() {
                source.setEnabled(true);
                processingCard.stop();
                try {
                    List<QuizQuestion> result = get();
                    if (result.isEmpty()) {
                        aiStatusLabel.setText("The AI did not find clear questions. Try a clearer document.");
                        return;
                    }
                    questions = result;
                    index = 0;
                    score = 0;
                    aiStatusLabel.setText("Generated " + result.size() + " questions.");
                    viewLayout.show(viewPanel, "question");
                    showQuestion();
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
                    aiStatusLabel.setText("Generation failed.");
                    JOptionPane.showMessageDialog(QuizzesPage.this, cause.getMessage(),
                            "The AI could not finish", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void startQuiz() {
        List<Flashcard> cards = manager.getFlashcards();
        if (cards.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add flashcards first, then build a quiz.");
            return;
        }

        String course = (String) courseBox.getSelectedItem();
        if (course == null || course.equals(ALL_COURSES)) {
            course = "";
        }
        int count = (Integer) countBox.getSelectedItem();
        String type = (String) typeBox.getSelectedItem();

        questions = builder.build(cards, course, count, type);
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No flashcards match that course yet.");
            return;
        }

        index = 0;
        score = 0;
        viewLayout.show(viewPanel, "question");
        showQuestion();
    }

    private void showQuestion() {
        answered = false;
        chosenAnswer = null;
        feedbackLabel.setText(" ");
        feedbackLabel.setForeground(Theme.MUTED);
        optionsPanel.removeAll();
        answerField.setText("");

        QuizQuestion question = questions.get(index);
        progressLabel.setText("Question " + (index + 1) + " of " + questions.size());
        promptArea.setText(question.getPrompt());

        boolean needsOptions = QuizQuestion.MULTIPLE_CHOICE.equals(question.getType())
                || QuizQuestion.TRUE_FALSE.equals(question.getType());
        answerField.setVisible(needsOptions == false);

        if (needsOptions) {
            List<String> options = question.getOptions();
            for (int optionIndex = 0; optionIndex < options.size(); optionIndex++) {
                final String option = options.get(optionIndex);
                JButton optionButton = Theme.button(option);
                optionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                optionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                optionButton.addActionListener(e -> chooseAnswer(option));
                optionsPanel.add(optionButton);
                optionsPanel.add(Box.createVerticalStrut(6));
            }
        }

        actionButton.setText("Check answer");
        actionButton.setEnabled(needsOptions == false);
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void chooseAnswer(String answer) {
        if (answered) {
            return;
        }
        chosenAnswer = answer;
        feedbackLabel.setText("Selected: " + answer);
        feedbackLabel.setForeground(Theme.MUTED);
        actionButton.setEnabled(true);
    }

    private void handleAction() {
        if (answered) {
            index = index + 1;
            if (index >= questions.size()) {
                showResult();
            } else {
                showQuestion();
            }
            return;
        }

        QuizQuestion question = questions.get(index);
        String answer = chosenAnswer;
        if (QuizQuestion.IDENTIFICATION.equals(question.getType())) {
            answer = answerField.getText();
        }
        if (answer == null || answer.trim().isEmpty()) {
            feedbackLabel.setText("Choose or type an answer first.");
            feedbackLabel.setForeground(Theme.MUTED);
            return;
        }

        answered = true;
        if (question.isCorrect(answer)) {
            score = score + 1;
            feedbackLabel.setText("Correct");
            feedbackLabel.setForeground(new Color(0x4ADE80));
        } else {
            feedbackLabel.setText("Wrong. Answer: " + question.getCorrectAnswer());
            feedbackLabel.setForeground(new Color(0xF87171));
        }
        actionButton.setText("Next");
        actionButton.setEnabled(true);
    }

    private void showResult() {
        resultLabel.setText("You scored " + score + " / " + questions.size());
        try {
            AppData.activity().recordQuiz(questions.size());
        } catch (java.io.IOException exception) {
            // keep the result visible even if the activity log fails
        }
        viewLayout.show(viewPanel, "result");
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
