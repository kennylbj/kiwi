package kenny.base;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by kennylbj on 16/9/14.
 * Timer implemented by PriorityBlockingQueue
 */
@ThreadSafe
public class PriorityTimer implements Timer {
    private final static long SECONDS_TO_NANOSECONDS = 1000_000_000;
    private final static long MILLISECONDS_TO_NANOSECONDS = 1000_1000;
    private final static long INFINITE_FUTURE = Integer.MAX_VALUE;

    private final BlockingQueue<TimerTask> timers;

    public PriorityTimer() {
        timers = new PriorityBlockingQueue<>();
    }

    @Override
    public TimerId runAfter(Runnable r, long delay) {
        return runAfterInNano(r, delay * SECONDS_TO_NANOSECONDS);
    }

    @Override
    public TimerId runEvery(Runnable r, long interval) {
        return runEveryInNano(r, interval * SECONDS_TO_NANOSECONDS);
    }

    @Override
    public void cancelTimer(TimerId timerId) {
        timers.remove(TimerTask.valueOf(timerId));
    }

    @Override
    public long getNextTimeoutIntervalMs() {
        long nextTimeoutIntervalMs = INFINITE_FUTURE;
        if (!timers.isEmpty()) {
            // The time recorded in timer is in nano-seconds. We have to convert it to milli-seconds
            // We need to ceil the result to avoid early wake up
            nextTimeoutIntervalMs =
                    (timers.peek().getExpirationTime() - System.nanoTime()
                            + MILLISECONDS_TO_NANOSECONDS) / MILLISECONDS_TO_NANOSECONDS;
        }
        return nextTimeoutIntervalMs;
    }

    @Override
    public void triggerExpiredTimers(long currentTime) {
        while (!timers.isEmpty()) {
            long nextExpiredTime = timers.peek().getExpirationTime();
            if (nextExpiredTime <= currentTime) {
                TimerTask timerTask = timers.poll();
                timerTask.getTask().run();
                //reinsert timer task if it's repeat task
                if (timerTask.getIntervalTime() > 0) {
                    timers.add(TimerTask.nextTimerTask(timerTask));
                }
            } else {
                return;
            }
        }
    }

    private TimerId runAfterInNano(Runnable r, long delayInNano) {
        checkNotNull(r);
        checkArgument(delayInNano >= 0);
        long expirationNs = System.nanoTime() + delayInNano;
        return doInsertTimerTask(r, expirationNs, 0);
    }

    private TimerId runEveryInNano(Runnable r, long interval) {
        checkNotNull(r);
        checkArgument(interval >= 0);
        long expirationNs = System.nanoTime() + interval;
        return doInsertTimerTask(r, expirationNs, interval);
    }

    private TimerId doInsertTimerTask(Runnable r, long expiration, long interval) {
        TimerTask timerTask = TimerTask.build(expiration, interval, r);
        checkState(timers.add(timerTask));
        return timerTask.getTimerId();
    }
}
