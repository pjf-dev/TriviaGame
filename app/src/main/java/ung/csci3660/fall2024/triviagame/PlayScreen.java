package ung.csci3660.fall2024.triviagame;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;

public class PlayScreen extends Fragment {

    /**
     * Simple listener interface for when user answers a question
     */
    public interface QuestionAnsweredListener {
        void onQuestionAnswered(boolean correct, int responseMillis);
    }

    public PlayScreen() {
        // Required empty public constructor
    }

    /**
     * Static method to easily initialize a new {@link PlayScreen}
     * @param playerNum Current player index
     * @param question {@link TriviaQuestion} to build fragment from
     * @param questionTime Time allotted (in seconds) to answer question
     * @return new instance of {@link PlayScreen} ready to be used
     */
    public static PlayScreen newInstance(int playerNum, TriviaQuestion question, int questionTime) {
        PlayScreen fragment = new PlayScreen();
        Bundle args = new Bundle();
        args.putInt("playerNum", playerNum);
        args.putParcelable("question", question);
        args.putInt("questionTime", questionTime);
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
    private int questionTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playerNum = getArguments().getInt("playerNum");
            questionTime = getArguments().getInt("questionTime");
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

    private CountDownTimer countDownTimer;
    private AtomicLong millisRemaining;
    private final int tickRate = 100; // 1 tick / 100 ms
    private ProgressBar timeBar;
    private TextView timeNumText;
    private int medColor, lowColor;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Make background transparent
        view.setBackgroundColor(ContextCompat.getColor(view.getContext(), android.R.color.transparent));

        // setup timebar / timer config early to use in answer event
        timeBar = view.findViewById(R.id.timeProgress);
        int totalMillis = questionTime * 1000;
        timeBar.setMax(totalMillis / tickRate); // subdivide progress bar based on total ticks
        if (millisRemaining == null) {
            millisRemaining = new AtomicLong(totalMillis);
        }
        timeBar.setProgress((int) (millisRemaining.get()/tickRate));

        timeNumText = view.findViewById(R.id.timeRemainNum);
        timeNumText.setText(String.valueOf(questionTime));

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
//        System.out.println("Correct Answer: " + question.correctAnswer()); // Cheat
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
            if (countDownTimer != null) countDownTimer.cancel();
            RadioButton rb = group.findViewById(checkedId);
            answeredListener.onQuestionAnswered((boolean) rb.getTag(), (int) (totalMillis - millisRemaining.get()));
        });

        // Start timer countdown | Tick every 100ms for smooth progress bar

        // Bind quitBtn click to GameQuitListener
        Button quitBtn = view.findViewById(R.id.homeButton);
        quitBtn.setOnClickListener(v -> {
            countDownTimer.cancel();
            quitListener.onGameQuit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        countDownTimer = new CountDownTimer(millisRemaining.get(), tickRate) {
            private int curSeconds = questionTime;
            private int barPhase = 0;
            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining.set(millisUntilFinished);
                timeBar.setProgress(timeBar.getProgress()-1);
                int remainSeconds = (int) (millisUntilFinished / 1000);
                if (remainSeconds != curSeconds) {
                    curSeconds--;
                    timeNumText.setText(String.valueOf(remainSeconds));
                    if (barPhase == 0 && remainSeconds < (questionTime * 0.60)) {
                        ColorStateList csl = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.time_med));
                        timeBar.setProgressTintList(csl);
                        barPhase = 1;
                    } else if (barPhase == 1 && remainSeconds < (questionTime * 0.25)) {
                        ColorStateList csl = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.time_low));
                        timeBar.setProgressTintList(csl);
                        barPhase = 2;
                    }
                }
            }

            @Override
            public void onFinish() {
                answeredListener.onQuestionAnswered(false, questionTime * 1000);
            }
        }.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        countDownTimer.cancel();
    }
}