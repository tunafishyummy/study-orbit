package ui.pages;

import data.AppData;
import data.NoteManager;
import data.QuizService;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import model.Note;
import model.QuizQuestion;
import ui.Theme;
import ui.components.RoundedTextField;

// Screen 7, quizzes: pick notes, set up a quiz, take it, see the results
public class QuizzesPage extends JPanel {

    private static final Color PILL_LABEL = new Color(0xC62F37);
    private static final Color PILL_NOTE = new Color(0xE26B6B);
    private static final Color GOOD = new Color(0x3FB97A);
    private static final Color BAD = new Color(0xE5484D);
    private static final Color DIVIDER = new Color(0x52525B);
    private static final Color PICKED_FILL = new Color(0x2E2A6B);

    private final NoteManager manager = AppData.get();
    private final QuizService service = new QuizService();
    private final CardLayout views = new CardLayout();
    private final JPanel viewHolder = new JPanel(views);

    // ----- setup view -----
    private final List<Note> selected = new ArrayList<>();
    private final JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final PlusButton plusButton = new PlusButton();
    private final JTextArea previewArea = new JTextArea();
    private final Stepper lengthStepper =
            new Stepper(new String[]{"5 questions", "10 questions", "15 questions", "20 questions"}, 2);
    private final Stepper ambiguityStepper = new Stepper(new String[]{"Low", "Medium", "High"}, 0);
    private final Stepper typeStepper = new Stepper(new String[]{
            QuizQuestion.MULTIPLE_CHOICE, QuizQuestion.TRUE_FALSE, QuizQuestion.IDENTIFICATION}, 0);
    private final Stepper attemptsStepper = new Stepper(new String[]{"1", "2", "3"}, 0);
    private final JButton generateButton = Theme.primaryButton("Generate");
    private boolean generating;

    // ----- quiz view -----
    private final JLabel progressLabel = new JLabel();
    private final JLabel attemptsLabel = new JLabel();
    private final ProgressBar progressBar = new ProgressBar();
    private final JTextArea questionArea = new JTextArea();
    private final JPanel answersPanel = new JPanel();
    private final RoundedTextField answerField = new RoundedTextField();
    private final JTextArea feedbackArea = new JTextArea();
    private final JButton actionButton = Theme.primaryButton("Submit answer");
    private final JButton quitButton = Theme.button("Quit quiz");
    private final List<ChoiceButton> choiceButtons = new ArrayList<>();
    private ButtonGroup choiceGroup = new ButtonGroup();
    private List<QuizQuestion> questions = new ArrayList<>();
    private int current;
    private int maxAttempts = 1;
    private int[] attemptsUsed = new int[0];
    private boolean[] correct = new boolean[0];
    private boolean resolved;

    // ----- results view -----
    private final JPanel resultsHolder = new JPanel(new BorderLayout());

