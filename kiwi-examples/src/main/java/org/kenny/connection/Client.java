package org.kenny.connection;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by kennylbj on 22/02/2017.
 */
public class Client {
    private static final int SOCKET_NUM = 2048;
    public static void main(String[] args) {
        try {
            for (int i = 0; i < SOCKET_NUM; i++) {
                new Socket("localhost", 8111);
            }
            System.out.println("Connect completed!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Connect failed.");
        }
    }
}
