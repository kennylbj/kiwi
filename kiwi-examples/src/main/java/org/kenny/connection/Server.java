package org.kenny.connection;

import kenny.net.KiwiServer;
import kenny.net.NIOEventLoop;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kennylbj on 22/02/2017.
 */
public class Server {
    private final KiwiServer server;
    private final AtomicLong connect = new AtomicLong(0);
    private Server(NIOEventLoop loop, String host, int port) throws IOException {
        server = new KiwiServer(loop, "ConnectionServer", host, port);
        //server.setThreadNum(5);
    }

    private void start() {
        server.setConnectFinishCallback(connection ->
                System.out.println("Socket connect " + connect.getAndIncrement()));
        server.start();
    }

    public static void main(String[] args) throws IOException {
        NIOEventLoop loop = new NIOEventLoop();
        Server server = new Server(loop, "localhost", 8111);
        server.start();
        final int amount = 1024 * 1024;
        OperatingSystemMXBean osmx = ManagementFactory.getOperatingSystemMXBean();
        loop.runEvery(() -> {
            double cpuUsage = osmx.getSystemLoadAverage() / osmx.getAvailableProcessors();
            int memoryUsage = (int)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / amount;
            System.out.println("Memory usage: [ " + memoryUsage + " ]; CPU usage: [ " + cpuUsage + " ].");
        }, 5);
        loop.loop();
    }
}