    public QuizzesPage() {
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));
        add(Theme.header("Quizzes"), BorderLayout.NORTH);

        viewHolder.setOpaque(false);
        resultsHolder.setOpaque(false);
        viewHolder.add(buildSetup(), "setup");
        viewHolder.add(buildQuiz(), "quiz");
        viewHolder.add(resultsHolder, "results");
        add(viewHolder, BorderLayout.CENTER);
        views.show(viewHolder, "setup");

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                reconcileNotes();
            }
        });
        refreshSetup();
    }

    // ================= setup =================

    private JPanel buildSetup() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        chipRow.setOpaque(false);
        chipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hint = Theme.muted("Select notes to include in quiz generation");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(chipRow);
        top.add(Box.createVerticalStrut(6));
        top.add(hint);
        panel.add(top, BorderLayout.NORTH);

        plusButton.addActionListener(e -> showNotePicker());
        generateButton.addActionListener(e -> generate());

        JPanel grid = new JPanel(new GridLayout(1, 2, 16, 0));
        grid.setOpaque(false);
        grid.add(previewCard());
        grid.add(settingsCard());
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel previewCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());
        previewArea.setEditable(false);
        previewArea.setFocusable(false);
        previewArea.setOpaque(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setFont(Theme.BODY);
        previewArea.setForeground(Theme.TEXT);
        card.add(Theme.inputScrollPane(previewArea), BorderLayout.CENTER);
        return card;
    }

    private JPanel settingsCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 8));
        JLabel title = new JLabel("Quiz Generation Settings");
        title.setFont(Theme.BODY);
        title.setForeground(Theme.TEXT);
        card.add(title, BorderLayout.NORTH);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        addSetting(column, "Length", lengthStepper);
        addSetting(column, "Ambiguity", ambiguityStepper);
        addSetting(column, "Type", typeStepper);
        addSetting(column, "Attempts per question", attemptsStepper);
        column.add(Box.createVerticalStrut(16));
        generateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(generateButton);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(column);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private void addSetting(JPanel column, String name, Stepper stepper) {
        JLabel label = new JLabel(name, SwingConstants.CENTER);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        stepper.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(label);
        column.add(Box.createVerticalStrut(4));
        column.add(stepper);
        column.add(Box.createVerticalStrut(12));
    }

    // redraws the note chips and the preview, and enables Generate only when a note is chosen
    private void refreshSetup() {
        chipRow.removeAll();
        chipRow.add(new Pill("Orbiting Note:", PILL_LABEL, false));
        for (Note note : selected) {
            Pill chip = new Pill(note.getTitle() + "  \u00D7", PILL_NOTE, true);
            chip.setToolTipText("Click to remove");
            chip.addActionListener(e -> {
                selected.remove(note);
                refreshSetup();
            });
            chipRow.add(chip);
        }
        chipRow.add(plusButton);
        chipRow.revalidate();
        chipRow.repaint();

        StringBuilder preview = new StringBuilder();
        for (Note note : selected) {
            if (preview.length() > 0) preview.append("\n\n");
            preview.append(note.getTitle()).append("\n").append(note.getBody());
        }
        if (selected.isEmpty()) {
            previewArea.setForeground(Theme.MUTED);
            previewArea.setText("Press + to add the notes you want to be quizzed on.");
        } else {
            previewArea.setForeground(Theme.TEXT);
            previewArea.setText(preview.toString());
        }
        previewArea.setCaretPosition(0);
        generateButton.setEnabled(!selected.isEmpty() && !generating);
    }

    // drops notes that were deleted and picks up edits made on the Notes page
    private void reconcileNotes() {
        Map<String, Note> byId = new HashMap<>();
        for (Note n : manager.getAllNotes()) byId.put(n.getId(), n);
        List<Note> fresh = new ArrayList<>();
        for (Note n : selected) {
            Note now = byId.get(n.getId());
            if (now != null) fresh.add(now);
        }
        selected.clear();
        selected.addAll(fresh);
        refreshSetup();
    }

    private boolean isSelected(Note note) {
        for (Note n : selected) if (n.getId().equals(note.getId())) return true;
        return false;
    }

    private void showNotePicker() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.SURFACE);
        menu.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        List<Note> all = manager.getAllNotes();
        List<Note> available = new ArrayList<>();
        for (Note n : all) if (!isSelected(n)) available.add(n);

        if (available.isEmpty()) {
            JMenuItem none = menuItem(all.isEmpty() ? "No notes yet. Create one in the Notes tab." : "All notes are added");
            none.setEnabled(false);
            menu.add(none);
        } else {
            for (Note note : available) {
                String label = note.getTitle() + (note.getCourseCode().isEmpty() ? "" : "  (" + note.getCourseCode() + ")");
                JMenuItem item = menuItem(label);
                item.addActionListener(e -> {
                    selected.add(note);
                    refreshSetup();
                });
                menu.add(item);
            }
        }
        menu.show(plusButton, 0, plusButton.getHeight() + 4);
    }

    private JMenuItem menuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(Theme.BODY);
        item.setOpaque(true);
        item.setBackground(Theme.SURFACE);
        item.setForeground(Theme.TEXT);
        item.setBorder(new EmptyBorder(6, 14, 6, 14));
        return item;
    }

    private void generate() {
        if (selected.isEmpty() || generating) return;
        final List<Note> notes = new ArrayList<>(selected);
        final int count = Integer.parseInt(lengthStepper.value().split(" ")[0]);
        final String ambiguity = ambiguityStepper.value();
        final String type = typeStepper.value();
        final int attempts = Integer.parseInt(attemptsStepper.value());

        generating = true;
        generateButton.setText("Generating...");
        generateButton.setEnabled(false);

        new Thread(() -> {
            try {
                List<QuizQuestion> result = service.generate(notes, count, ambiguity, type);
                SwingUtilities.invokeLater(() -> {
                    doneGenerating();
                    maxAttempts = attempts;
                    startQuiz(result);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    doneGenerating();
                    String reason = ex.getMessage() == null ? ex.toString() : ex.getMessage();
                    if (reason.length() > 300) reason = reason.substring(0, 300) + "...";
                    JOptionPane.showMessageDialog(this,
                            "Could not generate the quiz.\n" + reason
                                    + "\n\nCheck the AI Assistant settings and that the model is running.",
                            "Quiz", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void doneGenerating() {
        generating = false;
        generateButton.setText("Generate");
        refreshSetup();
    }

    // ================= taking the quiz =================

    private JPanel buildQuiz() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 16));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel labels = new JPanel(new BorderLayout());
        labels.setOpaque(false);
        progressLabel.setFont(Theme.HEADING);
        progressLabel.setForeground(Theme.TEXT);
        attemptsLabel.setFont(Theme.BODY);
        attemptsLabel.setForeground(Theme.MUTED);
        labels.add(progressLabel, BorderLayout.WEST);
        labels.add(attemptsLabel, BorderLayout.EAST);
        labels.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(labels);
        top.add(Box.createVerticalStrut(10));
        top.add(progressBar);
        card.add(top, BorderLayout.NORTH);

        styleText(questionArea, Theme.font(Font.BOLD, 18), Theme.TEXT);
        styleText(feedbackArea, Theme.BODY, Theme.MUTED);
        answersPanel.setOpaque(false);
        answersPanel.setLayout(new BoxLayout(answersPanel, BoxLayout.Y_AXIS));
        answerField.setPreferredSize(new Dimension(100, 44));
        answerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        answerField.setAlignmentX(Component.LEFT_ALIGNMENT);
        answerField.addActionListener(e -> onAction());

        JPanel lower = new JPanel(new BorderLayout(0, 12));
        lower.setOpaque(false);
        lower.add(answersPanel, BorderLayout.NORTH);
        lower.add(feedbackArea, BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        body.add(questionArea, BorderLayout.NORTH);
        body.add(lower, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setOpaque(false);
        buttons.add(quitButton, BorderLayout.WEST);
        buttons.add(actionButton, BorderLayout.EAST);
        card.add(buttons, BorderLayout.SOUTH);

        actionButton.addActionListener(e -> onAction());
        quitButton.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Quit this quiz? Your progress will be lost.",
                    "Quit quiz", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) views.show(viewHolder, "setup");
        });
        return card;
    }

    private static void styleText(JTextArea area, Font font, Color color) {
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(null);
        area.setFont(font);
        area.setForeground(color);
    }

    private void startQuiz(List<QuizQuestion> list) {
        questions = list;
        current = 0;
        attemptsUsed = new int[list.size()];
        correct = new boolean[list.size()];
        views.show(viewHolder, "quiz");
        showQuestion();
    }

    private void showQuestion() {
        QuizQuestion q = questions.get(current);
        resolved = false;
        progressLabel.setText("Question " + (current + 1) + " of " + questions.size());
        progressBar.setFraction((double) current / questions.size());
        updateAttemptsLabel();
        questionArea.setText(q.getPrompt());

        answersPanel.removeAll();
        choiceButtons.clear();
        choiceGroup = new ButtonGroup();
        if (q.hasChoices()) {
            boolean letters = QuizQuestion.MULTIPLE_CHOICE.equals(q.getType());
            List<String> choices = q.getChoices();
            for (int i = 0; i < choices.size(); i++) {
                String label = letters ? (char) ('A' + i) + ".   " + choices.get(i) : choices.get(i);
                ChoiceButton button = new ChoiceButton(label);
                button.setAlignmentX(Component.LEFT_ALIGNMENT);
                choiceGroup.add(button);
                choiceButtons.add(button);
                answersPanel.add(button);
                answersPanel.add(Box.createVerticalStrut(8));
            }
        } else {
            answerField.setText("");
            answerField.setEditable(true);
            answersPanel.add(answerField);
            SwingUtilities.invokeLater(answerField::requestFocusInWindow);
        }
        feedbackArea.setText("");
        actionButton.setText("Submit answer");
        answersPanel.revalidate();
        answersPanel.repaint();
    }

    private void updateAttemptsLabel() {
        int left = Math.max(0, maxAttempts - attemptsUsed[current]);
        attemptsLabel.setText("Attempts left: " + left);
    }

    private void onAction() {
        if (resolved) next();
        else submit();
    }

    private int selectedChoice() {
        for (int i = 0; i < choiceButtons.size(); i++) {
            if (choiceButtons.get(i).isSelected()) return i;
        }
        return -1;
    }

    private void submit() {
        QuizQuestion q = questions.get(current);
        boolean ok;
        int pick = -1;
        if (q.hasChoices()) {
            pick = selectedChoice();
            if (pick < 0) {
                showFeedback("Pick an answer first.", Theme.MUTED);
                return;
            }
            ok = q.isCorrectChoice(pick);
        } else {
            String text = answerField.getText().trim();
            if (text.isEmpty()) {
                showFeedback("Type your answer first.", Theme.MUTED);
                return;
            }
            ok = q.isCorrectText(text);
        }
        attemptsUsed[current]++;
        int left = maxAttempts - attemptsUsed[current];

        if (ok) {
            correct[current] = true;
            String extra = q.getExplanation().isEmpty() ? "" : " " + q.getExplanation();
            finishQuestion("Correct!" + extra, GOOD, pick);
        } else if (left > 0) {
            showFeedback("Not quite. " + left + (left == 1 ? " attempt" : " attempts") + " left, try again.", BAD);
            if (pick >= 0) {
                choiceButtons.get(pick).mark(ChoiceButton.WRONG);
                choiceGroup.clearSelection();
            } else {
                answerField.selectAll();
            }
            updateAttemptsLabel();
        } else {
            String extra = q.getExplanation().isEmpty() ? "" : " " + q.getExplanation();
            finishQuestion("Out of attempts. The answer is: " + q.getAnswer() + "." + extra, BAD, pick);
        }
    }

    // locks the question and shows the right answer
    private void finishQuestion(String message, Color color, int pick) {
        resolved = true;
        QuizQuestion q = questions.get(current);
        for (int i = 0; i < choiceButtons.size(); i++) {
            ChoiceButton button = choiceButtons.get(i);
            if (q.isCorrectChoice(i)) button.mark(ChoiceButton.RIGHT);
            else if (i == pick) button.mark(ChoiceButton.WRONG);
            button.setEnabled(false);
        }
        answerField.setEditable(false);
        showFeedback(message, color);
        updateAttemptsLabel();
        actionButton.setText(current + 1 >= questions.size() ? "Finish quiz" : "Next question");
    }

    private void showFeedback(String message, Color color) {
        feedbackArea.setForeground(color);
        feedbackArea.setText(message);
    }

    private void next() {
        if (current + 1 >= questions.size()) {
            progressBar.setFraction(1.0);
            showResults();
            return;
        }
        current++;
        showQuestion();
    }

    // ================= results =================

    private void showResults() {
        int total = questions.size();
        int score = 0;
        for (boolean b : correct) if (b) score++;
        int percent = Math.round(100f * score / total);

        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 14));

        JPanel summary = new JPanel();
        summary.setOpaque(false);
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        JLabel big = new JLabel(score + " / " + total);
        big.setFont(Theme.font(Font.BOLD, 40));
        big.setForeground(Theme.TEXT);
        JLabel message = Theme.muted(percent + "%  -  " + messageFor(percent));
        big.setAlignmentX(Component.LEFT_ALIGNMENT);
        message.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.add(big);
        summary.add(message);
        card.add(summary, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (int i = 0; i < total; i++) list.add(reviewRow(i));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(list, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton newQuiz = Theme.button("New quiz");
        JButton retake = Theme.primaryButton("Retake quiz");
        newQuiz.addActionListener(e -> views.show(viewHolder, "setup"));
        retake.addActionListener(e -> startQuiz(questions));
        buttons.add(newQuiz);
        buttons.add(retake);
        card.add(buttons, BorderLayout.SOUTH);

        resultsHolder.removeAll();
        resultsHolder.add(card, BorderLayout.CENTER);
        resultsHolder.revalidate();
        resultsHolder.repaint();
        views.show(viewHolder, "results");
    }

    private String messageFor(int percent) {
        if (percent >= 90) return "Excellent work!";
        if (percent >= 70) return "Nice job, keep it up.";
        if (percent >= 50) return "Getting there. Review the ones you missed.";
        return "Review your notes and try again.";
    }

    private JPanel reviewRow(int i) {
        QuizQuestion q = questions.get(i);
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, DIVIDER), new EmptyBorder(10, 0, 10, 0)));

        JPanel badgeWrap = new JPanel(new GridBagLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(new Badge(correct[i] ? "Correct" : "Missed", correct[i] ? GOOD : BAD));
        row.add(badgeWrap, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel question = new JLabel(clip((i + 1) + ".  " + q.getPrompt(), 140));
        question.setFont(Theme.font(Font.BOLD, 14));
        question.setForeground(Theme.TEXT);
        int used = attemptsUsed[i];
        JLabel answer = Theme.muted("Answer: " + clip(q.getAnswer(), 100) + "   -   "
                + used + (used == 1 ? " attempt" : " attempts"));
        question.setAlignmentX(Component.LEFT_ALIGNMENT);
        answer.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(question);
        text.add(Box.createVerticalStrut(2));
        text.add(answer);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private static String clip(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    // ================= small components =================

    // left/right arrows around a value, like the Figma settings
    private static class Stepper extends JPanel {
        private final String[] options;
        private int index;
        private final JLabel label = new JLabel("", SwingConstants.CENTER);

        Stepper(String[] options, int start) {
            this.options = options;
            this.index = start;
            setOpaque(false);
            setLayout(new BorderLayout());
            label.setFont(Theme.BODY);
            label.setForeground(Theme.MUTED);
            add(arrow("<", -1), BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
            add(arrow(">", 1), BorderLayout.EAST);
            setPreferredSize(new Dimension(300, 30));
            setMaximumSize(new Dimension(300, 30));
            showValue();
        }

        private JButton arrow(String text, int step) {
            JButton button = new JButton(text);
            button.setFont(Theme.font(Font.BOLD, 16));
            button.setForeground(Theme.MUTED);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setFocusable(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setPreferredSize(new Dimension(36, 30));
            button.addActionListener(e -> {
                index = (index + step + options.length) % options.length;
                showValue();
            });
            return button;
        }

        String value() {
            return options[index];
        }

        private void showValue() {
            label.setText(options[index]);
        }
    }

    // rounded red label / note chip
    private static class Pill extends JButton {
        private final Color fill;

        Pill(String text, Color fill, boolean clickable) {
            super(text);
            this.fill = fill;
            setFont(Theme.font(Font.PLAIN, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            if (clickable) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 28, fm.getHeight() + 10);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // the white "+" next to the note chips
    private static class PlusButton extends JButton {
        PlusButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Add a note");
            setPreferredSize(new Dimension(30, 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.drawLine(cx - 8, cy, cx + 8, cy);
            g2.drawLine(cx, cy - 8, cx, cy + 8);
            g2.dispose();
        }
    }

    private static class Badge extends JComponent {
        private final String text;
        private final Color color;

        Badge(String text, Color color) {
            this.text = text;
            this.color = color;
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(Font.BOLD, 11));
            return new Dimension(fm.stringWidth(text) + 20, fm.getHeight() + 4);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setFont(Theme.font(Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    private static class ProgressBar extends JComponent {
        private double fraction;

        ProgressBar() {
            setPreferredSize(new Dimension(0, 8));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        }

        void setFraction(double fraction) {
            this.fraction = Math.max(0, Math.min(1, fraction));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BORDER);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            int w = (int) Math.round(getWidth() * fraction);
            if (w > 0) {
                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(0, 0, Math.max(w, getHeight()), getHeight(), getHeight(), getHeight());
            }
            g2.dispose();
        }
    }

    // one answer option; turns green or red once graded
    private static class ChoiceButton extends JToggleButton {
        static final int NORMAL = 0;
        static final int RIGHT = 1;
        static final int WRONG = 2;
        private int state = NORMAL;
        private final String fullText;

        ChoiceButton(String text) {
            super(text);
            this.fullText = text;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setFont(Theme.BODY);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(text);
            setPreferredSize(new Dimension(100, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        }

        void mark(int newState) {
            state = newState;
            if (newState == WRONG) setEnabled(false);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color fill = Theme.BG;
            Color outline = Theme.BORDER;
            if (state == RIGHT) {
                fill = new Color(0x1E3A2B);
                outline = GOOD;
            } else if (state == WRONG) {
                fill = new Color(0x3F1F22);
                outline = BAD;
            } else if (isSelected()) {
                fill = PICKED_FILL;
                outline = Theme.PRIMARY;
            } else if (getModel().isRollover() && isEnabled()) {
                fill = new Color(0x363636);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, 16, 16);
            g2.setColor(outline);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String text = fullText;
            int max = w - 36;
            if (fm.stringWidth(text) > max) {
                while (text.length() > 1 && fm.stringWidth(text + "...") > max) {
                    text = text.substring(0, text.length() - 1);
                }
                text = text + "...";
            }
            boolean dim = !isEnabled() && state == NORMAL;
            g2.setColor(dim ? Theme.MUTED : Theme.TEXT);
            g2.drawString(text, 18, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
