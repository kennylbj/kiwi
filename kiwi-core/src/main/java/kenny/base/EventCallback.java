package kenny.base;

import kenny.net.Connection;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

/**
 * Created by kennylbj on 16/9/14.
 */
public interface EventCallback {

    //TODO Connection instead of Channel
    interface MessageCallback extends EventCallback {
        void onMessage(SelectableChannel channel, Connection connection, ByteBuffer buffer);
    }

    interface AcceptCallback extends EventCallback {
        void onAccept(SelectableChannel channel);
    }

    interface ConnectCallback extends EventCallback {
        void onConnect(SelectableChannel channel);
    }

    interface ConnectFinishCallback extends EventCallback {
        void onConnectFinish(Connection connection);
    }

    interface HighWaterMarkCallback extends EventCallback {
        void onHighWaterMark(SelectableChannel channel, int waterMark);
    }

    interface WriteCompleteCallback extends EventCallback {
        void onWriteComplete(SelectableChannel channel, Connection connection);
    }

    interface CloseCallback extends EventCallback {
        void onClose(SelectableChannel channel);
    }
}
