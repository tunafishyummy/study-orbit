package ai;

import model.Flashcard;
import model.QuizQuestion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// turns a document into flashcards, quiz questions or a note
public class StudyGenerator {
    public interface Progress {
        void update(String message);
    }

    private static final int CHUNK_SIZE = 6000;
    private static final int MAX_CARDS_PER_PART = 10;
    private static final int MAX_QUESTIONS_PER_PART = 5;

    private static final String CARD_SYSTEM =
            "You turn study material into flashcards.\n"
            + "Rules:\n"
            + "- One flashcard per line, exactly in the format: front :: back\n"
            + "- The front is a short question that names the term, like What is encapsulation? or What does inheritance allow? The bare term like Encapsulation is also fine. Never copy the full sentence into the front\n"
            + "- The back holds the material's exact sentence for that term, copied as is. Do not rename, paraphrase or shorten it. Exams and quizzes use the same wording\n"
            + "- Every card must read on its own: never start the back with a pronoun like they, it, this or these. Name the term instead, like Inheritance allows... not They allow..., while the rest of the words stay as in the material\n"
            + "- The back can be one or two full sentences from the material when the definition needs it\n"
            + "- Use only facts that are in the material\n"
            + "- No numbering, no bullets, no markdown, no extra text\n"
            + "- Write at most " + MAX_CARDS_PER_PART + " flashcards\n"
            + "Example lines:\n"
            + "What is encapsulation? :: Encapsulation is the wrapping of data and methods into a single unit called a class\n"
            + "What does inheritance allow? :: Inheritance allows a new class to acquire the properties and methods of an existing class\n"
            + "Method overloading :: Two methods in the same class share a name but have different parameters";

    private static final String QUIZ_SYSTEM =
            "You write multiple choice quiz questions from study material.\n"
            + "Rules:\n"
            + "- One question per line, exactly in the format: question :: option A :: option B :: option C :: option D :: answer\n"
            + "- The answer must repeat one of the four options exactly\n"
            + "- Keep the material's exact words for the question and for every option. Do not rename or paraphrase terms. Real exams use the same wording\n"
            + "- Questions and options must read on their own: name the term instead of using a pronoun like they, it, this or these\n"
            + "- Use only facts that are in the material\n"
            + "- No numbering, no bullets, no markdown, no extra text\n"
            + "- Write at most " + MAX_QUESTIONS_PER_PART + " questions\n"
            + "Example line: Which keyword makes a class inherit another? :: extends :: implements :: inherits :: superclass :: extends";

    private static final String NOTE_SYSTEM =
            "You write one short study note from study material.\n"
            + "Rules:\n"
            + "- First line: a short title of a few words\n"
            + "- Then one blank line\n"
            + "- Then the body: keep the material's content in its own words for the key parts. Do not rename terms and do not shorten definitions. You may drop filler and fix line breaks, but keep the exact sentences that matter (term and definition lines, lists, steps)\n"
            + "- Use only facts that are in the material\n"
            + "- No markdown headings, no numbering\n"
            + "Reply in exactly this shape:\n"
            + "Encapsulation and Inheritance\n"
            + "\n"
            + "Fields stay private and change only through methods. Inheritance lets one class reuse another class. Polymorphism lets the same method call behave differently depending on the object.";

    private final AiClient client;
    private final AiConfig config;

    public StudyGenerator(AiClient client, AiConfig config) {
        this.client = client;
        this.config = config;
    }

