package ung.csci3660.fall2024.triviagame.game;

import android.os.Parcel;
import android.os.Parcelable;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;

import java.util.ArrayList;
import java.util.List;

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

    public GameSession(GameConfig config, List<TriviaQuestion> questionsInit) {
        this.config = config;
        this.questions = new ArrayList<>();
        this.gameResult = new GameResult(config.getNumPlayers());

        this.questionIndex = 0;
        this.playerIndex = 0;

        this.questions.addAll(questionsInit);
    }

    protected GameSession(Parcel in) {
        questions = in.createTypedArrayList(TriviaQuestion.CREATOR);
        config = in.readParcelable(GameConfig.class.getClassLoader());
        gameResult = in.readParcelable(GameResult.class.getClassLoader());
        questionIndex = in.readInt();
        playerIndex = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(questions);
        dest.writeParcelable(config, flags);
        dest.writeParcelable(gameResult, flags);
        dest.writeInt(questionIndex);
        dest.writeInt(playerIndex);
    }

    @Override
    public int describeContents() {
        return 0;
    }

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
     */
    public void loadQuestions(List<TriviaQuestion> questions) {
        this.questions.addAll(questions);
    }

    /**
     * Records answer results and updates game statistics
     */
    public GameResult recordAnswer(boolean correct, long responseTimeMillis) {
        gameResult.addAnswer(playerIndex, correct, calculatePoints(correct, responseTimeMillis, getCurrentQuestion().difficulty()));
        return gameResult;
    }

    /**
     * Question navigation methods
     */
    public boolean hasNextQuestion() {
        return questionIndex < questions.size() - 1;
    }

    public void moveToNext() {
        if (hasNextQuestion()) questionIndex++;
    }
    public TriviaQuestion nextQuestion() {
        moveToNext();
        return getCurrentQuestion();
    }

    public int nextPlayer() {
        playerIndex = playerIndex == config.getNumPlayers()-1 ? 0 : playerIndex + 1;
        return playerIndex;
    }

    /**
     * Getters for game state and progress
     */
    public TriviaQuestion getCurrentQuestion() { return questions.get(questionIndex); }
    public int getCurrentQuestionIndex() { return questionIndex; }
    public int getCurrentPlayerIndex() { return playerIndex; }
    public int getTotalQuestions() { return questions.size(); }
    public GameConfig getConfig() { return config; }
    public GameResult getGameResult() { return gameResult; }

    /**
     * Calculates points based on correctness, time, and difficulty
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