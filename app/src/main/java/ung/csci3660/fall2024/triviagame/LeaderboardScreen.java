package ung.csci3660.fall2024.triviagame;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.jetbrains.annotations.NotNull;
import ung.csci3660.fall2024.triviagame.game.GameResult;

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
        // TODO: Implement LeaderboardScreen
    }
}