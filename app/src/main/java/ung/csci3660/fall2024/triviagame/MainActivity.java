package ung.csci3660.fall2024.triviagame;

import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import ung.csci3660.fall2024.triviagame.api.*;
import ung.csci3660.fall2024.triviagame.game.GameConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TriviaAPI
        TriviaAPI api = TriviaAPI.initializeAPI(true, null, false);

        // Start loading categories ASAP in the background
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        // Load categories asynchronously without proper handling | TODO: Add spinner?
        api.initializeCategories(new Callback<>() {
            @Override
            public void onSuccess(Response<Map<String, Integer>> response) {
                System.out.println("Success Res");
                runOnUiThread(() -> {
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                            MainActivity.this, android.R.layout.simple_spinner_item,
                            response.data.keySet().toArray(new String[0]));
                    categorySpinner.setAdapter(categoryAdapter);
                    categorySpinner.setSelection(0);
                });
            }

            @Override
            public void onError(Response<Void> response, IOException e) {
                // TODO: Error Handle
                System.out.println("Error res");
                if (e != null) {
                    e.printStackTrace();
                } else {
                    System.out.println(response);
                }
            }
        }, false);

        Spinner playerSpinner = findViewById(R.id.spinnerPlayer);
        ArrayAdapter<Integer> playerAdapter = new ArrayAdapter<>(
                MainActivity.this, android.R.layout.simple_spinner_item,
                new Integer[] {1,2,3,4,5}
        );
        playerSpinner.setAdapter(playerAdapter);
        playerSpinner.setSelection(0);

        findViewById(R.id.playButton).setOnClickListener((view) -> {
            Integer categoryID = api.getCategories().getOrDefault((String) categorySpinner.getSelectedItem(), -1);
            Integer numPlayers = (Integer) playerSpinner.getSelectedItem();
            GameConfig config = new GameConfig.Builder()
                    .setCategory(categoryID) // Safely ignore due to getOrDefault
                    .setQuestionType(TriviaQuestion.Type.MULTIPLE)
                    .setDifficulty(TriviaQuestion.Difficulty.ANY) // TODO: Implement Selector?
                    .setNumPlayers(numPlayers)
                    .setNumberOfQuestions(numPlayers*10)
                    .setTimePerQuestion(30)
                    .build();

            // Initialize questions and start GameActivity
            GameActivity.getQuestions(new Callback<>() {
                @Override
                public void onSuccess(Response<List<TriviaQuestion>> response) {
                    GameActivity.start(MainActivity.this, config, response.data.toArray(new TriviaQuestion[0]));
                }

                @Override
                public void onError(Response<Void> response, IOException e) {
                    // TODO: Error Handle
                }
            }, config);
        });
    }
}