package kenny.net;


import net.jcip.annotations.NotThreadSafe;

/**
 * Created by kennylbj on 16/9/18.
 * Implements of this interface should NOT be thread safe
 */
@NotThreadSafe
interface EventLoopPool {

    void start();

    EventLoop nextLoop();

    void setThreadNum(int threadNum);
}
