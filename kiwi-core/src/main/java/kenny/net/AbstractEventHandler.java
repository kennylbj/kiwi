package kenny.net;

import java.nio.channels.SelectableChannel;

/**
 * Created by kennylbj on 16/9/14.
 * A skeletal implementation of a EventHandler.
 */
public class AbstractEventHandler implements EventHandler {
    @Override
    public void handleRead(SelectableChannel channel) {

    }

    @Override
    public void handleWrite(SelectableChannel channel) {

    }

    @Override
    public void handleAccept(SelectableChannel channel) {

    }

    @Override
    public void handleConnect(SelectableChannel channel) {

    }

    @Override
    public void handleError(SelectableChannel channel) {

    }

    @Override
    public void handleClose(SelectableChannel channel) {

    }
}
