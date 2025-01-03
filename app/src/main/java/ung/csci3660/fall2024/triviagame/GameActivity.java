package ung.csci3660.fall2024.triviagame;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ung.csci3660.fall2024.triviagame.api.APITask;
import ung.csci3660.fall2024.triviagame.api.TriviaAPI;
import ung.csci3660.fall2024.triviagame.api.TriviaCallback;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;
import ung.csci3660.fall2024.triviagame.game.GameConfig;
import ung.csci3660.fall2024.triviagame.game.GameResult;
import ung.csci3660.fall2024.triviagame.game.GameSession;

public class GameActivity extends AppCompatActivity implements PlayScreen.QuestionAnsweredListener, PassPhoneScreen.NextClickedListener, GameQuitListener {

    /**
     * Static method to start {@link GameActivity} ensuring proper intent / args
     * @param context Context for creating intent and starting activity
     * @param config {@link GameConfig} object representing the game configuration
     * @param initQuestions Initial {@link List} of {@link TriviaQuestion}s
     */
    public static void start(Context context, GameConfig config, List<TriviaQuestion> initQuestions) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra("config", config);
        intent.putParcelableArrayListExtra("questions", (ArrayList<? extends Parcelable>) initQuestions);
        context.startActivity(intent);
    }

    /**
     * Static method to get questions based on a {@link GameConfig} object
     * @param callback Callback to execute when a response is received
     * @param config Game configuration object
     * @return Cancellable {@link APITask}
     */
    public static APITask getQuestions(@NotNull TriviaCallback<List<TriviaQuestion>> callback, GameConfig config) {
        return TriviaAPI.getInstance().getQuestions(callback, config.getCategoryId(), config.getDifficulty(), config.getNumberOfQuestions(), config.getQuestionType());
    }

    /*
        Instance Variables
     */
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
        } else { // API < 33
            config = getIntent().getParcelableExtra("config");
            questions = getIntent().getParcelableArrayListExtra("questions");
        }

        // Init game session
        session = new GameSession(config, questions);
        setContentView(R.layout.activity_game);

        // Start game by showing next question
        showNextQuestion(session.getCurrentPlayerIndex(), session.getCurrentQuestion());
    }

    /**
     * Display the next question to the screen by creating a new PlayScreen fragment with args
     * @param playerNum The player whose turn it is to go
     * @param question The question to display on the {@link PlayScreen}
     */
    private void showNextQuestion(int playerNum, TriviaQuestion question) {
        PlayScreen screen = PlayScreen.newInstance(playerNum, question, config.getTimePerQuestionSeconds());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, screen)
                .commit();
        // Record question start time
        qStartTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Click handler for {@link PassPhoneScreen} next player button
     */
    @Override
    public void onNextClick() {
        int nextPlayer = session.nextPlayer();
        /* Test if we have next question and if next player exists
        Additionally test if theres more than 1 player remaining on the condition that theres more than one total player at the game start */
        if (session.hasNextQuestion() && nextPlayer != -1
            && (config.getNumPlayers() == 1 || session.getPlayersRemaining() > 1)) {
            // start next question
            showNextQuestion(nextPlayer, session.nextQuestion());
        } else {
            // End Game
            LeaderboardScreen leaderboardScreen = LeaderboardScreen.newInstance(session.getGameResult(), config);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, leaderboardScreen).commit();
        }
    }

    /**
     * Handler for when user answers question in {@link PlayScreen}
     * @param correct Was the answer correct
     */
    @Override
    public void onQuestionAnswered(boolean correct, int responseMillis) {
        GameResult res;
        if (config.getGameMode().equals(GameConfig.Mode.Infinity)) {
            res = session.recordAnswer(correct);
        } else {
            long now = Calendar.getInstance().getTimeInMillis();
            res = session.recordAnswer(correct, now - qStartTime);
        }
        int player = session.getCurrentPlayerIndex();
        System.out.printf("P: %s\nL: %s\nS: %s\n", player + 1, res.getLastAnswerPoints(player), res.getScore(player));
        PassPhoneScreen passScreen = PassPhoneScreen.newInstance(
                player+1, res.getLastAnswerPoints(player), res.getScore(player), config);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, passScreen).commit();
    }


    private boolean gameQuit = false;
    /**
     * Click handler for the game "quit" button
     * Also used for handling game exit in {@link LeaderboardScreen}
     */
    @Override
    public void onGameQuit() {
        gameQuit = true;
        this.getOnBackPressedDispatcher().onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!gameQuit && MainActivity.musicPlayer != null)
            MainActivity.musicPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainActivity.musicPlayer != null && !MainActivity.musicPlayer.isPlaying())
            MainActivity.musicPlayer.start();
    }
}