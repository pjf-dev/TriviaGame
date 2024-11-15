package ung.csci3660.fall2024.triviagame;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PassPhoneScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PassPhoneScreen extends Fragment {

    /**
     * Simple event listener interface for when user clicks next user button
     */
    public interface NextClickedListener {
        void onNextClick();
    }

    public PassPhoneScreen() {
        // Required empty public constructor
    }

    /**
     * Static method to initialize new PassPhoneScreen fragment
     * @param playerNum Index of current player + 1
     * @param addedScore Amount of score added
     * @param totalScore Players total score
     * @return new instance of {@link PassPhoneScreen} ready to be used
     */
    public static PassPhoneScreen newInstance(int playerNum, int addedScore, int totalScore) {
        PassPhoneScreen fragment = new PassPhoneScreen();
        Bundle args = new Bundle();
        args.putInt("playerNum", playerNum);
        args.putInt("addedScore", addedScore);
        args.putInt("totalScore", totalScore);
        fragment.setArguments(args);
        return fragment;
    }

    private NextClickedListener nextClickedListener;
    private GameQuitListener quitListener;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            // Bind NextClickListener and GameQuitListener to launching / hosting activity
            nextClickedListener = (NextClickedListener) context;
            quitListener = (GameQuitListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement NextClickListener, GameQuitListener");
        }
    }

    private int playerNum, addedScore, totalScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playerNum = getArguments().getInt("playerNum");
            addedScore = getArguments().getInt("addedScore");
            totalScore = getArguments().getInt("totalScore");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pass_phone_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Build score string based on player num, added score, and total score
        StringBuilder scoreStr = new StringBuilder();
        scoreStr.append("Player ").append(playerNum).append("\n");
        scoreStr.append(addedScore > 0 ? "Correct: +" : "Incorrect: +")
                .append(addedScore).append("\n");
        scoreStr.append("Total Score: ").append(totalScore);

        // Set qResultText view's text to scoreStr
        TextView qResultView = view.findViewById(R.id.qResultText);
        qResultView.setText(scoreStr.toString());

        // Bind next btn click to NextClickedListener
        Button nextBtn = view.findViewById(R.id.nextPlayerReadyButton);
        nextBtn.setOnClickListener(v -> nextClickedListener.onNextClick());

        // Bind quitButton click to GameQuitListener
        Button quitBtn = view.findViewById(R.id.homeButton);
        quitBtn.setOnClickListener(v -> quitListener.onGameQuit());

    }
}