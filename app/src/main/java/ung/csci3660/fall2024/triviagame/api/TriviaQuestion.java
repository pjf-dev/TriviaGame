package ung.csci3660.fall2024.triviagame.api;

// TODO: Add Documentation
public class TriviaQuestion {

    private final Type type;
    private final Difficulty difficulty;
    private final String question;
    private final String category; // TODO: Maybe make custom Category class
    private final String correctAnswer;
    private final String incorrectAnswers;

    public TriviaQuestion(Type type, Difficulty difficulty, String question, String category, String correctAnswer, String incorrectAnswers) {
        this.type = type;
        this.difficulty = difficulty;
        this.question = question;
        this.category = category;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
    }

    public Type getType() {
        return type;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public String getCategory() {
        return category;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public enum Type {
        BOOLEAN,
        MULTIPLE
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

}
