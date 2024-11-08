package ung.csci3660.fall2024.triviagame.game;

/**
 * Tracks game progress and statistics
 */
public class GameResult {
    private int score;
    private int correctAnswers;
    private int totalAnswered;
    private long lastResponseTime;
    private boolean lastAnswerCorrect;
    private int lastPoints;

    public GameResult() {
        this.score = 0;
        this.correctAnswers = 0;
        this.totalAnswered = 0;
    }

    public void addAnswer(boolean correct, int points, long responseTime) {
        if (correct) correctAnswers++;
        totalAnswered++;
        score += points;
        lastResponseTime = responseTime;
        lastAnswerCorrect = correct;
        lastPoints = points;
    }

    // Getters
    public int getScore() { return score; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getTotalAnswered() { return totalAnswered; }
    public double getAccuracyPercentage() {
        return totalAnswered == 0 ? 0.0 :
                (double) correctAnswers / totalAnswered * 100.0;
    }
    public long getLastResponseTime() { return lastResponseTime; }
    public boolean isLastAnswerCorrect() { return lastAnswerCorrect; }
    public int getLastPoints() { return lastPoints; }
}

// GameState enum
package ung.csci3660.fall2024.triviagame.game;

public enum GameState {
    INITIALIZING,
    READY,
    IN_PROGRESS,
    FINISHED
}