package ung.csci3660.fall2024.triviagame;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ung.csci3660.fall2024.triviagame.api.TriviaAPI;
import ung.csci3660.fall2024.triviagame.api.TriviaCallback;
import ung.csci3660.fall2024.triviagame.api.TriviaQuestion;
import ung.csci3660.fall2024.triviagame.api.TriviaResponse;
import ung.csci3660.fall2024.triviagame.game.GameConfig;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer musicPlayer;

    private Integer playerCount = 1;
    private Integer categoryID = -1;

    private TextView categoryDisplay, playerCountDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_TriviaGame);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize MediaPlayer with the MP3 file and play it (also make it loop)
        musicPlayer = MediaPlayer.create(this, R.raw.kahoot_music);
        musicPlayer.setLooping(true);
        musicPlayer.start();

        categoryDisplay = findViewById(R.id.categoryDisplay);
        categoryDisplay.setText("Any");
        playerCountDisplay = findViewById(R.id.playerCountDisplay);
        playerCountDisplay.setText("1");

        RadioGroup rg = findViewById(R.id.modeSelect);

        // Initialize TriviaAPI
        TriviaAPI api = TriviaAPI.initializeAPI(true, null, false);

        // Start loading categories ASAP in the background
        // Load categories asynchronously without proper handling | TODO: Add loading spinner?
        api.initializeCategories(new TriviaCallback<>() {
            @Override
            public void onSuccess(TriviaResponse<Map<String, Integer>> response) {
                System.out.println("Success Res");
                runOnUiThread(() -> { // Update categorySpinner on UI thread
                    // Requires UI thread for view + click listener stuff
                    Button categoryButton = findViewById(R.id.categoryButton);
                    categoryButton.setOnClickListener((v -> {
                        PickerDialog dialog = new PickerDialog(400,"Select Category:", api.getCategories(),
                                ((itemText, itemValue) -> {
                                    categoryID = itemValue;
                                    categoryDisplay.setText(itemText);
                                }));
                        dialog.show(getLayoutInflater());
                    }));
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

        Button playersButton = findViewById(R.id.playersButton);
        Map<String, Integer> playersMap = new HashMap<>() {{
            put("1", 1);
            put("2", 2);
            put("3", 3);
            put("4", 4);
            put("5", 5);
        }};

        playersButton.setOnClickListener((v) -> {
            PickerDialog dialog = new PickerDialog(200, "Player Count:",playersMap,
                    ((itemText, itemValue) -> {
                        playerCount = itemValue;
                        playerCountDisplay.setText(itemText);
                    }));
            dialog.show(getLayoutInflater());
        });

        findViewById(R.id.playButton).setOnClickListener((view) -> { // Play button click listener

            GameConfig.Mode gameMode = GameConfig.Mode.Classic;
            if (rg.getCheckedRadioButtonId() == R.id.infinityRadio) {
                gameMode = GameConfig.Mode.Infinity;
            }

            // Build game config from supplied values / current limitations
            GameConfig config = new GameConfig.Builder()
                    .setCategory(categoryID) // Safely ignore due to getOrDefault
                    .setQuestionType(TriviaQuestion.Type.MULTIPLE)
                    .setDifficulty(TriviaQuestion.Difficulty.ANY) // TODO: Implement Selector?
                    .setNumPlayers(playerCount)
                    .setNumberOfQuestions(playerCount*10) // 10 questions per player
                    .setTimePerQuestion(30) // 30 seconds to answer
                    .setMode(gameMode) // set game mode
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
        if (musicPlayer != null) {
            musicPlayer.release(); // Release resources when done
            musicPlayer = null;
        }
    }
}