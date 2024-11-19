package ung.csci3660.fall2024.triviagame;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ung.csci3660.fall2024.triviagame.game.GameConfig;
import ung.csci3660.fall2024.triviagame.game.GameResult;

public class LeaderboardScreen extends Fragment {

    public LeaderboardScreen() {
        // Required empty public constructor
    }

    /**
     * Static new instance method which ensures proper argument handling
     * @param gameResult {@link GameResult} object that represents the game sessions result
     * @return {@link LeaderboardScreen} fragment ready to be used
     */
    public static LeaderboardScreen newInstance(GameResult gameResult, GameConfig config) {
        LeaderboardScreen fragment = new LeaderboardScreen();
        Bundle args = new Bundle();
        args.putParcelable("gameResult", gameResult);
        args.putParcelable("config", config);
        fragment.setArguments(args);
        return fragment;
    }

    private GameQuitListener quitListener;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            // Bind GameQuitListener to the activity that starts/hosts the fragment
            quitListener = (GameQuitListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement GameQuitListener");
        }
    }

    private GameResult gameResult;
    private GameConfig config;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gameResult = getArguments().getParcelable("gameResult", GameResult.class);
                config = getArguments().getParcelable("config", GameConfig.class);
            } else { // < API 33
                gameResult = getArguments().getParcelable("gameResult");
                config = getArguments().getParcelable("config");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find and set click listener for playAgainButton
        Button playAgainBtn = view.findViewById(R.id.playAgainButton);
        playAgainBtn.setOnClickListener((v) -> quitListener.onGameQuit());

        List<Map.Entry<Integer, Integer>> scores;
        if (config.getGameMode().equals(GameConfig.Mode.Classic)) {
            // Retrieve game scores and map them to a new sorted list of Map.Entries to ensure player num and score remain together
            List<Integer> gScores = gameResult.getScores();
            scores = IntStream.range(0, gScores.size())
                    .mapToObj(i -> new AbstractMap.SimpleEntry<>(i, gScores.get(i))).sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());
        } else {
            scores = new ArrayList<>();
            for (int i = 0; i < config.getNumPlayers(); i++) {
                int correct = gameResult.getCorrectAnswers(i);
                int strikesRemaining = config.getStrikes() - gameResult.getScore(i);
                // Make player score = correct answers + strikes remaining.
                // Ensures first and second place player always have different scores
                scores.add(new AbstractMap.SimpleEntry<>(i, correct+strikesRemaining));
            }
            scores.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        }
        // Layout items for the score text views
        LinearLayout leaderboardLayout = view.findViewById(R.id.leaderboardLayout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int dp8 = (int) (8 * getResources().getDisplayMetrics().density);

        /*
        Loop over all scores and create text view with the score
        Text Size depends on position in ranking | 24 -> 20 -> 16 etc
         */
        for (int i = 0; i < scores.size(); i++) {
            Map.Entry<Integer, Integer> score = scores.get(i);
            TextView scoreView = new TextView(view.getContext());
            scoreView.setLayoutParams(layoutParams);
            scoreView.setPadding(dp8, dp8, dp8, dp8);
            scoreView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24 - (i*4));
            scoreView.setText(String.format("%s. Player %s - %s Points", i+1, score.getKey()+1, score.getValue()));
            scoreView.setTextColor(ContextCompat.getColor(view.getContext(), R.color.textColor));
            leaderboardLayout.addView(scoreView);
        }

    }
}