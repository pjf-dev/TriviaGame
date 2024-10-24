package ung.csci3660.fall2024.triviagame.api;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * TriviaAPI Class Singleton
 */
public class TriviaAPI {

    // Singleton Code
    private static TriviaAPI instance;
    public static TriviaAPI getInstance() {
        if (instance == null) {
            instance = new TriviaAPI();
        }
        return instance;
    }

    // Class Code

    // API URLs | EP = End Point
    private final String BASE_URL = "https://opentdb.com";
    private final String API_EP = "/api.php";
    private final String TOKEN_EP = "/api_token.php";
    private final String CATEGORY_EP = "/api_category.php";
    // API Limits | Hard limits, not changeable
    private final int RATE_LIMIT_MILLIS = 5 * 5000; // 1 request per 5 seconds
    private final int CATEGORIES_PER_REQUEST = 1;
    private final int QS_PER_REQUEST = 50;

    // Since TriviaApi is a singleton, we'll only have one instance of RateLimitedClient at any given time
    // This is necessary because threads are expensive, and RateLimitedClient spawns its own thread to perform HTTP requests
    private final RateLimitedClient client;

    private List<TriviaCategory> categories;

    private TriviaAPI() {
        this.client = new RateLimitedClient(RATE_LIMIT_MILLIS);
    }

    /**
     * Asynchronous initialization method for Trivia Categories using a ? type TriviaCallback
     * @param force | Should we initialize categories even if already initialized
     * @param callback | Callback to run when API request completes
     * @return Cancelable API task
     * @see APITask#cancel(boolean)
     */
    public APITask initializeCategories(boolean force, TriviaCallback<?> callback) {
        if (categories == null || force) {
            Request request = new Request.Builder().url(BASE_URL + CATEGORY_EP).build();
            return client.queueRequest(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onError(null, e); // Bubble error
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    // Initialize categories
                    // Errors handled with error callback
                    if (response.isSuccessful() && response.body() != null) { // This endpoint doesn't provide the api error codes so we use isSuccessful()
                        try {
                            // Any JSON errors will be caught by catch block so we're safe to use json methods
                            JSONObject resObj = new JSONObject(response.body().string());
                            JSONArray jCategories = resObj.getJSONArray("trivia_categories");
                            List<TriviaCategory> newCategories = new ArrayList<>();
                            for (int i = 0; i < jCategories.length(); i++) {
                                JSONObject jCategory = jCategories.getJSONObject(i);
                                TriviaCategory category = new TriviaCategory(jCategory.getInt("id"), jCategory.getString("name"));
                                newCategories.add(category);
                            }
                            response.body().close();
                            newCategories.add(new TriviaCategory(-1, "Any"));
                            categories = newCategories;
                            callback.onSuccess(new TriviaResponse<>(0, null));
                        } catch (JSONException e) {
                            callback.onError(new TriviaResponse<>(-2, null), null);
                        }
                    } else {
                        callback.onError(new TriviaResponse<>(-1, null), null);
                    }
                }
            });
        }
        return null;
    }

    // TODO: Implement methods to get questions

    public APITask getQuestions(TriviaCategory category, TriviaQuestion.Difficulty difficulty, int numQuestions, TriviaResponse<List<TriviaQuestion>> callback) {
        // TODO: Implement method
        return null;
    }

    /**
     * Get the list of Trivia Categories
     * Make sure it's initialized first by calling {@link TriviaAPI#initializeCategories(boolean, TriviaCallback)}
     * @return the list of TriviaCategories
     */
    public List<TriviaCategory> getCategories() {
        return categories;
    }

}
