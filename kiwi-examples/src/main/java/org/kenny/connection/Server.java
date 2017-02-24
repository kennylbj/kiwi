package org.kenny.connection;

import kenny.net.KiwiServer;
import kenny.net.NIOEventLoop;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;

/**
 * Created by kennylbj on 22/02/2017.
 */
public class Server {
    private final KiwiServer server;
    private long count = 0;
    private long received = 0;
    private Instant startTime = Instant.now();
    private long oldReceive = 0;
    private Server(NIOEventLoop loop, String host, int port) throws IOException {
        server = new KiwiServer(loop, "ConnectionServer", host, port);
        //server.setThreadNum(5);
    }

    private void start() {
        server.setConnectFinishCallback(connection -> {
            System.out.println("Socket connect " + count++);
        });

        server.setMessageCallback((channel, connection, buffer) -> {
            int newComes = buffer.remaining();
            byte[] message = new byte[newComes];
            received += newComes;
            buffer.get(message);
        });
        server.start();
    }

    public static void main(String[] args) throws IOException {
        NIOEventLoop loop = new NIOEventLoop();
        Server server = new Server(loop, "localhost", 8111);
        server.start();

        //monitoring system state
        final int amount = 1024 * 1024;
        OperatingSystemMXBean osmx = ManagementFactory.getOperatingSystemMXBean();
        loop.runEvery(() -> {
            double cpuUsage = osmx.getSystemLoadAverage() / osmx.getAvailableProcessors();
            int memoryUsage = (int)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / amount;
            System.out.println("Memory usage: [ " + memoryUsage + " ]; CPU usage: [ " + cpuUsage + " ]"
            + " totally received: " + server.received);
        }, 5);

        // monitoring throughput each 3 seconds.
        loop.runEvery(() -> {
            Instant endTime = Instant.now();
            long newReceive = server.received;
            long dataFlow = newReceive - server.oldReceive;
            double elapsed = endTime.toEpochMilli() - server.startTime.toEpochMilli();
            System.out.printf("Throughput: [%4.3f MiB/s]\n", dataFlow/elapsed/1024/1024*1000);
            server.startTime = endTime;
            server.oldReceive = newReceive;
        }, 3);
        loop.loop();
    }
}
