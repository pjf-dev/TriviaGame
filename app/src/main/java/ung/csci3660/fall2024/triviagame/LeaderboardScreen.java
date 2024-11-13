package ung.csci3660.fall2024.triviagame;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.jetbrains.annotations.NotNull;
import ung.csci3660.fall2024.triviagame.game.GameResult;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LeaderboardScreen extends Fragment {

    public LeaderboardScreen() {
        // Required empty public constructor
    }

    public static LeaderboardScreen newInstance(GameResult gameResult) {
        LeaderboardScreen fragment = new LeaderboardScreen();
        Bundle args = new Bundle();
        args.putParcelable("gameResult", gameResult);
        fragment.setArguments(args);
        return fragment;
    }

    private GameQuitListener quitListener;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            quitListener = (GameQuitListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement GameQuitListener");
        }
    }

    private GameResult gameResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gameResult = getArguments().getParcelable("gameResult", GameResult.class);
            } else {
                gameResult = getArguments().getParcelable("gameResult");
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

        Button playAgainBtn = view.findViewById(R.id.playAgainButton);
        playAgainBtn.setOnClickListener((v) -> quitListener.onGameQuit());

        List<Integer> gScores = gameResult.getScores();

        LinearLayout leaderboardLayout = view.findViewById(R.id.leaderboardLayout);
        List<Map.Entry<Integer, Integer>> scores = IntStream.range(0, gScores.size())
                .mapToObj(i -> new AbstractMap.SimpleEntry<>(i, gScores.get(i))).sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int dp8 = (int) (8 * getResources().getDisplayMetrics().density);

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