package ung.csci3660.fall2024.triviagame.api;

// Fairly self-explanatory
public class TriviaQuestion {

    private final Type type;
    private final Difficulty difficulty;
    private final String question;
    private final String categoryString;
    private final String correctAnswer;
    private final String[] incorrectAnswers;
    private final int categoryID;

    public TriviaQuestion(Type type, Difficulty difficulty, String question, String categoryString, int categoryID, String correctAnswer, String[] incorrectAnswers) {
        this.type = type;
        this.difficulty = difficulty;
        this.question = question;
        this.categoryString = categoryString;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
        this.categoryID = categoryID;
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

    public String getCategoryString() {
        return categoryString;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String[] getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public enum Type {
        BOOLEAN,
        MULTIPLE,
        ANY // Used for query only, never for a question
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD,
        ANY // Used for query only, never for a question
    }

}
