package ung.csci3660.fall2024.triviagame.api;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Simple wrapper for {@link java.util.concurrent.ScheduledFuture} with unknown type that we don't care about
 */
public class APITask {

    private final ScheduledFuture<?> task;

    public APITask(ScheduledFuture<?> future) {
        this.task = future;
    }

    public void cancel(boolean mayInterruptIfRunning) {
        task.cancel(mayInterruptIfRunning);
    }

    public boolean isDone() {
        return task.isDone();
    }

    public long getDelay(TimeUnit unit) {
        return task.getDelay(unit);
    }

}
