package org.kenny.pingpong;

import kenny.net.KiwiServer;
import kenny.net.NIOEventLoop;

import java.io.IOException;

/**
 * Created by kennylbj on 16/9/19.
 */
public class Server {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 7890;
        NIOEventLoop loop = new NIOEventLoop();
        KiwiServer server = new KiwiServer(loop, "PingPongServer", host, port);
        server.setMessageCallback((channel, connection, buffer) -> {
            System.out.println("on Message " + buffer.toString());
            connection.send(buffer);
        });
        server.start();
        loop.loop();
    }

}
