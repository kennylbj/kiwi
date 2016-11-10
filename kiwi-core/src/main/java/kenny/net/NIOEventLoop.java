package kenny.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by kennylbj on 16/9/14.
 */
public class NIOEventLoop extends EventLoop {
    private final static Logger LOG = LoggerFactory.getLogger(NIOEventLoop.class);
    private final Selector selector;

    public NIOEventLoop() throws IOException {
        selector = Selector.open();
    }

    @Override
    protected void doWait() {
        assertInLoopThread();
        // If timer task exists, the doWait() should wait not later than the time timer to execute
        // If no timer exists, we consider it will wait forever until other threads call wakeUp()
        // The nextTimeoutIntervalMs is in milli-seconds
        long nextTimeoutIntervalMs = timer.getNextTimeoutIntervalMs();
        // doWait(timeout), which in fact is implemented by selector.select(timeout), and it will
        // wake up, if other threads wake it up, it meets the timeout, one channel is selected, or
        // the current thread is interrupted.
        int events;
        try {
            if (nextTimeoutIntervalMs > 0) {
                // The select will take the timeout in unit of milli-seconds
                events = selector.select(nextTimeoutIntervalMs);
            } else {
                events = selector.selectNow();
            }
        } catch (IOException e) {
            LOG.info("Select failed.", e);
            throw new RuntimeException(e);
        }
        //wakeup and handle NIO events.
        handleNIOEvents();
    }

    @Override
    protected void wakeUp() {
        selector.wakeup();
    }

