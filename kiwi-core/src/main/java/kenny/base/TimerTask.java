package kenny.base;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.jcip.annotations.ThreadSafe;

/**
 * Created by kennylbj on 16/9/14.
 * Timer In Nano Seconds
 */
@ThreadSafe
public final class TimerTask implements Comparable<TimerTask> {
    private final TimerId timerId;
    private final long expirationTime;
    private final long intervalTime;
    private final Runnable task;

    TimerTask(TimerId timerId, long expirationTime, long intervalTime, Runnable task) {
        this.timerId = timerId;
        this.expirationTime = expirationTime;
        this.intervalTime = intervalTime;
        this.task = task;
    }

    public static TimerTask build(long expirationTime, long intervalTime, Runnable task) {
        return new TimerTask(TimerId.generate(), expirationTime, intervalTime, task);
    }

    public static TimerTask valueOf(TimerId timerId) {
        return new TimerTask(timerId, 0, 0, null);
    }

    public static TimerTask nextTimerTask(TimerTask current) {
        return new TimerTask(current.getTimerId(),
                System.nanoTime() + current.intervalTime,
                current.intervalTime,
                current.task);
    }

    @Override
    public int compareTo(TimerTask other) {
        return Long.compare(expirationTime, other.expirationTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TimerTask)) {
            return false;
        }
        final TimerTask other = (TimerTask) obj;
        return timerId.equals(other.timerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(timerId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timerId", timerId)
                .add("expirationTime", expirationTime)
                .add("intervalTime", intervalTime)
                .toString();
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public Runnable getTask() {
        return task;
    }

    public TimerId getTimerId() {
        return timerId;
    }

    public long getIntervalTime() {
        return intervalTime;
    }

}