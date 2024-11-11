package ung.csci3660.fall2024.triviagame.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Partially referencing https://developer.android.com/develop/background-work/background-tasks/asynchronous/java-threads

// Package private
class RateLimitedClient {

//    private final OkHttpClient client;
    private final long rateLimitMillis;
    private final ScheduledExecutorService executor;

    private long lastScheduledTaskTime; // Time at which the latest (most recent) task is/was scheduled to execute

    RateLimitedClient(long rateLimitMillis) {
//        client = new OkHttpClient();
        executor = Executors.newSingleThreadScheduledExecutor(); // We only need single threaded because we can only execute 1 task at a time
        lastScheduledTaskTime = System.currentTimeMillis() - rateLimitMillis; // We subtract rateLimitMillis to allow a task to be executed right away
        this.rateLimitMillis = rateLimitMillis;
    }

    APITask queueRequest(URL request, Callback callback) {
        long scheduledTaskTime = lastScheduledTaskTime + rateLimitMillis;
        long delay = scheduledTaskTime - System.currentTimeMillis(); // Can be negative, just means execute ASAP / now
        APITask task = new APITask(executor.schedule(() -> {
            // Builds off OkHTTP's Call/Callback classes to simplify async requests and errors
            // This is necessary / more optimal than relying on OkHTTP's Call#enqueue method since we queue requests ourselves and don't want to use extra resources
            try {
                HttpURLConnection connection = (HttpURLConnection) request.openConnection();
                callback.onResponse(connection);
            } catch (IOException e) {
                callback.onError(e);
            }
        }, delay < 0 ? 0 : delay, TimeUnit.MILLISECONDS));
        lastScheduledTaskTime = delay < 0 ? System.currentTimeMillis() : scheduledTaskTime; // Catch up to current time if we scheduled in the past
        return task;
    }

    void shutdown(boolean now) {
        if (now) {
            executor.shutdownNow();
        } else {
            executor.shutdown();
        }
    }

    interface Callback {
        void onResponse(HttpURLConnection connection);
        void onError(IOException e);
    }

}
