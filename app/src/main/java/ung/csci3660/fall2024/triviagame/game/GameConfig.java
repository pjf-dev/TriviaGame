package ung.csci3660.fall2024.triviagame.game;

import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;

/**
 * Holds configuration settings for a trivia game session, including category, difficulty,
 * question type, number of questions, and time allowed per question.
 * Use the Builder class to create an instance of GameConfig.
 */
public class GameConfig {
    private final int categoryId;
    private final TriviaQuestion.Difficulty difficulty;
    private final TriviaQuestion.Type questionType;
    private final int numberOfQuestions;
    private final int timePerQuestionSeconds;

    private GameConfig(Builder builder) {
        this.categoryId = builder.categoryId;
        this.difficulty = builder.difficulty;
        this.questionType = builder.questionType;
        this.numberOfQuestions = builder.numberOfQuestions;
        this.timePerQuestionSeconds = builder.timePerQuestionSeconds;
    }

    /**
     * Builder for creating a GameConfig instance with custom settings.
     */
    public static class Builder {
        private int categoryId = -1;  // Default: any category
        private TriviaQuestion.Difficulty difficulty = TriviaQuestion.Difficulty.ANY; // Default: any difficulty
        private TriviaQuestion.Type questionType = TriviaQuestion.Type.ANY; // Default: any type
        private int numberOfQuestions = 10; // Default: 10 questions
        private int timePerQuestionSeconds = 30; // Default: 30 seconds per question

        public Builder setCategory(int categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder setDifficulty(TriviaQuestion.Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder setQuestionType(TriviaQuestion.Type type) {
            this.questionType = type;
            return this;
        }

        public Builder setNumberOfQuestions(int count) {
            this.numberOfQuestions = count;
            return this;
        }

        public Builder setTimePerQuestion(int seconds) {
            this.timePerQuestionSeconds = seconds;
            return this;
        }

        public GameConfig build() {
            return new GameConfig(this);
        }
    }

    // Getters
    public int getCategoryId() { return categoryId; }
    public TriviaQuestion.Difficulty getDifficulty() { return difficulty; }
    public TriviaQuestion.Type getQuestionType() { return questionType; }
    public int getNumberOfQuestions() { return numberOfQuestions; }
    public int getTimePerQuestionSeconds() { return timePerQuestionSeconds; }
}
