package data;

import ai.AiClient;
import ai.AiConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Note;
import model.QuizQuestion;

// turns notes or a document into quiz questions through the configured ai provider
public class QuizService {
    private static final int MAX_NOTE_CHARS = 12000;

    public List<QuizQuestion> generate(List<Note> notes, int count, String ambiguity, String type) throws Exception {
        StringBuilder material = new StringBuilder();
        for (int index = 0; index < notes.size(); index++) {
            Note note = notes.get(index);
            material.append("## ").append(note.getTitle()).append("\n").append(note.getBody()).append("\n\n");
        }
        return generateFromDocument(material.toString(), count, ambiguity, type);
    }

    public List<QuizQuestion> generateFromDocument(String document, int count, String ambiguity, String type)
            throws Exception {
        String text = document == null ? "" : document.trim();
        if (text.length() > MAX_NOTE_CHARS) {
            text = text.substring(0, MAX_NOTE_CHARS);
        }
        if (text.isEmpty()) {
            throw new IllegalArgumentException("There is nothing to make a quiz from yet.");
        }

        AiConfig config = AiConfig.load();
        if (config.isDemoMode()) {
            return demoQuestions(count, type);
        }

        AiClient client = new AiClient(config);
        if (client.isAvailable() == false) {
            if (config.isLocal()) {
                throw new IOException("The local AI is not running. Start Ollama first with \"ollama run "
                        + config.getModel() + "\", or pick another provider in Settings.");
            }
            throw new IOException("Could not reach " + config.getBaseUrl()
                    + ". Check the internet connection and the key in Settings.");
        }

        String prompt = "Write a quiz of exactly " + count + " questions based ONLY on the notes below.\n"
                + "Question type: " + type + ".\n"
                + "Difficulty / ambiguity: " + ambiguityRule(ambiguity) + "\n\n"
                + formatRule(type) + "\n"
                + "Every object also has an 'explanation' key: one short sentence on why the answer is right.\n"
                + "Respond STRICTLY with a raw JSON array. No Markdown, no code blocks.\n\n"
                + "NOTES:\n" + text;

        String raw = client.complete("You write quizzes as strict JSON. Reply with a raw JSON array only.", prompt);

        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start < 0 || end <= start) {
            throw new IOException("The AI reply was not a quiz. Try again.");
        }
        JsonArray array = JsonParser.parseString(raw.substring(start, end + 1)).getAsJsonArray();

