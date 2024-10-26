package ung.csci3660.fall2024.triviagame.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class TriviaAPIOffline extends TriviaAPI {

    // TODO: Implement this at some point

    TriviaAPIOffline() {

    }

    @Override
    public APITask initializeCategories(@NotNull TriviaCallback<Map<String, Integer>> callback, boolean force) {
        return null;
    }

    @Override
    public APITask getQuestions(@NotNull TriviaCallback<List<TriviaQuestion>> callback, int categoryCode, @Nullable TriviaQuestion.Difficulty difficulty, @Nullable Integer numQuestions, @Nullable TriviaQuestion.Type type) {
        return null;
    }
}
