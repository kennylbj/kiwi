package org.kenny.discard;

import kenny.net.KiwiServer;
import kenny.net.NIOEventLoop;

import java.io.IOException;

/**
 * Created by kennylbj on 16/9/19.
 */
public class DiscardServer {
    private final KiwiServer server;
    public DiscardServer(NIOEventLoop loop, String host, int port) throws IOException {
        server = new KiwiServer(loop, "DiscardServer", host, port);
    }

    public void start() {
        server.setConnectFinishCallback(connection -> {
            System.out.println("Connection succeed.");
        });
        server.setMessageCallback((channel, connection, buffer) -> {
            byte[] message = new byte[buffer.remaining()];
            buffer.get(message);
            System.out.println("Receive message " + new String(message));
        });
        server.start();
    }

    public static void main(String[] args) throws IOException {
        NIOEventLoop loop = new NIOEventLoop();
        DiscardServer discardServer = new DiscardServer(loop, "localhost", 8110);
        discardServer.start();
        loop.loop();
    }

}
