package ung.csci3660.fall2024.triviagame;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import ung.csci3660.fall2024.triviagame.api.TriviaAPI;
import ung.csci3660.fall2024.triviagame.api.TriviaCallback;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;
import ung.csci3660.fall2024.triviagame.api.TriviaResponse;
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
        // Load categories asynchronously without proper handling | TODO: Add loading spinner?
        api.initializeCategories(new TriviaCallback<>() {
            @Override
            public void onSuccess(TriviaResponse<Map<String, Integer>> response) {
                System.out.println("Success Res");
                runOnUiThread(() -> { // Update categorySpinner on UI thread
                    // Create ArrayAdapter for category spinner, populating it with the category names
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                            MainActivity.this, android.R.layout.simple_spinner_item,
                            response.data.keySet().toArray(new String[0]));
                    categorySpinner.setAdapter(categoryAdapter);
                    categorySpinner.setSelection(0);
                });
            }

            @Override
            public void onError(TriviaResponse<Void> response, IOException e) {
                // TODO: Error Handle
                System.out.println("Error res");
                if (e != null) {
                    e.printStackTrace();
                } else {
                    System.out.println(response);
                }
            }
        }, false);

        // Populate playerSpinner with values 1-5 using an ArrayAdapter
        Spinner playerSpinner = findViewById(R.id.spinnerPlayer);
        ArrayAdapter<Integer> playerAdapter = new ArrayAdapter<>(
                MainActivity.this, android.R.layout.simple_spinner_item,
                new Integer[] {1,2,3,4,5}
        );
        playerSpinner.setAdapter(playerAdapter);
        playerSpinner.setSelection(0);

        findViewById(R.id.playButton).setOnClickListener((view) -> { // Play button click listener
            // Use TriviaAPI#categories map to get id of selected item in categorySpinner
            Integer categoryID = api.getCategories().getOrDefault((String) categorySpinner.getSelectedItem(), -1);
            // Get num players from playerSpinner
            Integer numPlayers = (Integer) playerSpinner.getSelectedItem();

            // Build game config from supplied values / current limitations
            GameConfig config = new GameConfig.Builder()
                    .setCategory(categoryID) // Safely ignore due to getOrDefault
                    .setQuestionType(TriviaQuestion.Type.MULTIPLE)
                    .setDifficulty(TriviaQuestion.Difficulty.ANY) // TODO: Implement Selector?
                    .setNumPlayers(numPlayers)
                    .setNumberOfQuestions(numPlayers*10) // 10 questions per player
                    .setTimePerQuestion(30) // 30 seconds to answer
//                    .setMode(GameConfig.Mode.Infinity) // temp
                    .build();

            // Initialize questions and start GameActivity on success
            GameActivity.getQuestions(new TriviaCallback<>() {
                @Override
                public void onSuccess(TriviaResponse<List<TriviaQuestion>> response) {
                    GameActivity.start(MainActivity.this, config, response.data);
                }

                @Override
                public void onError(TriviaResponse<Void> response, IOException e) {
                    // TODO: Error Handle
                }
            }, config);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}