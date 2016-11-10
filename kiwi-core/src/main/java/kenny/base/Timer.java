package kenny.base;

/**
 * Created by kennylbj on 16/9/14.
 * Implements of this class must be thread-safe
 */
public interface Timer {
    TimerId runAfter(Runnable r, long delay);

    TimerId runEvery(Runnable r, long interval);

    void cancelTimer(TimerId timerId);

    long getNextTimeoutIntervalMs();

    void triggerExpiredTimers(long currentTime);
}
