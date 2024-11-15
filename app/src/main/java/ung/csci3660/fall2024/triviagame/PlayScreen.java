package ung.csci3660.fall2024.triviagame;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;

public class PlayScreen extends Fragment {

    /**
     * Simple listener interface for when user answers a question
     */
    public interface QuestionAnsweredListener {
        void onQuestionAnswered(boolean correct);
    }

    public PlayScreen() {
        // Required empty public constructor
    }

    /**
     * Static method to easily initialize a new {@link PlayScreen}
     * @param playerNum Current player index
     * @param question {@link TriviaQuestion} to build fragment from
     * @return new instance of {@link PlayScreen} ready to be used
     */
    public static PlayScreen newInstance(int playerNum, TriviaQuestion question) {
        PlayScreen fragment = new PlayScreen();
        Bundle args = new Bundle();
        args.putInt("playerNum", playerNum);
        args.putParcelable("question", question);
        fragment.setArguments(args);
        return fragment;
    }

    private QuestionAnsweredListener answeredListener;
    private GameQuitListener quitListener;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            // Bind QuestionAnsweredListener and GameQuitListener to launching / hosting activity
            answeredListener = (QuestionAnsweredListener) context;
            quitListener = (GameQuitListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement QuestionAnswerListener, GameQuitListener");
        }
    }

    private int playerNum;
    private TriviaQuestion question;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playerNum = getArguments().getInt("playerNum");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                question = getArguments().getParcelable("question", TriviaQuestion.class);
            } else { // < API 33
                question = getArguments().getParcelable("question");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Replace player num text view's text with current player num
        TextView playerNumText = view.findViewById(R.id.playerNumText);
        playerNumText.setText(playerNumText.getText().toString().replace('#', Character.forDigit(playerNum+1, 10)));

        // Set question text to initialized questions text
        TextView questionText = view.findViewById(R.id.questionText);
        questionText.setText(question.question());

        // Find radio group and option buttons, adding buttons to list for easy iteration
        RadioGroup rg = view.findViewById(R.id.answerGroup);
        List<RadioButton> answerButtons = new ArrayList<>();
        answerButtons.add(rg.findViewById(R.id.option1));
        answerButtons.add(rg.findViewById(R.id.option2));
        answerButtons.add(rg.findViewById(R.id.option3));
        answerButtons.add(rg.findViewById(R.id.option4));

        // Randomize which answer will be correct
        int correct = new Random().nextInt(answerButtons.size());
        // Track index of incorrect answers
        int incorrectIndex = 0;

        // Sets button with correct index as correct answer, all other button are incorrect answer
        for (int i = 0; i < answerButtons.size(); i++) {
            RadioButton rb = answerButtons.get(i);
            if (i == correct) {
                rb.setText(question.correctAnswer());
                rb.setTag(true);
            } else {
                rb.setText(question.incorrectAnswers()[incorrectIndex]);
                rb.setTag(false);
                incorrectIndex++;
            }
        }

        // Bind radio group check change listener to QuestionAnsweredListener
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            answeredListener.onQuestionAnswered((boolean) rb.getTag());
        });

        // Bind quitBtn click to GameQuitListener
        Button quitBtn = view.findViewById(R.id.homeButton);
        quitBtn.setOnClickListener(v -> quitListener.onGameQuit());
    }
}