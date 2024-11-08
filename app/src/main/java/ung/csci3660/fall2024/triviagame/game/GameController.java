package ung.csci3660.fall2024.triviagame.game;

import ung.csci3660.fall2024.triviagame.api.*;
import java.util.*;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * Main controller for the trivia game.
 * Handles game flow, scoring, and category management.
 */
public class GameController {
    private final GameSession session;
    private final QuestionTimer timer;
    private Map<String, Integer> availableCategories;

    public GameController() {
        this.session = new GameSession();
        this.timer = new QuestionTimer();
        this.availableCategories = new HashMap<>();
    }

    /**
     * Loads category list from API for game setup
     */
    public void loadCategories(CategoryLoadCallback callback) {
        TriviaAPI.getInstance().initializeCategories(new TriviaCallback<Map<String, Integer>>() {
            @Override
            public void onSuccess(TriviaResponse<Map<String, Integer>> response) {
                availableCategories = response.data;
                callback.onCategoriesLoaded(response.data);
            }

            @Override
            public void onError(TriviaResponse<Void> response, IOException e) {
                callback.onError("Failed to load categories");
            }
        }, false);
    }

    /**
     * Initializes game with selected configuration
     * @return true if initialization successful
     */
    public boolean initialize(GameConfig config) {
        if (session.getState() != GameResult.GameState.INITIALIZING) return false;
        return session.loadQuestions(config);
    }

    /**
     * Starts game if ready
     * @return true if game started successfully
     */
    public boolean startGame() {
        if (session.getState() != GameResult.GameState.READY) return false;
        session.setState(GameResult.GameState.IN_PROGRESS);
        startCurrentQuestion();
        return true;
    }

    /**
     * Processes player's answer and calculates score
     * @return GameResult containing score and correctness
     */
    public GameResult submitAnswer(String answer) {
        if (session.getState() != GameResult.GameState.IN_PROGRESS) return null;

        long responseTime = timer.getElapsedTime();
        TriviaQuestion current = session.getCurrentQuestion();
        boolean correct = answer.equals(current.correctAnswer());

        int points = calculatePoints(correct, responseTime, current.difficulty());
        timer.stop();

        return session.recordAnswer(correct, points, responseTime);
    }

    /**
     * Advances to next question if available
     * @return false if no more questions
     */
    public boolean moveToNextQuestion() {
        if (session.getState() != GameResult.GameState.IN_PROGRESS) return false;

        if (session.hasNextQuestion()) {
            session.moveToNext();
            startCurrentQuestion();
            return true;
        } else {
            session.setState(GameResult.GameState.FINISHED);
            return false;
        }
    }

    /**
     * Starts timer for current question
     */
    private void startCurrentQuestion() {
        timer.start(session.getConfig().getTimePerQuestionSeconds(), () -> {
            session.setState(GameResult.GameState.FINISHED);
        });
    }

    /**
     * Calculates points based on correctness, time, and difficulty
     */
    private int calculatePoints(boolean correct, long responseTimeMs,
                                TriviaQuestion.Difficulty difficulty) {
        if (!correct) return 0;

        // Calculate base points (100-1000) from response time
        double timeRatio = 1.0 - (responseTimeMs /
                (session.getConfig().getTimePerQuestionSeconds() * 1000.0));
        int basePoints = (int) (900 * timeRatio + 100);

        // Add difficulty bonus
        switch (difficulty) {
            case HARD: return basePoints + 300;
            case MEDIUM: return basePoints + 150;
            default: return basePoints;
        }
    }

    // UI Getters
    public Map<String, Integer> getAvailableCategories() {
        return new HashMap<>(availableCategories);
    }

    public TriviaQuestion getCurrentQuestion() {
        return session.getCurrentQuestion();
    }

    public int getCurrentQuestionIndex() {
        return session.getCurrentIndex();
    }

    public int getTotalQuestions() {
        return session.getTotalQuestions();
    }

    public GameResult.GameState getState() {
        return session.getState();
    }

    public GameResult getGameResult() {
        return session.getGameResult();
    }

    /**
     * Cleanup resources when game ends
     */
    public void shutdown() {
        timer.shutdown();
    }

    /**
     * Callback for category loading results
     */
    public interface CategoryLoadCallback {
        void onCategoriesLoaded(Map<String, Integer> categories);
        void onError(String message);
    }
}