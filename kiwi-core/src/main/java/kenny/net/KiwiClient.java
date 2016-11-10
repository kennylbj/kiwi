package kenny.net;

import com.google.common.base.Preconditions;
import net.jcip.annotations.ThreadSafe;
import kenny.base.EventCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kennylbj on 16/9/19.
 * KiwiClient may be called in multiple threads
 */
@ThreadSafe
public class KiwiClient {
    private final static Logger LOG = LoggerFactory.getLogger(KiwiClient.class);
    private final NIOEventLoop loop;
    private final String name;

    //in case of multi-threads start a server
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Connector connector;

    //TODO synchronized?
    private Connection currentConn;
    //default value is 0, no need to synchronize because in loop thread
    private int nextConnectionId = 0;

    //volatile for memory-visibility in multi-threads environment
    private volatile EventCallback.ConnectFinishCallback connectFinishCallback;
    private volatile EventCallback.MessageCallback messageCallback;
    private volatile EventCallback.WriteCompleteCallback writeCompleteCallback;
    private volatile EventCallback.HighWaterMarkCallback highWaterMarkCallback;
    private volatile int highWaterMark = 65536;

    public KiwiClient(NIOEventLoop loop, String name, String host, int port) throws IOException{
        this.loop = loop;
        this.name = name;
        connector = new Connector(loop, host, port);
    }

    public void connect() {
        if (started.compareAndSet(false, true)) {
            LOG.info("KiwiClient {} connect", name);
            connector.setConnectCallback(channel -> {
                loop.assertInLoopThread();
                String connName = name + "-" + channel.toString() + "#" + nextConnectionId++;
                LOG.info("ConnName is {}", connName);
                Connection connection = new Connection(loop, connName, (SocketChannel) channel);
                connection.setMessageCallback(messageCallback);
                connection.setWriteCompleteCallback(writeCompleteCallback);
                connection.setConnectFinishCallback(connectFinishCallback);
                if (highWaterMarkCallback != null) {
                    connection.setHighWaterMarkCallback(highWaterMarkCallback, highWaterMark);
                }
                connection.setCloseCallback(closeChannel -> {
                    loop.assertInLoopThread();
                    connection.connectDestroyed();
                    //TODO restart
                });
                currentConn = connection;
                //connection.connectEstablished(connectCallback);
                connection.connectEstablished();
            });

            connector.start();
        }
    }

    public void disConnect() {
        if (started.compareAndSet(true, false)) {
            LOG.info("KiwiClient {} disconnect", name);
            if (currentConn != null) {
                currentConn.shutdown();
            }
        }
    }

    public void setConnectFinishCallback(EventCallback.ConnectFinishCallback connectFinishCallback) {
        this.connectFinishCallback = connectFinishCallback;
    }

    public void setMessageCallback(EventCallback.MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public void setWriteCompleteCallback(EventCallback.WriteCompleteCallback writeCompleteCallback) {
        this.writeCompleteCallback = writeCompleteCallback;
    }

    public void setHighWaterMarkCallback(EventCallback.HighWaterMarkCallback highWaterMarkCallback, int highWaterMark) {
        Preconditions.checkNotNull(highWaterMarkCallback);
        Preconditions.checkArgument(highWaterMark > 0);
        this.highWaterMarkCallback = highWaterMarkCallback;
        this.highWaterMark = highWaterMark;
    }



}