    private void handleNIOEvents() {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            EventHandler handler = (EventHandler) key.attachment();
            if (!key.isValid()) {
                // This method key.channel() will continue to return the channel even after the
                // key is cancelled.
                handler.handleError(key.channel());
                continue;
            }

            // We need to check whether the key is still valid since:
            // 1. The key could be cancelled by last operation
            // 2. The process might not fail-fast or throw exceptions after the key is cancelled
            if (key.isValid() && key.isWritable()) {
                handler.handleWrite(key.channel());
            }

            if (key.isValid() && key.isReadable()) {
                handler.handleRead(key.channel());
            }

            if (key.isValid() && key.isConnectable()) {
                handler.handleConnect(key.channel());
            }

            if (key.isValid() && key.isAcceptable()) {
                handler.handleAccept(key.channel());
            }

        }
    }

    public void removeAllInterest(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(selector);
        if (key != null) {
            key.cancel();
        }
    }

    public void enableReading(SelectableChannel channel, EventHandler handler) {
        if (!isReadRegistered(channel)) {
            try {
                registerRead(channel, handler);
            } catch (ClosedChannelException e) {
                handler.handleError(channel);
            }
        }
    }

    public void disableReading(SelectableChannel channel) {
        if (isReadRegistered(channel)) {
            unregisterRead(channel);
        }
    }

    public void enableWriting(SelectableChannel channel, EventHandler handler) {
        if (!isWriteRegistered(channel)) {
            try {
                registerWrite(channel, handler);
            } catch (ClosedChannelException e) {
                handler.handleError(channel);
            }
        }
    }

    public void disableWriting(SelectableChannel channel) {
        if (isWriteRegistered(channel)) {
            unregisterWrite(channel);
        }
    }

    /**
     * Followings are the register, unregister, isRegister for different operations for the selector and channel
     */
    public void registerRead(SelectableChannel channel, EventHandler callback)
            throws ClosedChannelException {
        assert channel.keyFor(selector) == null
                || (channel.keyFor(selector).interestOps() & SelectionKey.OP_CONNECT) == 0;
        System.out.println("begin to register read");
        addInterest(channel, SelectionKey.OP_READ, callback);
    }

    public void unregisterRead(SelectableChannel channel) {
        removeInterest(channel, SelectionKey.OP_READ);
    }

    public boolean isReadRegistered(SelectableChannel channel) {
        return isInterestRegistered(channel, SelectionKey.OP_READ);
    }

    public void registerConnect(SelectableChannel channel, EventHandler callback)
            throws ClosedChannelException {
        // This channel should be first use
        assert channel.keyFor(selector) == null;
        addInterest(channel, SelectionKey.OP_CONNECT, callback);
    }

    public void unregisterConnect(SelectableChannel channel) {
        removeInterest(channel, SelectionKey.OP_CONNECT);
    }

    public boolean isConnectRegistered(SelectableChannel channel) {
        return isInterestRegistered(channel, SelectionKey.OP_CONNECT);
    }

    public void registerAccept(SelectableChannel channel, EventHandler callback)
            throws ClosedChannelException {
        addInterest(channel, SelectionKey.OP_ACCEPT, callback);
    }

    public void unregisterAccept(SelectableChannel channel) {
        removeInterest(channel, SelectionKey.OP_ACCEPT);
    }

    public boolean isAcceptRegistered(SelectableChannel channel) {
        return isInterestRegistered(channel, SelectionKey.OP_ACCEPT);
    }

    public void registerWrite(SelectableChannel channel, EventHandler callback)
            throws ClosedChannelException {
        addInterest(channel, SelectionKey.OP_WRITE, callback);
    }

    public void unregisterWrite(SelectableChannel channel) {
        removeInterest(channel, SelectionKey.OP_WRITE);
    }

    public boolean isWriteRegistered(SelectableChannel channel) {
        return isInterestRegistered(channel, SelectionKey.OP_WRITE);
    }

    /**
     * Register an operation interest on a SelectableChannel, with EventHandler as callback attachment
     * There are two cases when trying to register an interest
     * 1. The whole key does not exist; no interests ever registered for this channel
     * 2. The key exists due to other interests registered but not the one we are adding
     * <p>
     * In 1st case, we just register this channel with operation on the given Selector
     * In 2nd case, we have to make sure the state of NIOLooper is clean:
     * 1. Key has to be valid
     * 2. The interest has not yet been registered
     * 3. If old attached EventHandler exists, it has to be the same as new one
     * If any one of above 3 conditions are not met, RuntimeException would be thrown.
     *
     * @param channel The Selectable to register operation interest
     * @param operation The operation interest to register
     * @param callback The Callback to handle
     * @throws ClosedChannelException if Channel is closed when trying to register an interest
     */
    private void addInterest(SelectableChannel channel,
                             int operation,
                             EventHandler callback)
            throws ClosedChannelException {
        SelectionKey key = channel.keyFor(selector);
        if (key == null) {
            System.out.println("register with " + operation + " " + callback);
            channel.register(selector, operation, callback);
        } else if (!key.isValid()) {
            throw new RuntimeException(
                    String.format("Unable to add %d in %s due to key is invalid", operation, channel));
        } else {
            // Key is not null and key is valid
            if ((key.interestOps() & operation) != 0) {
                throw new RuntimeException(
                        String.format("%d has been registered in %s", operation, channel));
            }
            /*
            if (key.attachment() == null) {
                System.out.println("attach callback");
                key.attach(callback);
            } else {
                if (callback != key.attachment()) {
                    throw new RuntimeException("Unmatched SelectHandler has already been attached"
                            + " for other operation");
                }
                // If call == key.attachment
                // Just skip
            }
            System.out.println("or interestOps");
            */
            key.attach(callback);
            key.interestOps(key.interestOps() | operation);
        }
    }

    /**
     * Remove one operation interest on a SelectableChannel.
     * The SelectableChannel has to be registered with Selector ahead.
     * Otherwise, NullPointerExceptions would throw
     * The key for SelectableChannel has to be valid.
     * Otherwise, InvalidValid Exception would throw.
     *
     * @param channel the SelectableChannel to remove operation interest
     * @param operation the interest to remove
     */
    private void removeInterest(SelectableChannel channel, int operation) {
        SelectionKey key = channel.keyFor(selector);
        //TODO attach null
        //key.attach(null);
        // Exception would be thrown if key is null or key is inValid
        // We do not need double check it ahead
        key.interestOps(key.interestOps() & (~operation));
    }

    /**
     * Check whether an operation interest was registered on a SelectableChannel
     * There are two cases that interest is not registered
     * 1. The whole key does not exist; no interests ever registered for this channel
     * 2. The key exists due to other interests registered but not the one we are adding
     * If the key exists, the key for SelectableChannel has to be valid.
     * Otherwise, InvalidValid Exception would throw.
     *
     * @param channel The Selectable to check
     * @param operation The operation interest to check
     */
    private boolean isInterestRegistered(SelectableChannel channel, int operation) {
        SelectionKey key = channel.keyFor(selector);

        return key != null && (key.interestOps() & operation) != 0;
    }
}
