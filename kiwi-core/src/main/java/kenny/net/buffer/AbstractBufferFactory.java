package kenny.net.buffer;

import java.nio.ByteOrder;

/**
 * Created by kennylbj on 16/10/4.
 */
public abstract class AbstractBufferFactory implements BufferFactory {
    private final ByteOrder defaultOrder;

    /**
     * Creates a new factory whose default {@link ByteOrder} is
     * {@link ByteOrder#BIG_ENDIAN}.
     */
    protected AbstractBufferFactory() {
        this(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Creates a new factory with the specified default {@link ByteOrder}.
     *
     * @param defaultOrder the default {@link ByteOrder} of this factory
     */
    protected AbstractBufferFactory(ByteOrder defaultOrder) {
        if (defaultOrder == null) {
            throw new NullPointerException("defaultOrder");
        }
        this.defaultOrder = defaultOrder;
    }


    @Override
    public Buffer getBuffer(int capacity) {
        return getBuffer(getDefaultOrder(), capacity);
    }

    @Override
    public Buffer getBuffer(byte[] array, int offset, int length) {
        return getBuffer(getDefaultOrder(), array, offset, length);
    }

    @Override
    public ByteOrder getDefaultOrder() {
        return defaultOrder;
    }
}
