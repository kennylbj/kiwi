package org.kenny.connection;

import kenny.net.KiwiClient;
import kenny.net.NIOEventLoop;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by kennylbj on 23/02/2017.
 */
public class Client {
    private final KiwiClient client;
    private ByteBuffer message;

    private Client(NIOEventLoop loop, String host, int port) throws IOException {
        client = new KiwiClient(loop, "Client", host, port);
        message = ByteBuffer.allocate(64*64);
    }

    private void connect() {
        client.setConnectFinishCallback(connection -> {
            System.out.println("Connect finish with conn " + connection);
            connection.send(message);
            message.flip();

        });

        client.setWriteCompleteCallback((channel, connection) -> {
            connection.send(message);
            message = ByteBuffer.allocate(64*64);
        });

        client.setHighWaterMarkCallback((channel, waterMark) -> {
            System.out.println("HighWaterMark!!");
            //client.disConnect();
        }, 65536);
        client.connect();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Start client");
        NIOEventLoop loop = new NIOEventLoop();
        Client client = new Client(loop, "localhost", 8111);
        client.connect();
        loop.loop();
    }
}
