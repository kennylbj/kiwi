package org.kenny.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by kennylbj on 22/02/2017.
 */
public class PlainClient {
    private static final int SOCKET_NUM = 2048;
    private static final byte message[] = new byte[1024*1024];

    private void sendMessage(OutputStream os) throws IOException {
        os.write(message);
    }

    public static void main(String[] args) {

        /*
        IntStream.range(0, SOCKET_NUM).forEach($ -> {
            try {
                Socket socket = new Socket("localhost", 8111);
                new PlainClient().sendMessage(socket.getOutputStream());
            } catch (IOException e) {
                System.out.println("Connection failed.");
            }
        });


        try {
            Socket socket = new Socket("localhost", 8111);
            PlainClient client = new PlainClient();
            while (true) {
                client.sendMessage(socket.getOutputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        List<Socket> sockets = IntStream.range(0, SOCKET_NUM).mapToObj(i -> {
            try {
                return new Socket("localhost", 8111);
            } catch (IOException e) {
                System.out.println("Connection failed.");
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());


        sockets.parallelStream().forEach(socket -> {
            try {
                new PlainClient().sendMessage(socket.getOutputStream());
            } catch (IOException e) {
                System.out.println("Connection failed.");
            }
        });

        System.out.println("Connect completed!");

    }
}
