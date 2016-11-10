package kenny.net.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * Created by kennylbj on 16/10/4.
 */
public class DynamicBuffer extends AbstractBuffer {

    private final BufferFactory factory;
    private final ByteOrder endianness;
    private Buffer buffer;

    public DynamicBuffer(int estimatedLength) {
        this(ByteOrder.BIG_ENDIAN, estimatedLength);
    }

    public DynamicBuffer(ByteOrder endianness, int estimatedLength) {
        this(endianness, estimatedLength, HeapBufferFactory.getInstance(endianness));
    }

    public DynamicBuffer(ByteOrder endianness, int estimatedLength, BufferFactory factory) {
        if (estimatedLength < 0) {
            throw new IllegalArgumentException("estimatedLength: " + estimatedLength);
        }
        if (endianness == null) {
            throw new NullPointerException("endianness");
        }
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        this.factory = factory;
        this.endianness = endianness;
        buffer = factory.getBuffer(order(), estimatedLength);
    }

    @Override
    public void ensureWritableBytes(int minWritableBytes) {
        if (minWritableBytes <= writableBytes()) {
            return;
        }

        int newCapacity;
        if (capacity() == 0) {
            newCapacity = 1;
        } else {
            newCapacity = capacity();
        }
        int minNewCapacity = writerIndex() + minWritableBytes;
        while (newCapacity < minNewCapacity) {
            newCapacity <<= 1;

            // Check if we exceeded the maximum size of 2gb if this is the case then
            // newCapacity == 0
            //
            // https://github.com/netty/netty/issues/258
            if (newCapacity == 0) {
                throw new IllegalStateException("Maximum size of 2gb exceeded");
            }
        }

        Buffer newBuffer = factory().getBuffer(order(), newCapacity);
        newBuffer.writeBytes(buffer, 0, writerIndex());
        buffer = newBuffer;
    }

    public BufferFactory factory() {
        return factory;
    }

    public ByteOrder order() {
        return endianness;
    }

    public boolean isDirect() {
        return buffer.isDirect();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public boolean hasArray() {
        return buffer.hasArray();
    }

    public byte[] array() {
        return buffer.array();
    }

    public int arrayOffset() {
        return buffer.arrayOffset();
    }

    public byte getByte(int index) {
        return buffer.getByte(index);
    }

    public short getShort(int index) {
        return buffer.getShort(index);
    }


    public int getInt(int index) {
        return buffer.getInt(index);
    }

    public long getLong(int index) {
        return buffer.getLong(index);
    }

    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        buffer.getBytes(index, dst, dstIndex, length);
    }

    public void getBytes(int index, Buffer dst, int dstIndex, int length) {
        buffer.getBytes(index, dst, dstIndex, length);
    }

    public void getBytes(int index, ByteBuffer dst) {
        buffer.getBytes(index, dst);
    }

    public int getBytes(int index, GatheringByteChannel out, int length)
            throws IOException {
        return buffer.getBytes(index, out, length);
    }

    public void getBytes(int index, OutputStream out, int length)
            throws IOException {
        buffer.getBytes(index, out, length);
    }

    public void setByte(int index, int value) {
        buffer.setByte(index, value);
    }

    public void setShort(int index, int value) {
        buffer.setShort(index, value);
    }

    public void setInt(int index, int value) {
        buffer.setInt(index, value);
    }

    public void setLong(int index, long value) {
        buffer.setLong(index, value);
    }

    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        buffer.setBytes(index, src, srcIndex, length);
    }

    public void setBytes(int index, Buffer src, int srcIndex, int length) {
        buffer.setBytes(index, src, srcIndex, length);
    }

    public void setBytes(int index, ByteBuffer src) {
        buffer.setBytes(index, src);
    }

    public int setBytes(int index, InputStream in, int length)
            throws IOException {
        return buffer.setBytes(index, in, length);
    }

    public int setBytes(int index, ScatteringByteChannel in, int length)
            throws IOException {
        return buffer.setBytes(index, in, length);
    }

    @Override
    public void writeByte(int value) {
        ensureWritableBytes(1);
        super.writeByte(value);
    }

    @Override
    public void writeShort(int value) {
        ensureWritableBytes(2);
        super.writeShort(value);
    }

    @Override
    public void writeInt(int value) {
        ensureWritableBytes(4);
        super.writeInt(value);
    }

    @Override
    public void writeLong(long value) {
        ensureWritableBytes(8);
        super.writeLong(value);
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        ensureWritableBytes(length);
        super.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes(Buffer src, int srcIndex, int length) {
        ensureWritableBytes(length);
        super.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes(ByteBuffer src) {
        ensureWritableBytes(src.remaining());
        super.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        ensureWritableBytes(length);
        return super.writeBytes(in, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel in, int length)
            throws IOException {
        ensureWritableBytes(length);
        return super.writeBytes(in, length);
    }

    @Override
    public void writeZero(int length) {
        ensureWritableBytes(length);
        super.writeZero(length);
    }

    @Override
    public Buffer duplicate() {
        return new DuplicatedBuffer(this);
    }

    @Override
    public Buffer copy(int index, int length) {
        DynamicBuffer copiedBuffer = new DynamicBuffer(order(), Math.max(length, 64), factory());
        copiedBuffer.buffer = buffer.copy(index, length);
        copiedBuffer.setIndex(0, length);
        return copiedBuffer;
    }

    @Override
    public Buffer slice(int index, int length) {
        if (index == 0) {
            if (length == 0) {
                return BufferHelpler.EMPTY_BUFFER;
            }
            return new TruncatedBuffer(this, length);
        } else {
            if (length == 0) {
                return BufferHelpler.EMPTY_BUFFER;
            }
            return new SlicedBuffer(this, index, length);
        }
    }

    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        return buffer.toByteBuffer(index, length);
    }
}
