package ung.csci3660.fall2024.triviagame.api;

import java.io.IOException;

/**
 * Trivia Callback interface
 * Calls {@link Callback#onSuccess(Response)} on successful API request
 * Calls {@link Callback#onError(Response, IOException)} otherwise
 * @param <T> type of {@link Response}
 */
public interface Callback<T> {
    /**
     * Called on successful API request
     * @param response Trivia Response of generic type T
     */
    void onSuccess(Response<T> response);
    /**
     * Called on unsuccessful API request
     * @param response Trivia Response of Void type | Data will always be null
     * @param e Bubbled IOException from OkHTTP client | May be null
     */
    void onError(Response<Void> response, IOException e);
}