        List<QuizQuestion> result = new ArrayList<QuizQuestion>();
        for (int index = 0; index < array.size(); index++) {
            JsonElement element = array.get(index);
            try {
                QuizQuestion question = parseQuestion(element.getAsJsonObject(), type);
                if (question != null) {
                    result.add(question);
                }
            } catch (RuntimeException skipBadQuestion) {
                // ignore one malformed question and keep the rest
            }
        }
        if (result.isEmpty()) {
            throw new IOException("No usable questions came back. Try again.");
        }
        if (result.size() > count) {
            result = new ArrayList<QuizQuestion>(result.subList(0, count));
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

    private QuizQuestion parseQuestion(JsonObject object, String type) {
        String question = str(object, "question");
        if (question.isEmpty()) {
            return null;
        }
        String explanation = str(object, "explanation");

        if (QuizQuestion.IDENTIFICATION.equals(type)) {
            String answer = str(object, "answer");
            if (answer.isEmpty()) {
                return null;
            }
            return new QuizQuestion(type, question, new ArrayList<String>(), -1, answer, explanation);
        }

        List<String> choices = new ArrayList<String>();
        int index = -1;
        if (object.has("answerIndex") && object.get("answerIndex").isJsonNull() == false) {
            index = object.get("answerIndex").getAsInt();
        }
        if (QuizQuestion.TRUE_FALSE.equals(type)) {
            choices.add("True");
            choices.add("False");
            if (index < 0 || index > 1) {
                String answer = str(object, "answer").toLowerCase();
                if (answer.startsWith("t")) {
                    index = 0;
                } else if (answer.startsWith("f")) {
                    index = 1;
                } else {
                    index = -1;
                }
            }
        } else {
            if (object.has("choices") == false || object.get("choices").isJsonArray() == false) {
                return null;
            }
            JsonArray array = object.getAsJsonArray("choices");
            for (int choice = 0; choice < array.size(); choice++) {
                choices.add(array.get(choice).getAsString().trim());
            }
            if (choices.size() < 2) {
                return null;
            }
        }
        if (index < 0 || index >= choices.size()) {
            return null;
        }
        return new QuizQuestion(type, question, choices, index, "", explanation);
    }

    private String str(JsonObject object, String key) {
        if (object.has(key) == false || object.get(key).isJsonNull()) {
            return "";
        }
        return object.get(key).getAsString().trim();
    }

    private List<QuizQuestion> demoQuestions(int count, String type) {
        List<QuizQuestion> questions = new ArrayList<QuizQuestion>();
        if (QuizQuestion.IDENTIFICATION.equals(type)) {
            questions.add(identification("What keyword creates an object?", "new",
                    "The new keyword allocates an object from a class."));
            questions.add(identification("What OOP concept hides fields behind methods?", "Encapsulation",
                    "Encapsulation keeps fields private and exposes methods instead."));
            questions.add(identification("What is the parent class called?", "Superclass",
                    "The class being inherited from is the superclass."));
            questions.add(identification("What keyword makes a class inherit another class?", "extends",
                    "The extends keyword sets up inheritance between two classes."));
            questions.add(identification("What is one instance of a class called?", "Object",
                    "An object is a single instance created from a class."));
        } else if (QuizQuestion.TRUE_FALSE.equals(type)) {
            questions.add(trueFalse("A class can extend more than one class in Java.", 1,
                    "Java allows only one superclass per class."));
            questions.add(trueFalse("Encapsulation keeps fields private and exposes methods.", 0,
                    "Encapsulation protects the fields behind methods."));
            questions.add(trueFalse("An object is an instance of a class.", 0,
                    "Objects are created from a class blueprint."));
            questions.add(trueFalse("The private modifier makes a field visible everywhere.", 1,
                    "Private keeps the field inside its own class."));
            questions.add(trueFalse("Inheritance lets a class reuse another class.", 0,
                    "A subclass inherits the fields and methods of its superclass."));
        } else {
            questions.add(multiple("Which Java keyword makes a class inherit another class?",
                    new String[] { "extends", "implements", "inherits", "superclass" }, 0,
                    "The extends keyword creates the inheritance link."));
            questions.add(multiple("What is a class?",
                    new String[] { "A blueprint for objects", "A running program", "A method that returns nothing",
                            "A package of classes" }, 0,
                    "A class describes the fields and methods its objects will have."));
            questions.add(multiple("What does encapsulation hide?",
                    new String[] { "Fields behind methods", "Classes", "Packages", "Imports" }, 0,
                    "Encapsulation keeps fields private and changes them through methods."));
            questions.add(multiple("Which modifier keeps a field visible only inside its own class?",
                    new String[] { "private", "public", "static", "final" }, 0,
                    "The private modifier limits access to the declaring class."));
            questions.add(multiple("What is created when you write new Student()?",
                    new String[] { "An object", "A class", "A method", "A package" }, 0,
                    "The new keyword creates one object from the Student class."));
        }

        if (questions.size() > count) {
            return new ArrayList<QuizQuestion>(questions.subList(0, count));
        }
        return questions;
    }

    private QuizQuestion multiple(String prompt, String[] choices, int correctIndex, String explanation) {
        List<String> options = new ArrayList<String>();
        for (int index = 0; index < choices.length; index++) {
            options.add(choices[index]);
        }
        return new QuizQuestion(QuizQuestion.MULTIPLE_CHOICE, prompt, options, correctIndex, "", explanation);
    }

    private QuizQuestion trueFalse(String prompt, int correctIndex, String explanation) {
        List<String> options = new ArrayList<String>();
        options.add("True");
        options.add("False");
        return new QuizQuestion(QuizQuestion.TRUE_FALSE, prompt, options, correctIndex, "", explanation);
    }

    private QuizQuestion identification(String prompt, String answer, String explanation) {
        return new QuizQuestion(QuizQuestion.IDENTIFICATION, prompt, new ArrayList<String>(), -1, answer, explanation);
    }
}
