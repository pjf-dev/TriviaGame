package ung.csci3660.fall2024.triviagame.api;

import java.io.IOException;

/**
 * Trivia Callback interface
 * Calls {@link TriviaCallback#onSuccess(TriviaResponse)} on successful API request
 * Calls {@link TriviaCallback#onError(TriviaResponse, IOException)} otherwise
 * @param <T> type of {@link TriviaResponse}
 */
public interface TriviaCallback<T> {
    /**
     * Called on successful API request
     * @param response Trivia Response of generic type T
     */
    void onSuccess(TriviaResponse<T> response);
    /**
     * Called on unsuccessful API request
     * @param response Trivia Response of Void type | Data will always be null
     * @param e Bubbled IOException from OkHTTP client | May be null
     */
    void onError(TriviaResponse<Void> response, IOException e);
}
