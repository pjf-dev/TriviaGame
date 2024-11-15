package ung.csci3660.fall2024.triviagame.game;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;

/**
 * Core game session handler that manages state, questions, and results
 * Acts as central coordinator between API and game components
 */
public class GameSession implements Parcelable {
    private final List<TriviaQuestion> questions;
    private final GameConfig config;
    private final GameResult gameResult;

    private int questionIndex;
    private int playerIndex;

    /**
     * Initialize GameSession object providing a config and initial list of questions
     * @param config {@link GameConfig} object representing the game configuration
     * @param questionsInit Initial {@link List} of {@link TriviaQuestion}s
     */
    public GameSession(GameConfig config, List<TriviaQuestion> questionsInit) {
        this.config = config;
        this.questions = new ArrayList<>();
        this.gameResult = new GameResult(config.getNumPlayers());

        this.questionIndex = 0;
        this.playerIndex = 0;

        this.questions.addAll(questionsInit);
    }

    // Parcelable init implementation
    protected GameSession(Parcel in) {
        questions = in.createTypedArrayList(TriviaQuestion.CREATOR);
        config = in.readParcelable(GameConfig.class.getClassLoader());
        gameResult = in.readParcelable(GameResult.class.getClassLoader());
        questionIndex = in.readInt();
        playerIndex = in.readInt();
    }

    // Parcelable implementation
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(questions);
        dest.writeParcelable(config, flags);
        dest.writeParcelable(gameResult, flags);
        dest.writeInt(questionIndex);
        dest.writeInt(playerIndex);
    }

    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable implementation
    public static final Creator<GameSession> CREATOR = new Creator<>() {
        @Override
        public GameSession createFromParcel(Parcel in) {
            return new GameSession(in);
        }

        @Override
        public GameSession[] newArray(int size) {
            return new GameSession[size];
        }
    };

    /**
     * Loads questions from TriviaQuestion list into game session
     * @param questions {@link List<TriviaQuestion>} to load into {@link GameSession} question list
     */
    public void loadQuestions(List<TriviaQuestion> questions) {
        this.questions.addAll(questions);
    }

    /**
     * Records answer results and updates game statistics
     * @param correct Is the provided answer correct?
     * @param responseTimeMillis Time it took for question response in milliseconds
     * @return {@link GameResult} running game result
     */
    public GameResult recordAnswer(boolean correct, long responseTimeMillis) {
        gameResult.addAnswer(playerIndex, correct, calculatePoints(correct, responseTimeMillis, getCurrentQuestion().difficulty()));
        return gameResult;
    }

    /**
     * Question navigation methods
     * @return {@link Boolean} true if another question exists, false otherwise
     */
    public boolean hasNextQuestion() {
        return questionIndex < questions.size() - 1;
    }

    /**
     * Utility method for checking if next question exists and incrementing index if true
     */
    public void moveToNext() {
        if (hasNextQuestion()) questionIndex++;
    }

    /**
     * Moves to the next question, retrieving the question
     * @return next {@link TriviaQuestion} in question list
     */
    public TriviaQuestion nextQuestion() {
        moveToNext();
        return getCurrentQuestion();
    }

    /**
     * Increment the playerIndex counter to the next player
     * @return Next player's index (starting from 0)
     */
    public int nextPlayer() {
        playerIndex = playerIndex == config.getNumPlayers()-1 ? 0 : playerIndex + 1;
        return playerIndex;
    }

    /*
    Getters for game state and progress
     */
    public TriviaQuestion getCurrentQuestion() { return questions.get(questionIndex); }
    public int getCurrentQuestionIndex() { return questionIndex; }
    public int getCurrentPlayerIndex() { return playerIndex; }
    public int getTotalQuestions() { return questions.size(); }
    public GameConfig getConfig() { return config; }
    public GameResult getGameResult() { return gameResult; }

    /**
     * Calculates points based on correctness, time, and difficulty
     *
     * @param correct Is the answer correct
     * @param responseTimeMs How many milliseconds the response took
     * @param difficulty Question's difficulty
     */
    private int calculatePoints(boolean correct, long responseTimeMs,
                                TriviaQuestion.Difficulty difficulty) {
        if (!correct) return 0;

        // Calculate base points (100-1000) from response time
        double timeRatio = 1.0 - (responseTimeMs /
                (config.getTimePerQuestionSeconds() * 1000.0));
        int basePoints = (int) (900 * timeRatio + 100);

        // Add difficulty bonus
        switch (difficulty) {
            case HARD: return basePoints + 300;
            case MEDIUM: return basePoints + 150;
            default: return basePoints;
        }
    }
}