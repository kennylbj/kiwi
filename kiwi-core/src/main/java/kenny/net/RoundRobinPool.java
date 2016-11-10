package kenny.net;

import com.google.common.collect.Lists;
import net.jcip.annotations.NotThreadSafe;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kennylbj on 16/9/18.
 * Single thread access guarantee
 */
@NotThreadSafe
public class RoundRobinPool<T extends EventLoop> implements EventLoopPool {
    private final List<T> loops = Lists.newLinkedList();
    private final EventLoop basicLoop;
    private final ExecutorService service;
    private final Class<T> loopClass;
    private int threadNum = 0;
    private int position = 0;

    public RoundRobinPool(EventLoop basicLoop) {
        this.basicLoop = basicLoop;
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) basicLoop.getClass();
        this.loopClass = clazz;
        service = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void start() {
        basicLoop.assertInLoopThread();
        for (int i = 0; i < threadNum; i++) {
            T loop = EventLoop.newInstance(loopClass);
            service.submit(loop::loop);
            loops.add(loop);
        }
    }

    @Override
    public EventLoop nextLoop() {
        basicLoop.assertInLoopThread();
        EventLoop loop = basicLoop;
        //if loops is not empty, return loops's event loop.
        if (!loops.isEmpty()) {
            loop = loops.get(position);
            if (++position == loops.size()) {
                position = 0;
            }
        }
        return loop;
    }

    @Override
    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }


}
