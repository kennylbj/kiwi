package kenny.net.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kennylbj on 16/10/4.
 */
public class HeapBufferFactory extends AbstractBufferFactory {

    private static final HeapBufferFactory INSTANCE_BE =
            new HeapBufferFactory(ByteOrder.BIG_ENDIAN);

    private static final HeapBufferFactory INSTANCE_LE =
            new HeapBufferFactory(ByteOrder.LITTLE_ENDIAN);

    public static HeapBufferFactory getInstance() {
        return INSTANCE_BE;
    }

    public static HeapBufferFactory getInstance(ByteOrder endianness) {
        if (endianness == ByteOrder.BIG_ENDIAN) {
            return INSTANCE_BE;
        } else if (endianness == ByteOrder.LITTLE_ENDIAN) {
            return INSTANCE_LE;
        } else if (endianness == null) {
            throw new NullPointerException("endianness");
        } else {
            throw new IllegalStateException("Should not reach here");
        }
    }


    /**
     * Creates a new factory whose default {@link ByteOrder} is
     * {@link ByteOrder#BIG_ENDIAN}.
     */
    public HeapBufferFactory() {
    }

    /**
     * Creates a new factory with the specified default {@link ByteOrder}.
     *
     * @param defaultOrder the default {@link ByteOrder} of this factory
     */
    public HeapBufferFactory(ByteOrder defaultOrder) {
        super(defaultOrder);
    }


    @Override
    public Buffer getBuffer(ByteOrder endianness, int capacity) {
        return null;
    }

    @Override
    public Buffer getBuffer(ByteOrder endianness, byte[] array, int offset, int length) {
        return null;
    }

    @Override
    public Buffer getBuffer(ByteBuffer nioBuffer) {
        return null;
    }
}
