package kenny.net;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import kenny.base.EventCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.*;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Created by kennylbj on 16/9/14.
 * TcpServer.register(this)
 * Connection is bind to a specify Channel
 */
public class Connection extends AbstractEventHandler {
    private final static Logger LOG = LoggerFactory.getLogger(Connection.class);
    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }
    private final NIOEventLoop loop;
    private final String name;
    private final SocketChannel channel;

    //todo no volatile because single thread invoked guarantee
    // Connection state
    private State state;
    private boolean isReading;

    private EventCallback.MessageCallback messageCallback;
    private EventCallback.WriteCompleteCallback writeCompleteCallback;
    private EventCallback.HighWaterMarkCallback highWaterMarkCallback;
    private EventCallback.CloseCallback closeCallback;
    private EventCallback.ConnectFinishCallback connectFinishCallback;

    private final ByteBuffer inputBuffer;

    // The unbounded queue of outstanding packets that need to be sent
    // Carefully check the size of queue before offering packets into it
    // to avoid the unbounded-growth of queue
    private final Queue<ByteBuffer> outputBuffer;

    private int highWaterMark;

    public Connection(NIOEventLoop loop, String name, SocketChannel channel) {
        this.loop = loop;
        this.name = name;
        this.channel = channel;
        //TODO determine size
        inputBuffer = ByteBuffer.allocate(65536);
        outputBuffer = new LinkedList<>();
        state = State.CONNECTING;
        isReading = false;
        highWaterMark = 64*1024*1024;//default high water mark
    }

    // We could not simply use socketChanel.isConnected() to tell whether the socketChannel
    // is connected or not, since:
    // SocketChannel.socket().isConnected() and SocketChannel.isConnected()
    // return false before the socket is connected.
    // Once the socket is connected they will return true,
    // they will not revert to false for any reason.
    // It violates what is documented in
    // http://docs.oracle.com/javase/7/docs/api/java/nio/channels/SocketChannel.html#isConnected()
    // Consider it is a JAVA bug
    public boolean isConnected() {
        return state == State.CONNECTED;
    }

    public boolean disConnected() {
        return state == State.DISCONNECTED;
    }

    public void connectEstablished() {
        loop.assertInLoopThread();
        LOG.info("Connection established");
        checkState(state == State.CONNECTING);
        setState(State.CONNECTED);

        //TODO enable reading failed???
        //loop.enableReading(channel, this);
        try {
            loop.registerRead(channel, this);
        } catch (IOException e) {
            System.out.println("Cant register read.");
        }

        //TODO rename to connectionEstablished ?
        if (connectFinishCallback != null) {
            connectFinishCallback.onConnectFinish(this);
        }
    }

    public void connectDestroyed() {
        loop.assertInLoopThread();
        if (state == State.CONNECTED) {
            loop.removeAllInterest(channel);
            setState(State.DISCONNECTED);
            //TODO connectionCallback?
        }
        //TODO should we close it?
        //channel.close();
    }

    //TODO may be called from other thread
    public void shutdown() {
        if (state == State.CONNECTED) {
            setState(State.DISCONNECTING);
            loop.runInLoop(this::shutdownInLoop);
        }
    }

    private void shutdownInLoop() {
        loop.assertInLoopThread();
        loop.removeAllInterest(channel);
        try {
            channel.close();
            closeCallback.onClose(channel);
        } catch (IOException e) {
            LOG.error("Failed to shutdown Connection {}", name);
        }

    }

    public void send(ByteBuffer buffer) {
        if (state == State.CONNECTED) {
            if (loop.isInLoopThread()) {
                sendInLoop(buffer);
            } else {
                loop.runInLoop(() -> sendInLoop(buffer));
            }
        } else {
            LOG.error("Connect state error.");
        }
    }


    private void sendInLoop(ByteBuffer buffer) {
        loop.assertInLoopThread();
        if (state == State.DISCONNECTED) {
            return;
        }

        // channel is writable
        if (!loop.isWriteRegistered(channel) && outputBuffer.isEmpty()) {
            try {
                channel.write(buffer);
                if (!buffer.hasRemaining() && writeCompleteCallback != null) {
                    //queueInLoop because we don't want to invoke callback right now in case of long term write
                    loop.queueInLoop(() -> writeCompleteCallback.onWriteComplete(channel, this));
                }
            } catch (IOException e) {
                handleError(channel);
                throw new RuntimeException("Failed to write buffer");
            }
        }

        if (buffer.hasRemaining()) {
            int remaining = outputBuffer.stream()
                    .mapToInt(java.nio.Buffer::remaining)
                    .sum();
            int waterMark = remaining + buffer.remaining();
            if (waterMark > highWaterMark
                    && remaining < highWaterMark //guarantee highWaterMarkCallback only be called first time.
                    && highWaterMarkCallback != null) {
                //TODO why queueInLoop?
                loop.queueInLoop(() -> highWaterMarkCallback.onHighWaterMark(channel, waterMark));
            }
            //insert the remaining buffer to queue.
            outputBuffer.offer(buffer);
            //enable writing
            loop.enableWriting(channel, this);
        }

    }

    public void startRead() {
        loop.runInLoop(this::startReadInLoop);
    }

    private void startReadInLoop() {
        loop.assertInLoopThread();
        if (!isReading) {
            isReading = true;
        }
        loop.enableReading(channel, this);
    }

    public void stopRead() {
        loop.runInLoop(this::stopReadInLoop);
    }

    private void stopReadInLoop() {
        loop.assertInLoopThread();
        if (isReading) {
            isReading = false;
        }
        loop.disableReading(channel);
    }

    @Override
    public void handleWrite(SelectableChannel writableChannel) {
        checkState(channel == writableChannel);
        loop.assertInLoopThread();
        if (loop.isWriteRegistered(channel)) {
            try {
                //TODO how many buffer should I write?
                while (!outputBuffer.isEmpty()) {
                    //System.out.println("output queue size: " + outputBuffer.size());
                    ByteBuffer buffer = checkNotNull(outputBuffer.peek());
                    channel.write(buffer);
                    if (buffer.hasRemaining()) {
                        // Partial writing, we would break since we could not write more data on socket.
                        // But we have set the next start point of ByteBuffer.
                        // Next time when the socket is writable, it will start from that point.
                        break;
                    } else {
                        // Fully write
                        outputBuffer.poll();
                    }
                }
            } catch (IOException e) {
                handleError(channel);
                throw new RuntimeException("Failed to handle write");
            }
            if (outputBuffer.isEmpty()) {
                loop.unregisterWrite(channel);
                if (writeCompleteCallback != null) {
                    //TODO WHY NOT call callback directly since we are already in loop thread?
                    loop.queueInLoop(() -> writeCompleteCallback.onWriteComplete(channel, this));
                }
            }
        } else {
            handleError(channel);
            throw new RuntimeException("Handle write state error");
        }
    }

    @Override
    public void handleRead(SelectableChannel readableChannel) {
        checkState(channel == readableChannel);
        loop.assertInLoopThread();
        // We will not read data anymore if inputBuffer is full.
        // This will cause system's tcp receive buffer to be full and then
        // Endpoint's send buffer to be full.
        if (!inputBuffer.hasRemaining()) {
            LOG.info("Input buffer is full");
            onMessage();
            return;
        }
        try {
            //todo no enough space to read, should we make inputBuffer to be a list?
            int read = channel.read(inputBuffer);
            if (read > 0) {
                onMessage();
            } else if (read == 0) {
                //todo
                // Something bad happens if the channel is readable
                // but reading length is 0
                handleClose(channel);
            } else {
                //Some connection may only connect without data
                //handleError(channel);
            }

        } catch (IOException e) {
            LOG.error("Failed to handle read.");
        }
    }

    @Override
    public void handleClose(SelectableChannel closeableChannel) {
        LOG.info("handle close with channel {}", closeableChannel);
        checkState(channel == closeableChannel);
        checkState(state == State.CONNECTED || state == State.DISCONNECTING);
        loop.assertInLoopThread();
        loop.removeAllInterest(channel);
        setState(State.DISCONNECTED);
        //TODO close channel?
        if (closeCallback != null) {
            closeCallback.onClose(channel);
        }
    }

    @Override
    public void handleError(SelectableChannel errorChannel) {
        checkState(channel == errorChannel);
        loop.assertInLoopThread();
        LOG.error("Channel {} is error", errorChannel);
    }

    private void setState(State state) {
        this.state = state;
    }

    private void onMessage() {
        // Make ByteBuffer ready to get
        inputBuffer.flip();
        if (messageCallback != null) {
            // MessageCallback may get as much data as it can
            messageCallback.onMessage(channel, this, inputBuffer);
        }
        // Compact buffer in case of next cycle handleRead
        inputBuffer.compact();
    }

    public String getName() {
        return name;
    }


    public void setHighWaterMark(int highWaterMark) {
        this.highWaterMark = highWaterMark;
    }


    public void setMessageCallback(EventCallback.MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public void setWriteCompleteCallback(EventCallback.WriteCompleteCallback writeCompleteCallback) {
        this.writeCompleteCallback = writeCompleteCallback;
    }

    public void setHighWaterMarkCallback(EventCallback.HighWaterMarkCallback highWaterMarkCallback,
                                         int highWaterMark) {
        this.highWaterMarkCallback = highWaterMarkCallback;
        this.highWaterMark = highWaterMark;
    }

    public void setCloseCallback(EventCallback.CloseCallback closeCallback) {
        this.closeCallback = closeCallback;
    }

    public void setConnectFinishCallback(EventCallback.ConnectFinishCallback connectFinishCallback) {
        this.connectFinishCallback = connectFinishCallback;
    }


}
