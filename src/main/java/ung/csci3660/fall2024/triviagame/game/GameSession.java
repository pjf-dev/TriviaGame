package ung.csci3660.fall2024.triviagame.game;

import ung.csci3660.fall2024.triviagame.api.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;

/**
 * Core game session handler that manages state, questions, and results
 * Acts as central coordinator between API and game components
 */
public class GameSession {
    private final TriviaAPI api;
    private GameState state;
    private List<TriviaQuestion> questions;
    private GameConfig config;
    private int currentIndex;
    private GameResult gameResult;

    public GameSession() {
        this.api = TriviaAPI.getInstance();
        this.questions = new ArrayList<>();
        this.state = GameState.INITIALIZING;
        this.gameResult = new GameResult();
    }

    /**
     * Loads questions from API and prepares game session
     * Handles async API call with 10-second timeout
     */
    public boolean loadQuestions(GameConfig config) {
        this.config = config;
        this.currentIndex = 0;
        this.gameResult = new GameResult();

        try {
            CompletableFuture<List<TriviaQuestion>> future = new CompletableFuture<>();

            api.getQuestions(new TriviaCallback<List<TriviaQuestion>>() {
                                 @Override
                                 public void onSuccess(TriviaResponse<List<TriviaQuestion>> response) {
                                     future.complete(response.data);
                                 }

                                 @Override
                                 public void onError(TriviaResponse<Void> response, IOException e) {
                                     future.completeExceptionally(e != null ? e :
                                             new RuntimeException(response.type.msg));
                                 }
                             }, config.getCategoryId(), config.getDifficulty(),
                    config.getNumberOfQuestions(), config.getQuestionType());

            questions = future.get(10, TimeUnit.SECONDS);
            state = GameState.READY;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Records answer results and updates game statistics
     */
    public GameResult recordAnswer(boolean correct, int points, long responseTime) {
        gameResult.addAnswer(correct, points, responseTime);
        return gameResult;
    }

    /**
     * Question navigation methods
     */
    public boolean hasNextQuestion() {
        return currentIndex < questions.size() - 1;
    }

    public void moveToNext() {
        if (hasNextQuestion()) currentIndex++;
    }

    /**
     * Getters for game state and progress
     */
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public TriviaQuestion getCurrentQuestion() { return questions.get(currentIndex); }
    public int getCurrentIndex() { return currentIndex; }
    public int getTotalQuestions() { return questions.size(); }
    public GameConfig getConfig() { return config; }
    public GameResult getGameResult() { return gameResult; }
}