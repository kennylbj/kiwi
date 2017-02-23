package kenny.net;

import kenny.base.PriorityTimer;
import net.jcip.annotations.GuardedBy;
import kenny.base.Timer;
import kenny.base.TimerId;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by kennylbj on 16/9/14.
 */
public abstract class EventLoop {
    @GuardedBy("this")
    private final ArrayList<Runnable> tasks;
    protected final Timer timer;
    private final long tid;
    private volatile boolean exist = false;
    private boolean handlingTasks = false;

    public EventLoop() {
        this.tasks = new ArrayList<>();
        this.timer = new PriorityTimer();
        this.tid = Thread.currentThread().getId();
    }

    public void loop() {
        assertInLoopThread();
        while (!exist) {
            doWait();
            executeTasks();
            triggerExpiredTimers();
        }
    }

    public void runInLoop(Runnable r) {
        checkNotNull(r);
        if (isInLoopThread()) {
            r.run();
        } else {
            queueInLoop(r);
        }
    }

    public void queueInLoop(Runnable r) {
        checkNotNull(r);
        synchronized (this) {
            tasks.add(r);
        }
        // If looper is handling tasks, new task will no be executed unless wakeup.
        if (!isInLoopThread() || handlingTasks) {
            wakeUp();
        }
    }

    public TimerId runAfter(Runnable r, long delay) {
        return timer.runAfter(r, delay);
    }

    public TimerId runEvery(Runnable r, long interval) {
        return timer.runEvery(r, interval);
    }

    public void cancelTimer(TimerId timerId) {
        timer.cancelTimer(timerId);
    }

    public void assertInLoopThread() {
        checkState(isInLoopThread(), "no in loop thread");
    }

    public boolean isInLoopThread() {
        return Thread.currentThread().getId() == tid;
    }

    protected abstract void doWait();

    protected abstract void wakeUp();

    // This cast is correct because the copy and the tasks
    // are the same type.
    @SuppressWarnings("unchecked")
    private void executeTasks() {
        handlingTasks = true;
        List<Runnable> tasksCopy;
        synchronized (this) {
            tasksCopy = (List<Runnable>) tasks.clone();
            tasks.clear();
        }
        tasksCopy.forEach(Runnable::run);
        handlingTasks = false;
    }

    private void triggerExpiredTimers() {
        timer.triggerExpiredTimers(System.nanoTime());
    }

    //FIXME
    public static <T extends EventLoop> T newInstance(Class<T> clazz) {
        T loop;
        try {
            loop = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to instance EventLoop " + clazz.toGenericString());
        }
        return loop;
    }

}
