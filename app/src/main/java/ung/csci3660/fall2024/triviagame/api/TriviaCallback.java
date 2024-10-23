package ung.csci3660.fall2024.triviagame.api;

import java.io.IOException;

// TODO: Add Documentation
public interface TriviaCallback<T> {
    void onSuccess(TriviaResponse<T> response) throws IOException;
    void onError(TriviaResponse<?> response, IOException e);
}
