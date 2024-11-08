package ung.csci3660.fall2024.triviagame.game;

import java.util.concurrent.*;

/**
 * Handles countdown timing for trivia questions with timeout functionality.
 * Uses a single thread executor to manage timing tasks.
 */
public class QuestionTimer {
    // Executor for timer tasks
    private final ScheduledExecutorService scheduler;
    // Current running timer task
    private ScheduledFuture<?> timerTask;
    // Time when question started
    private long startTime;
    // Countdown seconds left
    private int secondsRemaining;
    // Function to call when time runs out
    private Runnable timeoutCallback;

    public QuestionTimer() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Starts a new timer for a question
     * @param seconds Time allowed for question
     * @param onTimeout Callback when time expires
     */
    public void start(int seconds, Runnable onTimeout) {
        stop();  // Stop any existing timer
        this.secondsRemaining = seconds;
        this.timeoutCallback = onTimeout;
        this.startTime = System.currentTimeMillis();

        // Schedule countdown task every second
        timerTask = scheduler.scheduleAtFixedRate(() -> {
            secondsRemaining--;
            if (secondsRemaining <= 0) {
                stop();
                timeoutCallback.run();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
      Stops current timer if running
     */
    public void stop() {
        if (timerTask != null) {
            timerTask.cancel(false);
        }
    }

    /**
      @return Milliseconds elapsed since question started
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
      @return Seconds remaining for current question
     */
    public int getSecondsRemaining() {
        return secondsRemaining;
    }


    public void shutdown() {
        stop();
        scheduler.shutdownNow();
    }
}