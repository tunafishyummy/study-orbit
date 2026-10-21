package data;

import model.Flashcard;
import model.QuizQuestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuizBuilder {

    private final Random random = new Random();

    public List<QuizQuestion> build(List<Flashcard> cards, String courseCode, int count, String type) {
        List<Flashcard> pool = new ArrayList<Flashcard>();
        for (int index = 0; index < cards.size(); index++) {
            Flashcard card = cards.get(index);
            if (courseCode == null || courseCode.isEmpty() || card.getCourseCode().equals(courseCode)) {
                pool.add(card);
            }
        }
        Collections.shuffle(pool, random);
        if (pool.size() > count) {
            pool = new ArrayList<Flashcard>(pool.subList(0, count));
        }

        List<QuizQuestion> questions = new ArrayList<QuizQuestion>();
        for (int index = 0; index < pool.size(); index++) {
            Flashcard card = pool.get(index);
            if (QuizQuestion.MULTIPLE_CHOICE.equals(type)) {
                questions.add(buildMultipleChoice(card, cards));
            } else if (QuizQuestion.TRUE_FALSE.equals(type)) {
                questions.add(buildTrueFalse(card, cards));
            } else {
                questions.add(buildIdentification(card));
            }
        }
        return questions;
    }

    private QuizQuestion buildIdentification(Flashcard card) {
        return new QuizQuestion(QuizQuestion.IDENTIFICATION, card.getFront(), card.getBack(),
                new ArrayList<String>());
    }

    private QuizQuestion buildMultipleChoice(Flashcard card, List<Flashcard> all) {
        List<String> options = new ArrayList<String>();
        options.add(card.getBack());

        List<Flashcard> others = new ArrayList<Flashcard>(all);
        Collections.shuffle(others, random);
        for (int index = 0; index < others.size(); index++) {
            if (options.size() >= 4) {
                break;
            }
            String back = others.get(index).getBack();
            if (back.equalsIgnoreCase(card.getBack()) == false && options.contains(back) == false) {
                options.add(back);
            }
        }

        Collections.shuffle(options, random);
        return new QuizQuestion(QuizQuestion.MULTIPLE_CHOICE, card.getFront(), card.getBack(), options);
    }

    private QuizQuestion buildTrueFalse(Flashcard card, List<Flashcard> all) {
        boolean showCorrect = random.nextBoolean();
        String shownAnswer = card.getBack();

        if (showCorrect == false && all.size() > 1) {
            List<Flashcard> others = new ArrayList<Flashcard>(all);
            Collections.shuffle(others, random);
            for (int index = 0; index < others.size(); index++) {
                String back = others.get(index).getBack();
                if (back.equalsIgnoreCase(card.getBack()) == false) {
                    shownAnswer = back;
                    break;
                }
            }
        }

        String prompt = card.getFront() + " = " + shownAnswer;
        String answer = "False";
        if (showCorrect) {
            answer = "True";
        }

        List<String> options = new ArrayList<String>();
        options.add("True");
        options.add("False");
        return new QuizQuestion(QuizQuestion.TRUE_FALSE, prompt, answer, options);
    }
}
