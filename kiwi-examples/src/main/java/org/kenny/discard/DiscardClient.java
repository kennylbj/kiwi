package org.kenny.discard;

import kenny.net.KiwiClient;
import kenny.net.NIOEventLoop;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by kennylbj on 16/9/19.
 */
public class DiscardClient {
    private final KiwiClient client;
    private final ByteBuffer message;

    public DiscardClient(NIOEventLoop loop, String host, int port) throws IOException {
        client = new KiwiClient(loop, "DiscardClient", host, port);
        String respond = "hello world";
        message = ByteBuffer.wrap(respond.getBytes(Charset.forName("UTF-8")));
    }

    public void connect() {
        client.setConnectFinishCallback(connection -> {
            System.out.println("Connect finish with conn " + connection);
            connection.send(message);
            message.flip();
        });

        client.setWriteCompleteCallback((channel, connection) -> {
            System.out.println("send message ");
            connection.send(message);
            message.flip();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Sleep failed", e);
            }
        });

        client.setHighWaterMarkCallback((channel, waterMark) -> {
            System.out.println("HighWaterMark!!");
            client.disConnect();
        }, 65536);

        client.connect();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Start discard client");
        NIOEventLoop loop = new NIOEventLoop();
        DiscardClient client = new DiscardClient(loop, "localhost", 8110);
        client.connect();
        loop.loop();
    }
}
