package ung.csci3660.fall2024.triviagame.api;

// Fairly self-explanatory
// Record class type added in Java 14
public record TriviaQuestion(ung.csci3660.fall2024.triviagame.api.TriviaQuestion.Type type,
                             ung.csci3660.fall2024.triviagame.api.TriviaQuestion.Difficulty difficulty, String question,
                             String categoryString, String correctAnswer, String[] incorrectAnswers) {

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
