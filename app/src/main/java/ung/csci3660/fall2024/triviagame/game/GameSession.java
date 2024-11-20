package ung.csci3660.fall2024.triviagame.game;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ung.csci3660.fall2024.triviagame.GameActivity;
import ung.csci3660.fall2024.triviagame.api.TriviaCallback;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;
import ung.csci3660.fall2024.triviagame.api.TriviaResponse;

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
     * Records answer results and updates game statistics | ONLY FOR CLASSIC MODE
     * @param correct Is the provided answer correct?
     * @param responseTimeMillis Time it took for question response in milliseconds
     * @return {@link GameResult} running game result
     */
    public GameResult recordAnswer(boolean correct, long responseTimeMillis) {
        if (config.getGameMode().equals(GameConfig.Mode.Infinity))
            throw new UnsupportedOperationException("Cannot call GameSession#recordAnswer(boolean correct, long responseTimeMillis) in Infinity mode.");
        gameResult.addAnswer(playerIndex, correct, calculatePoints(correct, responseTimeMillis, getCurrentQuestion().difficulty()));
        return gameResult;
    }

    /**
     * Records answer results and updates game statistics | ONLY FOR INFINITY MODE
     * @param correct Is the provided answer correct?
     * @return {@link GameResult} running game result
     */
    public GameResult recordAnswer(boolean correct) {
        if (config.getGameMode().equals(GameConfig.Mode.Classic))
            throw new UnsupportedOperationException("Cannot call GameSession#recordAnswer(boolean correct) in Classic mode.");
        gameResult.addAnswer(playerIndex, correct, correct ? 0 : 1);
        return gameResult;
    }

    /**
     * Get the count of players remaining in the game | Used for Infinity mode
     * @return count of players remaining
     */
    public int getPlayersRemaining() {
        if (config.getGameMode().equals(GameConfig.Mode.Infinity)) {
            return (int) gameResult.getScores().stream().filter((s) -> s < config.getStrikes()).count();
        } else {
            // Classic mode doesn't eliminate, so return total player count
            return config.getNumPlayers();
        }
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
        if (config.getGameMode().equals(GameConfig.Mode.Infinity)
                && questions.size() - questionIndex == 3) {
            System.out.println("We're loading questions!!!");
            /* Start to load more questions
             We can do this here instead of the in GameActivity because the UI doesn't
                care about the result and the method is called from the UI */
            GameActivity.getQuestions(new TriviaCallback<>() {
                @Override
                public void onSuccess(TriviaResponse<List<TriviaQuestion>> response) {
                    // Load questions into game session
                    loadQuestions(response.data);
                    System.out.println("Questions loaded!!!");
                }

                @Override
                public void onError(TriviaResponse<Void> response, IOException e) {
                    // TODO: Do something on error... Dialog??
                }
            }, config);

        }
        return getCurrentQuestion();
    }

    /**
     * Increment the playerIndex counter to the next player
     * @return Next player's index (starting from 0)
     */
    public int nextPlayer() {
        int startIndex = playerIndex == config.getNumPlayers()-1 ? 0 : playerIndex + 1;
        if (config.getGameMode().equals(GameConfig.Mode.Infinity)) {
            for (int i = 0; i < config.getNumPlayers(); i++) {
                // Simple way to roll over player count using modulo
                int testIndex = (startIndex + i) % config.getNumPlayers();
                if (gameResult.getTotalAnswered(testIndex)
                    - gameResult.getCorrectAnswers(testIndex) < 3) {
                    // Player isn't eliminated
                    playerIndex = testIndex;
                    return playerIndex;
                }
            }
        } else {
            playerIndex = startIndex;
            return playerIndex;
        }
        return -1; // No players left, game over
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