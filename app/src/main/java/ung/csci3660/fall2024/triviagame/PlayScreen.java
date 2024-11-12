package ung.csci3660.fall2024.triviagame;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.jetbrains.annotations.NotNull;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayScreen extends Fragment {

    public interface QuestionAnsweredListener {
        void onQuestionAnswered(boolean correct);
    }

    public PlayScreen() {
        // Required empty public constructor
    }

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
            } else {
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

        TextView playerNumText = view.findViewById(R.id.playerNumText);
        playerNumText.setText(playerNumText.getText().toString().replace('#', Character.forDigit(playerNum+1, 10)));

        TextView questionText = view.findViewById(R.id.questionText);
        questionText.setText(question.question());

        RadioGroup rg = view.findViewById(R.id.answerGroup);

        List<RadioButton> answerButtons = new ArrayList<>();
        answerButtons.add(rg.findViewById(R.id.option1));
        answerButtons.add(rg.findViewById(R.id.option2));
        answerButtons.add(rg.findViewById(R.id.option3));
        answerButtons.add(rg.findViewById(R.id.option4));

        int correct = new Random().nextInt(answerButtons.size());
        int incorrectIndex = 0;
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

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            answeredListener.onQuestionAnswered((boolean) rb.getTag());
        });

        Button quitBtn = view.findViewById(R.id.homeButton);
        quitBtn.setOnClickListener(v -> quitListener.onGameQuit());
    }
}