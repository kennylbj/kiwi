package kenny.net;

import kenny.base.EventCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by kennylbj on 16/9/17.
 * Used by Client
 */
public class Connector extends AbstractEventHandler {
    private final static Logger LOG = LoggerFactory.getLogger(Connector.class);
    private final NIOEventLoop loop;
    private final SocketChannel channel;
    private final InetSocketAddress address;
    private EventCallback.ConnectCallback connectCallback;

    // A flag to determine whether the socket is connected or not
    // We could not simply use socketChanel.isConnected() to tell whether the socketChannel
    // is connected or not, since:
    // SocketChannel.socket().isConnected() and SocketChannel.isConnected()
    // return false before the socket is connected.
    // Once the socket is connected they will return true,
    // they will not revert to false for any reason.
    // It violates what is documented in
    // http://docs.oracle.com/javase/7/docs/api/java/nio/channels/SocketChannel.html#isConnected()
    // Consider it is a JAVA bug
    private boolean isConnected;

    public Connector(NIOEventLoop loop, String host, int port) throws IOException {
        this.loop = loop;
        this.address = new InetSocketAddress(host, port);
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().setTcpNoDelay(true);
        //channel.socket().setSendBufferSize(1024);
        //channel.socket().setReceiveBufferSize(1024);
        isConnected = false;
    }

    public void start() {
        loop.runInLoop(this::startInLoop);
    }

    private void startInLoop() {
        loop.assertInLoopThread();
        if (!isConnected) {
            connect();
        }
    }

    private void connect() {
        try {
            // If the socketChannel has already connect to endpoint, call handleConnect()
            // Otherwise, registerConnect(), which will call handleConnect() when it is connectible
            if (channel.connect(address)) {
                //TODO Why handleConnect instead of onConnect?
                loop.runInLoop(() -> handleConnect(channel));
            } else {
                loop.registerConnect(channel, this);
            }
        } catch (IOException e) {
            LOG.error("Error connecting to remote endpoint: {}", address, e);
        }
    }

    @Override
    public void handleConnect(SelectableChannel connectChannel) {
        checkState(channel == connectChannel);
        loop.assertInLoopThread();
        try {

            if (channel.finishConnect()) {
                // If we finishConnect(), we have to unregisterConnect, otherwise there will be a bug
                // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4960791
                loop.unregisterConnect(channel);
            }
        } catch (IOException e) {
            LOG.info("Failed to FinishConnect to endpoint: {}", address, e);
            return;
        }
        //TODO move following statements to finishConnect block?
        isConnected = true;
        //TODO runInLoop?
        if (connectCallback != null) {
            connectCallback.onConnect(channel);
        }

    }

    public void setConnectCallback(EventCallback.ConnectCallback connectCallback) {
        this.connectCallback = connectCallback;
    }
}
