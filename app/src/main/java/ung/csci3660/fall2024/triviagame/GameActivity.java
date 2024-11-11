package ung.csci3660.fall2024.triviagame;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.jetbrains.annotations.NotNull;
import ung.csci3660.fall2024.triviagame.api.APITask;
import ung.csci3660.fall2024.triviagame.api.TriviaAPI;
import ung.csci3660.fall2024.triviagame.api.Callback;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;
import ung.csci3660.fall2024.triviagame.game.GameConfig;
import ung.csci3660.fall2024.triviagame.game.GameSession;

import java.util.List;

public class GameActivity extends AppCompatActivity {

    public static void start(Context context, GameConfig config, TriviaQuestion[] initQuestions) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra("config", config);
        intent.putExtra("questions", initQuestions);
        context.startActivity(intent);
    }

    private GameSession session;
    private GameConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TriviaQuestion[] questions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            config = getIntent().getParcelableExtra("config", GameConfig.class);
            questions = getIntent().getParcelableArrayExtra("questions", TriviaQuestion.class);
        } else {
            config = getIntent().getParcelableExtra("config");
            questions = (TriviaQuestion[]) getIntent().getParcelableArrayExtra("questions");
        }

        session = new GameSession(questions);

        setContentView(R.layout.activity_game);
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

    public static APITask getQuestions(@NotNull Callback<List<TriviaQuestion>> callback, GameConfig config) {
        return TriviaAPI.getInstance().getQuestions(callback, config.getCategoryId(), config.getDifficulty(), config.getNumberOfQuestions(), config.getQuestionType());
    }

//    /**
//     * Initializes game with selected configuration
//     * @return true if initialization successful
//     */
//    public boolean initialize(GameConfig config) {
//        if (session.getState() != GameResult.GameState.INITIALIZING) return false;
//        return session.loadQuestions(config);
//    }
//
//    /**
//     * Starts game if ready
//     * @return true if game started successfully
//     */
//    public boolean startGame() {
//        if (session.getState() != GameResult.GameState.READY) return false;
//        session.setState(GameResult.GameState.IN_PROGRESS);
//        startCurrentQuestion();
//        return true;
//    }
//
//
//    /**
//     * Advances to next question if available
//     * @return false if no more questions
//     */
//    public boolean moveToNextQuestion() {
//        if (session.getState() != GameResult.GameState.IN_PROGRESS) return false;
//
//        if (session.hasNextQuestion()) {
//            session.moveToNext();
//            startCurrentQuestion();
//            return true;
//        } else {
//            session.setState(GameResult.GameState.FINISHED);
//            return false;
//        }
//    }
//
//    /**
//     * Starts timer for current question
//     */
//    private void startCurrentQuestion() {
//        timer.start(session.getConfig().getTimePerQuestionSeconds(), () -> {
//            session.setState(GameResult.GameState.FINISHED);
//        });
//    }
}