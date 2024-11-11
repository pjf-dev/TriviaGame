package ung.csci3660.fall2024.triviagame.game;

import android.os.Parcel;
import android.os.Parcelable;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;

/**
 * Holds configuration settings for a trivia game session, including category, difficulty,
 * question type, number of questions, and time allowed per question.
 * Use the Builder class to create an instance of GameConfig.
 */
public class GameConfig implements Parcelable {
    private final int categoryId;
    private final TriviaQuestion.Difficulty difficulty;
    private final TriviaQuestion.Type questionType;
    private final int numberOfQuestions;
    private final int timePerQuestionSeconds;
    private final int numPlayers;

    private GameConfig(Builder builder) {
        this.categoryId = builder.categoryId;
        this.difficulty = builder.difficulty;
        this.questionType = builder.questionType;
        this.numberOfQuestions = builder.numberOfQuestions;
        this.timePerQuestionSeconds = builder.timePerQuestionSeconds;
        this.numPlayers = builder.numPlayers;
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
        private int numPlayers = 1;

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

        public Builder setNumPlayers(int numPlayers) {
            this.numPlayers = numPlayers;
            return this;
        }

        public GameConfig build() {
            return new GameConfig(this);
        }
    }

    protected GameConfig(Parcel in) {
        categoryId = in.readInt();
        numberOfQuestions = in.readInt();
        timePerQuestionSeconds = in.readInt();
        difficulty = TriviaQuestion.Difficulty.valueOf(in.readString());
        questionType = TriviaQuestion.Type.valueOf(in.readString());
        numPlayers = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(categoryId);
        dest.writeInt(numberOfQuestions);
        dest.writeInt(timePerQuestionSeconds);
        dest.writeString(difficulty.toString());
        dest.writeString(questionType.toString());
        dest.writeInt(numPlayers);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GameConfig> CREATOR = new Creator<GameConfig>() {
        @Override
        public GameConfig createFromParcel(Parcel in) {
            return new GameConfig(in);
        }

        @Override
        public GameConfig[] newArray(int size) {
            return new GameConfig[size];
        }
    };

    // Getters
    public int getCategoryId() { return categoryId; }
    public TriviaQuestion.Difficulty getDifficulty() { return difficulty; }
    public TriviaQuestion.Type getQuestionType() { return questionType; }
    public int getNumberOfQuestions() { return numberOfQuestions; }
    public int getTimePerQuestionSeconds() { return timePerQuestionSeconds; }
    public int getNumPlayers() { return numPlayers; }
}
