package org.kenny.connection;

import kenny.net.KiwiServer;
import kenny.net.NIOEventLoop;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by fishdicks on 22/02/2017.
 */
public class Server {
    private final KiwiServer server;
    private final AtomicLong connect = new AtomicLong(0);
    public Server(NIOEventLoop loop, String host, int port) throws IOException {
        server = new KiwiServer(loop, "ConnectionServer", host, port);
    }

    public void start() {
        server.setConnectFinishCallback(connection -> {
            System.out.println("Socket connect " + connect.getAndIncrement());
        });
        server.start();
    }

    public static void main(String[] args) throws IOException {
        NIOEventLoop loop = new NIOEventLoop();
        Server server = new Server(loop, "localhost", 8111);
        server.start();
        loop.loop();
    }
}
