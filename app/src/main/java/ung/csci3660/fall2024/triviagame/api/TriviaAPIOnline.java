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

public class TriviaAPIOnline extends TriviaAPI {

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

    private String token;

    TriviaAPIOnline(@Nullable String token) {
        this.client = new RateLimitedClient(RATE_LIMIT_MILLIS);
        this.token = token;
    }

    // TODO: Make a method to avoid repeat code in below methods?

    /**
     * Initialize a new session token | Always overwrites stored token (if present)
     * Keep in mind that session tokens expire after 6 hours of no activity
     * The API interface does not track token expiration for you. If using session tokens,
     * make sure that you handle api error code 3 (TOKEN_NOT_FOUND) in the event that the token is deleted,
     * also be sure to track your own activity as an optimization to avoid bad requests.
     * @param callback TriviaCallback of type String
     * @return Cancellable API task
     */
    public APITask initializeToken(@NotNull TriviaCallback<String> callback) {
        Request request = new Request.Builder().url(BASE_URL + TOKEN_EP + "?command=request").build();
        return client.queueRequest(request, new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Any JSON errors will be caught by catch block so we're safe to use json methods
                        JSONObject resObj = new JSONObject(response.body().string());
                        response.close();
                        int resCode = resObj.getInt("response_code");
                        if (resCode == 0) {
                            String newToken = resObj.getString("token");
                            token = newToken;
                            callback.onSuccess(new TriviaResponse<>(0, newToken));
                        } else {
                            callback.onError(new TriviaResponse<>(resCode, null), null);
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
                callback.onError(null, e);
            }
        });
    }

    /**
     * Get current token string
     * @return Token String | Can be null
     */
    public String getToken() {
        return token;
    }

    @Override
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

    @Override
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
        if (token != null) {
            params.add("token=" + token);
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
                            callback.onError(new TriviaResponse<>(resCode, null), null);
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
}
