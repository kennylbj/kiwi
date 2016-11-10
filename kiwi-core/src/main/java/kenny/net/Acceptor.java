package kenny.net;

import kenny.base.EventCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by kennylbj on 16/9/14.
 * Used by Server
 */
public class Acceptor extends AbstractEventHandler {
    private final static Logger LOG = LoggerFactory.getLogger(Acceptor.class);
    private final NIOEventLoop loop;
    private final ServerSocketChannel acceptChannel;
    private EventCallback.AcceptCallback acceptCallback;

    public Acceptor(NIOEventLoop loop, String host, int port) throws IOException {
        this.loop = loop;
        acceptChannel = ServerSocketChannel.open();
        acceptChannel.configureBlocking(false);
        acceptChannel.socket().bind(new InetSocketAddress(host, port));
    }

    public void accept() {
        loop.assertInLoopThread();
        try {
            loop.registerAccept(acceptChannel, this);
        } catch (IOException e) {
            LOG.error("Failed to accept");
        }
    }

    public void setAcceptCallback(EventCallback.AcceptCallback callback) {
        this.acceptCallback = callback;
    }

    @Override
    public void handleAccept(SelectableChannel channel) {
        loop.assertInLoopThread();
        LOG.info("Accept channel {}", channel.toString());
        //same channel in fact.
        checkState(acceptChannel == channel);
        try {
            SocketChannel socketChannel = acceptChannel.accept();
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                socketChannel.socket().setTcpNoDelay(true);
                //socketChannel.socket().setSendBufferSize(1024);
                //socketChannel.socket().setReceiveBufferSize(1024);
                if (acceptCallback != null) {
                    //registered by Server
                    acceptCallback.onAccept(socketChannel);
                    // Finish accept
                    boolean isChannelRead = (socketChannel.validOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ;
                    System.out.println("After accept " + System.identityHashCode(socketChannel) + " valid is read : " + isChannelRead);

                } else {
                    //nothing to do here, so we need to close connection.
                    socketChannel.close();
                }
            }
            //todo the special problem of accept()ing when you can't" in libev's doc.
        } catch (IOException e) {
            throw new RuntimeException("failed to handle accept " + channel.toString());
        }
    }
}
