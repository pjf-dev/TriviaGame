package ung.csci3660.fall2024.triviagame;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.appcompat.app.AppCompatActivity;
import org.jetbrains.annotations.NotNull;
import ung.csci3660.fall2024.triviagame.api.APITask;
import ung.csci3660.fall2024.triviagame.api.TriviaAPI;
import ung.csci3660.fall2024.triviagame.api.TriviaCallback;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;
import ung.csci3660.fall2024.triviagame.game.GameConfig;
import ung.csci3660.fall2024.triviagame.game.GameResult;
import ung.csci3660.fall2024.triviagame.game.GameSession;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GameActivity extends AppCompatActivity implements PlayScreen.QuestionAnsweredListener, PassPhoneScreen.NextClickedListener, GameQuitListener {

    public static void start(Context context, GameConfig config, List<TriviaQuestion> initQuestions) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra("config", config);
        intent.putParcelableArrayListExtra("questions", (ArrayList<? extends Parcelable>) initQuestions);
        context.startActivity(intent);
    }

    public static APITask getQuestions(@NotNull TriviaCallback<List<TriviaQuestion>> callback, GameConfig config) {
        return TriviaAPI.getInstance().getQuestions(callback, config.getCategoryId(), config.getDifficulty(), config.getNumberOfQuestions(), config.getQuestionType());
    }

    private GameSession session;
    private GameConfig config;
    private long qStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<TriviaQuestion> questions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            config = getIntent().getParcelableExtra("config", GameConfig.class);
            questions = getIntent().getParcelableArrayListExtra("questions", TriviaQuestion.class);
        } else {
            config = getIntent().getParcelableExtra("config");
            questions = getIntent().getParcelableArrayListExtra("questions");
        }

        session = new GameSession(config, questions);
        setContentView(R.layout.activity_game);

        showNextQuestion(session.getCurrentPlayerIndex(), session.getCurrentQuestion());
    }

    private void showNextQuestion(int playerNum, TriviaQuestion question) {
        PlayScreen screen = PlayScreen.newInstance(playerNum, question);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, screen)
                .commit();
        qStartTime = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void onNextClick() {
        if (session.hasNextQuestion()) {
            // Increment player and start next question
            showNextQuestion(session.nextPlayer(), session.nextQuestion());
        } else {
            // End Game
            LeaderboardScreen leaderboardScreen = LeaderboardScreen.newInstance(session.getGameResult());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, leaderboardScreen).commit();
        }
    }

    @Override
    public void onQuestionAnswered(boolean correct) {
        long now = Calendar.getInstance().getTimeInMillis();
        GameResult res = session.recordAnswer(correct, now-qStartTime);
        int player = session.getCurrentPlayerIndex();
        System.out.printf("P: %s\nL: %s\nS: %s\n", player+1, res.getLastAnswerPoints(player), res.getScore(player));
        PassPhoneScreen passScreen = PassPhoneScreen.newInstance(
                player+1, res.getLastAnswerPoints(player), res.getScore(player));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, passScreen).commit();
    }

    @Override
    public void onGameQuit() {
        this.getOnBackPressedDispatcher().onBackPressed();
    }
}