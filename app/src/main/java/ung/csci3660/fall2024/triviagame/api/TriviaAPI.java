package ung.csci3660.fall2024.triviagame.api;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final int QS_PER_REQUEST = 50;

    // Since TriviaApi is a singleton, we'll only have one instance of RateLimitedClient at any given time
    // This is necessary because threads are expensive, and RateLimitedClient spawns its own thread to perform HTTP requests
    private final RateLimitedClient client;

    private Map<String, Integer> categoryMap;

    private TriviaAPI() {
        this.client = new RateLimitedClient(RATE_LIMIT_MILLIS);
    }

    /**
     * Asynchronous initialization method for Trivia Categories using a ? type TriviaCallback
     * @param force | Should we initialize categories even if already initialized
     * @param callback | Callback to run when API request completes
     * @return Cancelable API task, null if no request was needed
     * @see APITask#cancel(boolean)
     */
    public APITask initializeCategories(@NotNull TriviaCallback<?> callback, boolean force) {
        if (categoryMap == null || force) {
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
                            response.close();
                            JSONArray jCategories = resObj.getJSONArray("trivia_categories");
                            Map<String, Integer> newCategoryMap = new HashMap<>();
                            for (int i = 0; i < jCategories.length(); i++) {
                                JSONObject jCategory = jCategories.getJSONObject(i);
                                newCategoryMap.put(jCategory.getString("name"), jCategory.getInt("id"));
                            }
                            newCategoryMap.put("Any", -1);
                            categoryMap = newCategoryMap;
                            callback.onSuccess(new TriviaResponse<>(0, null));
                        } catch (JSONException e) {
                            response.close();
                            callback.onError(new TriviaResponse<>(-2, null), null);
                        }
                    } else {
                        response.close();
                        callback.onError(new TriviaResponse<>(-1, null), null);
                    }
                }
            });
        }
        return null;
    }

    // TODO: Maybe implement convince methods for getting questions so you dont have to specify null

    /**
     * Get questions list based on query, provides response via TriviaCallback.
     * Providing null to nullable fields means to query any of that identifier.
     * You must have called {@link TriviaAPI#initializeCategories(TriviaCallback, boolean)} before calling this method or a null pointer exception will occur.
     * @param callback Callback to call onError or onSuccess
     * @param categoryCode Category code to query | Nullable
     * @param difficulty Difficulty to query | Nullable
     * @param numQuestions Number of questions to query | Min 1 | Defaults to 10
     * @param type Type of questions to query | Nullable
     * @return Cancellable APITask
     */
    public APITask getQuestions(@NotNull TriviaCallback<List<TriviaQuestion>> callback, int categoryCode, @Nullable TriviaQuestion.Difficulty difficulty, @Nullable Integer numQuestions, @Nullable TriviaQuestion.Type type) {
        List<String> params = new ArrayList<>();

        if (categoryCode >= 0) {
            params.add("category=" + Math.min(categoryCode, QS_PER_REQUEST));
        }
        if (difficulty != null && difficulty != TriviaQuestion.Difficulty.ANY) {
            params.add("difficulty=" + difficulty.toString().toLowerCase());
        }
        if (numQuestions != null && numQuestions > 0) {
            params.add("amount=" + numQuestions);
        }
        if (type != null && type != TriviaQuestion.Type.ANY) {
            params.add("type=" + type.toString().toLowerCase());
        }

        // Build request string
        StringBuilder url = new StringBuilder(BASE_URL + API_EP);
        if (!params.isEmpty()) { url.append("?"); }
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) { url.append("&"); }
            url.append(params.get(i));
        }

        Request request = new Request.Builder().url(url.toString()).build();
        return client.queueRequest(request, new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // parse response json
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Any JSON errors will be caught by catch block so we're safe to use json methods
                        JSONObject resObj = new JSONObject(response.body().string());
                        response.close();
                        // TODO: Finish implementing method
                        int resCode = resObj.getInt("response_code");
                        JSONArray jQuestions = resObj.getJSONArray("results");

                        List<TriviaQuestion> questions = new ArrayList<>();

                        if (resCode == 0 && jQuestions.length() > 0) {
                            for (int i = 0; i < jQuestions.length(); i++) {
                                JSONObject jQuestion = jQuestions.getJSONObject(i);
                                String category = jQuestion.getString("category");

                                // Get incorrect answer array as String[]
                                JSONArray jIncorrectAnswers = jQuestion.getJSONArray("incorrect_answers");
                                String[] incorrectAnswers = new String[jIncorrectAnswers.length()];
                                for (int j = 0; j < jIncorrectAnswers.length(); j++) {
                                    incorrectAnswers[j] = jIncorrectAnswers.getString(j);
                                }

                                TriviaQuestion question = new TriviaQuestion(
                                        TriviaQuestion.Type.valueOf(jQuestion.getString("type").toUpperCase()),
                                        TriviaQuestion.Difficulty.valueOf(jQuestion.getString("difficulty").toUpperCase()),
                                        jQuestion.getString("question"),
                                        category, categoryMap.getOrDefault(category, -1), // Could technically be null, but we informed consumer to initialize categories first
                                        jQuestion.getString("correct_answer"),
                                        incorrectAnswers
                                );
                                questions.add(question);
                            }
                            callback.onSuccess(new TriviaResponse<>(0, questions));
                        } else {
                            callback.onSuccess(new TriviaResponse<>(resCode, null));
                        }
                    } catch (JSONException e) {
                        response.close();
                        callback.onError(new TriviaResponse<>(-2, null), null);
                    }
                } else {
                    response.close();
                    callback.onError(new TriviaResponse<>(-1, null), null);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onError(null, e); // Bubble error
            }
        });
    }

    /**
     * Get the map of Trivia Categories
     * Make sure it's initialized first by calling {@link TriviaAPI#initializeCategories(TriviaCallback, boolean)}
     * @return the Map with category name and id
     */
    public Map<String, Integer> getCategories() {
        return categoryMap;
    }

}
