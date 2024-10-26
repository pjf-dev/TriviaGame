package ung.csci3660.fall2024.triviagame.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * TriviaAPI Class Singleton
 */
public abstract class TriviaAPI {

    // Singleton Code
    private static TriviaAPI instance;

    public static TriviaAPI getInstance() {
        return instance;
    }

    /**
     * Initialize the TriviaAPI instance
     * @param online | Should we operate in online mode
     * @param token | Session token for online mode | Can be null for no token or to generate one later with{@link TriviaAPIOnline#initializeToken(TriviaCallback)}
     * @param force | Should we create a new instance if one already exists
     * @return The TriviaAPI built from the configuration specified in the parameters
     * @throws IllegalStateException | If the method was called and an instance of TriviaAPI exists and method call doesn't specify force parameter
     */
    public static TriviaAPI initializeAPI(boolean online, @Nullable String token, boolean force) throws IllegalStateException {
        if (instance == null || force) {
            if (online) {
                instance = new TriviaAPIOnline(token);
            } else {
                instance = new TriviaAPIOffline();
            }
        } else throw new IllegalStateException("TriviaAPI has already been initialized");
        return instance;
    }

    // Class Code

    protected Map<String, Integer> categoryMap;
    /**
     * Get the map of Trivia Categories
     * Make sure it's initialized first by calling {@link TriviaAPI#initializeCategories(TriviaCallback, boolean)}
     * @return the Map with category name and id
     */
    public Map<String, Integer> getCategories() {
        return categoryMap;
    }

    /**
     * Asynchronous initialization method for Trivia Categories returning categories map in TriviaResponse
     *
     * @param force    | Should we initialize categories even if already initialized
     * @param callback | Callback to run when API request completes
     * @return Cancelable API task, null if no request was needed
     * @see APITask#cancel(boolean)
     */
    public abstract APITask initializeCategories(@NotNull TriviaCallback<Map<String, Integer>> callback, boolean force);

    /**
     * Get questions list based on query, provides response via TriviaCallback.
     * Providing null to nullable fields means to query any of that identifier.
     * You must have called {@link TriviaAPI#initializeCategories(TriviaCallback, boolean)} before calling this method or a null pointer exception will occur.
     *
     * @param callback     Callback to call onError or onSuccess
     * @param categoryCode Category code to query | Nullable
     * @param difficulty   Difficulty to query | Nullable
     * @param numQuestions Number of questions to query | Min 1 | Defaults to 10
     * @param type         Type of questions to query | Nullable
     * @return Cancellable APITask
     */
    public abstract APITask getQuestions(@NotNull TriviaCallback<List<TriviaQuestion>> callback, int categoryCode, @Nullable TriviaQuestion.Difficulty difficulty, @Nullable Integer numQuestions, @Nullable TriviaQuestion.Type type);
}