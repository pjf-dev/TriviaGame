package ung.csci3660.fall2024.triviagame;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.jetbrains.annotations.NotNull;
import ung.csci3660.fall2024.triviagame.api.APITask;
import ung.csci3660.fall2024.triviagame.api.TriviaAPI;
import ung.csci3660.fall2024.triviagame.api.TriviaCallback;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;
import ung.csci3660.fall2024.triviagame.game.GameConfig;
import ung.csci3660.fall2024.triviagame.game.GameSession;

import java.util.List;

public class GameActivity extends AppCompatActivity {

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

    public static APITask getQuestions(@NotNull TriviaCallback<List<TriviaQuestion>> callback, GameConfig config) {
        return TriviaAPI.getInstance().getQuestions(callback, config.getCategoryId(), config.getDifficulty(), config.getNumberOfQuestions(), config.getQuestionType());
    }
}