    public List<Flashcard> suggestFlashcards(String document, String deckTitle, Progress progress) throws IOException {
        if (config.isDemoMode()) {
            return demoFlashcards(deckTitle);
        }
        List<String> parts = splitDocument(document);
        List<Flashcard> cards = new ArrayList<Flashcard>();
        Set<String> seen = new LinkedHashSet<String>();
        for (int index = 0; index < parts.size(); index++) {
            progress.update("Generating flashcards... part " + (index + 1) + " of " + parts.size());
            String answer = client.complete(CARD_SYSTEM, "Study material:\n\n" + parts.get(index) + "\n\nCreate the flashcards now.");
            String[] lines = answer.split("\n");
            for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
                String line = lines[lineIndex];
                int at = line.indexOf("::");
                if (at < 0) {
                    continue;
                }
                String front = clean(line.substring(0, at));
                String back = clean(line.substring(at + 2));
                front = shortFront(front, back);
                if (front.isEmpty() || back.isEmpty()) {
                    continue;
                }
                if (front.length() > 400 || back.length() > 1500) {
                    continue;
                }
                if (seen.add(front.toLowerCase())) {
                    cards.add(new Flashcard(deckTitle, front, back, new ArrayList<String>()));
                }
            }
        }
        return cards;
    }

    public List<QuizQuestion> suggestQuiz(String document, String source, Progress progress) throws IOException {
        if (config.isDemoMode()) {
            return demoQuiz(source);
        }
        List<String> parts = splitDocument(document);
        List<QuizQuestion> questions = new ArrayList<QuizQuestion>();
        Set<String> seen = new LinkedHashSet<String>();
        for (int index = 0; index < parts.size(); index++) {
            progress.update("Generating quiz questions... part " + (index + 1) + " of " + parts.size());
            String answer = client.complete(QUIZ_SYSTEM, "Study material:\n\n" + parts.get(index) + "\n\nWrite the quiz questions now.");
            String[] lines = answer.split("\n");
            for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
                String[] fields = lines[lineIndex].split("\\s*::\\s*");
                if (fields.length < 6) {
                    continue;
                }
                String question = clean(fields[0]);
                List<String> options = new ArrayList<String>();
                for (int field = 1; field <= 4; field++) {
                    options.add(clean(fields[field]));
                }
                String right = clean(fields[5]);
                if (question.isEmpty() || right.isEmpty()) {
                    continue;
                }
                boolean blank = false;
                for (int option = 0; option < options.size(); option++) {
                    if (options.get(option).isEmpty()) {
                        blank = true;
                    }
                }
                if (blank) {
                    continue;
                }
                if (seen.add(question.toLowerCase())) {
                    questions.add(new QuizQuestion(question, options, right, source));
                }
            }
        }
        return questions;
    }

    public String[] suggestNote(String document, Progress progress) throws IOException {
        if (config.isDemoMode()) {
            return demoNote();
        }
        progress.update("Writing a note from the document...");
        String answer = client.complete(NOTE_SYSTEM, "Study material:\n\n" + document + "\n\nWrite the note now.");

        List<String> lines = new ArrayList<String>();
        String[] rawLines = answer.split("\n");
        for (int index = 0; index < rawLines.length; index++) {
            String trimmed = rawLines[index].trim();
            if (trimmed.isEmpty() == false) {
                lines.add(trimmed);
            }
        }
        if (lines.isEmpty()) {
            return new String[] { "Imported note", "" };
        }

        String title = clean(lines.get(0));
        StringBuilder bodyBuilder = new StringBuilder();
        for (int index = 1; index < lines.size(); index++) {
            if (bodyBuilder.length() > 0) {
                bodyBuilder.append("\n");
            }
            bodyBuilder.append(lines.get(index));
        }
        String body = bodyBuilder.toString();

        if (body.isEmpty() || title.length() > 90) {
            String text = title + (body.isEmpty() ? "" : "\n" + body);
            int cut = firstSentenceEnd(text);
            if (cut > 20 && text.length() - cut > 60) {
                title = clean(text.substring(0, cut));
                body = text.substring(cut).trim();
            } else if (title.length() > 90) {
                int space = title.lastIndexOf(' ', 80);
                if (space < 40) {
                    space = 80;
                }
                body = (title.substring(space).trim() + (body.isEmpty() ? "" : "\n" + body)).trim();
                title = title.substring(0, space).trim();
            }
        }
        if (body.isEmpty()) {
            body = answer.trim();
        }
        if (title.isEmpty()) {
            title = "Imported note";
        }
        return new String[] { title, body };
    }

    private int firstSentenceEnd(String text) {
        for (int index = 30; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '.' || current == ':' || current == '?') {
                return index + 1;
            }
        }
        return -1;
    }

    private List<String> splitDocument(String document) {
        List<String> parts = new ArrayList<String>();
        String text = document == null ? "" : document.trim();
        while (text.length() > CHUNK_SIZE) {
            int cut = text.lastIndexOf("\n\n", CHUNK_SIZE);
            if (cut < CHUNK_SIZE / 2) {
                cut = text.lastIndexOf('\n', CHUNK_SIZE);
            }
            if (cut < CHUNK_SIZE / 2) {
                cut = CHUNK_SIZE;
            }
            parts.add(text.substring(0, cut).trim());
            text = text.substring(cut).trim();
        }
        if (text.isEmpty() == false) {
            parts.add(text);
        }
        return parts;
    }

    private String clean(String text) {
        String out = text.trim();
        while (out.startsWith("-") || out.startsWith("*") || out.startsWith("#")) {
            out = out.substring(1).trim();
        }
        if (out.startsWith("Title:")) {
            out = out.substring(6).trim();
        }
        while (out.endsWith("::")) {
            out = out.substring(0, out.length() - 2).trim();
        }
        return out;
    }

    private String shortFront(String front, String back) {
        boolean tooLong = front.length() > 90;
        if (tooLong == false && front.equalsIgnoreCase(back) == false) {
            return front;
        }
        String lower = front.toLowerCase();
        String[] separators = { " is ", " are ", " means ", " allows ", " refers ", " occurs ", " happens ", " uses ", " needs ", ":" };
        int cut = -1;
        for (int index = 0; index < separators.length; index++) {
            int at = lower.indexOf(separators[index]);
            if (at > 1 && at < 70 && (cut < 0 || at < cut)) {
                cut = at;
            }
        }
        String candidate;
        if (cut > 1) {
            candidate = front.substring(0, cut).trim();
        } else {
            String[] words = front.split(" ");
            int count = words.length;
            if (count > 8) {
                count = 8;
            }
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < count; index++) {
                if (index > 0) {
                    builder.append(' ');
                }
                builder.append(words[index]);
            }
            candidate = builder.toString().trim();
        }
        if (candidate.length() < 2) {
            return front;
        }
        return candidate;
    }

    private List<Flashcard> demoFlashcards(String deckTitle) {
        List<Flashcard> cards = new ArrayList<Flashcard>();
        cards.add(new Flashcard(deckTitle, "What does OOP stand for?", "Object Oriented Programming", new ArrayList<String>()));
        cards.add(new Flashcard(deckTitle, "What is a class?", "A blueprint that describes the fields and methods its objects have", new ArrayList<String>()));
        cards.add(new Flashcard(deckTitle, "What is an object?", "A single instance created from a class", new ArrayList<String>()));
        cards.add(new Flashcard(deckTitle, "What is inheritance?", "A class reusing the fields and methods of another class", new ArrayList<String>()));
        cards.add(new Flashcard(deckTitle, "What does the Java keyword extends do?", "Makes one class inherit from another class", new ArrayList<String>()));
        cards.add(new Flashcard(deckTitle, "What is encapsulation?", "Keeping fields private and changing them only through methods", new ArrayList<String>()));
        cards.add(new Flashcard(deckTitle, "What is polymorphism?", "The same method name behaving differently for different object types", new ArrayList<String>()));
        cards.add(new Flashcard(deckTitle, "What is an abstract class?", "A class that cannot be created directly and is meant to be extended", new ArrayList<String>()));
        return cards;
    }

    private List<QuizQuestion> demoQuiz(String source) {
        List<QuizQuestion> questions = new ArrayList<QuizQuestion>();

        List<String> first = new ArrayList<String>();
        first.add("extends");
        first.add("implements");
        first.add("inherits");
        first.add("superclass");
        questions.add(new QuizQuestion("Which Java keyword makes a class inherit another class?", first, "extends", source));

        List<String> second = new ArrayList<String>();
        second.add("A blueprint for objects");
        second.add("A running program");
        second.add("A method that returns nothing");
        second.add("A package of classes");
        questions.add(new QuizQuestion("What is a class?", second, "A blueprint for objects", source));

        List<String> third = new ArrayList<String>();
        third.add("It hides a field behind methods");
        third.add("It copies a class");
        third.add("It deletes an object");
        third.add("It makes all fields public");
        questions.add(new QuizQuestion("What does encapsulation mean in OOP?", third, "It hides a field behind methods", source));

        List<String> fourth = new ArrayList<String>();
        fourth.add("private");
        fourth.add("public");
        fourth.add("static");
        fourth.add("final");
        questions.add(new QuizQuestion("Which modifier keeps a field visible only inside its own class?", fourth, "private", source));

        List<String> fifth = new ArrayList<String>();
        fifth.add("Object");
        fifth.add("Class");
        fifth.add("Method");
        fifth.add("Package");
        questions.add(new QuizQuestion("What is created when you write new Student()?", fifth, "Object", source));
        return questions;
    }

    private String[] demoNote() {
        return new String[] { "Object Oriented Programming Summary",
                "A class is a blueprint and an object is one instance of that class.\n"
                + "Inheritance lets a class reuse the fields and methods of another class.\n"
                + "Encapsulation keeps fields private and exposes them through methods.\n"
                + "Polymorphism lets the same method call behave differently for different object types." };
    }
}
