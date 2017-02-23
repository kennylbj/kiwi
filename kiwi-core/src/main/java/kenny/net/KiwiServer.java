package kenny.net;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.Maps;
import net.jcip.annotations.ThreadSafe;
import kenny.base.EventCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kennylbj on 16/9/17.
 * KiwiServer may be called in multiple threads
 */
@ThreadSafe
public class KiwiServer {
    private final static Logger LOG = LoggerFactory.getLogger(KiwiServer.class);
    private final NIOEventLoop loop;
    private final String name;
    private final String host;
    private final int port;
    private final EventLoopPool pool;

    private Acceptor acceptor;
    private final Map<String, Connection> connectionMap;


    //volatile for memory-visibility in multi-thread env
    private volatile EventCallback.AcceptCallback acceptCallback;
    private volatile EventCallback.MessageCallback messageCallback;
    private volatile EventCallback.WriteCompleteCallback writeCompleteCallback;
    private volatile EventCallback.ConnectFinishCallback connectFinishCallback;

    //in case of multi thread start a server
    private final AtomicBoolean started;

    //default value is 0, no need to synchronize because in loop thread
    private int nextConnectionId = 0;

    public KiwiServer(NIOEventLoop loop, String name, String host, int port) {
        this.loop = loop;
        this.name = name;
        this.host = host;
        this.port = port;
        connectionMap = Maps.newHashMap();
        started = new AtomicBoolean(false);
        pool = new RoundRobinPool<>(loop);
    }

    public void setThreadNum(int threadNum) {
        checkArgument(threadNum >= 0);
        pool.setThreadNum(threadNum);
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            LOG.info("Start KiwiServer {}", name);
            //single thread access
            //pool.start();
            loop.runInLoop(pool::start);
            try {
                acceptor = new Acceptor(loop, host, port);
            } catch (IOException e) {
                LOG.error("Acceptor failed with endPoint {}:{}", host, port, e);
                throw new RuntimeException("Acceptor failed.");
            }
            loop.runInLoop(acceptor::accept);

            acceptor.setAcceptCallback(acceptChannel -> {
                loop.assertInLoopThread();
                String connName = name + "-" + host + ":" + port + "#" + nextConnectionId++;
                LOG.info("Accept connection {}", connName);
                // Safe to call nextLoop because it's in loop thread
                NIOEventLoop ioLoop = (NIOEventLoop) pool.nextLoop();
                //TODO error?
                Connection connection = new Connection(ioLoop, connName, (SocketChannel) acceptChannel);
                connectionMap.put(connName, connection);
                connection.setMessageCallback(messageCallback);
                connection.setWriteCompleteCallback(writeCompleteCallback);
                connection.setConnectFinishCallback(connectFinishCallback);
                //TODO acceptEstablished with acceptCallback
                ioLoop.runInLoop(connection::connectEstablished);
            });
        }

    }

    //TODO setAcceptCallback for Connection
    public void setAcceptCallback(EventCallback.AcceptCallback acceptCallback) {
        this.acceptCallback = acceptCallback;
    }

    public void setMessageCallback(EventCallback.MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public void setWriteCompleteCallback(EventCallback.WriteCompleteCallback writeCompleteCallback) {
        this.writeCompleteCallback = writeCompleteCallback;
    }

    public void setConnectFinishCallback(EventCallback.ConnectFinishCallback connectFinishCallback) {
        this.connectFinishCallback = connectFinishCallback;
    }


}
