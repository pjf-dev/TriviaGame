package ung.csci3660.fall2024.triviagame.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Trivia Response class of generic type T for use with Trivia Callbacks
 * @param <T> type for response data, usually a list of TriviaQuestions
 * @see TriviaQuestion
 * @see Callback
 */
public class Response<T> {

    public final Type type;
    public final T data;

    /**
     * Package Private constructor from API response code
     * Automatically converts code to {@link Response.Type}
     * @param code int code of API response
     * @param data Response data of type T
     */
    Response(int code, T data) {
        this.type = Type.fromCode(code);
        this.data = data;
    }

    /**
     * Package Private constructor from Response {@link Response.Type}
     * @param type type of response from API
     * @param data Response data of type T
     */
    Response(Type type, T data) {
        this.type = type;
        this.data = data;
    }

    /**
     * TriviaResponse Type Enum
     * Provides status codes and messages for responses from the Trivia API
     */
    public enum Type {
        SUCCESS(0, null),
        NO_RESULT(1, "API returned no results for query"),
        INVALID_PARAMETER(2, "Request contained invalid parameter(s)"),
        TOKEN_NOT_FOUND(3, "Request containing session token not found"),
        TOKEN_EMPTY(4, "No token was sent when server was expecting token"),
        RATE_LIMIT(5, "Exceeded rate limit for requesting IP"),
        SERVER_ERROR(-1, "Unexpected error occurred on the server"),
        BAD_RESPONSE(-3, "Server sent back an invalid response body"),
        UNKNOWN(-2, null);

        public final int code;
        public final String msg;
        Type(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        // create code hash map and populate with our Type enum values for easy conversion later
        private static final Map<Integer, Type> codeMap = new HashMap<>();
        // Didn't know you could do this, found it after some research on Google and StackOverflow
        static {
            for (Type type : Type.values()) {
                codeMap.put(type.code, type);
            }
        }

        /**
         * Get TriviaResponse Type enum from API code
         * @param code API code
         * @return Trivia Response Type enum
         */
        public static Type fromCode(int code) {
            return codeMap.getOrDefault(code, UNKNOWN);
        }
    }

}
