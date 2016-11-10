package kenny.net;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.jcip.annotations.NotThreadSafe;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kennylbj on 16/9/15.
 * Buffer helper class for manage ByteBuffer as a Queue.
 * This class is not thread safe so Single thread invoked
 * must be guaranteed in high level logic.
 */
@NotThreadSafe
public class Buffer {
    private final List<ByteBuffer> buffers;
    private final ByteBuffer inputBuffer;
    public Buffer() {
        buffers = new LinkedList<>();
        inputBuffer = ByteBuffer.allocate(65535);
    }

    public boolean hasRemaining() {
        return !buffers.isEmpty();
    }

    public int remaining() {
        return buffers.stream()
                .mapToInt(java.nio.Buffer::remaining)
                .sum();
    }

    public int read(SocketChannel channel) {
        //if (inputBuffer.remaining())
        return 1;
    }

    public void put(ByteBuffer buffer) {
        checkNotNull(buffer);
        buffers.add(buffer);
    }

    public int getBufferSize() {
        return buffers.size();
    }


    public static void main(String[] args) {
        Buffer buffer = new Buffer();
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(100);
        byteBuffer1.putInt(10);
        byteBuffer1.putInt(20);
        byteBuffer1.flip();
        System.out.println("ByteBuffer1 remaining " + byteBuffer1.remaining());

        buffer.put(byteBuffer1);
        System.out.println(buffer.remaining() + " size " + buffer.getBufferSize());

        ByteBuffer byteBuffer2 = ByteBuffer.allocate(100);
        byteBuffer2.putInt(30);
        byteBuffer2.flip();
        System.out.println("ByteBuffer2 remaining " + byteBuffer2.remaining());

        buffer.put(byteBuffer2);
        System.out.println(buffer.remaining() + " size " + buffer.getBufferSize());

        ByteBuffer byteBuffer3 = ByteBuffer.allocate(2048);
        for (int i = 0; i < 512; i++) {
            byteBuffer3.putInt(i);
        }
        //checkState(!byteBuffer3.hasRemaining());
        byteBuffer3.flip();
        System.out.println("ByteBuffer3 remaining " + byteBuffer3.remaining());

        buffer.put(byteBuffer3);
        System.out.println(buffer.remaining() + " size " + buffer.getBufferSize());

        ByteBuffer b1 = ByteBuffer.allocate(1024);
        b1.putInt(10);
        b1.putChar('a');
        System.out.println("b1 remaining " + b1.remaining() + " position " + b1.position());
        ByteBuffer b2 = b1.asReadOnlyBuffer();
        System.out.println("b2 remaining " + b2.remaining() + " position " + b2.position());
        b2.flip();
        System.out.println("b1 remaining " + b1.remaining() + " position " + b1.position());
        System.out.println("b2 remaining " + b2.remaining() + " position " + b2.position());

        System.out.println("int " + b2.getInt() + " and remaining " + b2.remaining() + " position " + b1.position());
        System.out.println("char " + b2.getChar() + " and remaining " + b2.remaining() + " position " + b1.position());

        ByteBuffer b3 = ByteBuffer.allocate(1024);
        b3.putChar('a');




    }


}
