package kenny.net.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * Created by kennylbj on 16/10/18.
 */
public class SlicedBuffer extends AbstractBuffer implements WrappedBuffer {
    private final Buffer buffer;
    private final int adjustment;
    private final int length;

    public SlicedBuffer(Buffer buffer, int index, int length) {
        if (index < 0 || index > buffer.capacity()) {
            throw new IndexOutOfBoundsException("Invalid index of " + index
                    + ", maximum is " + buffer.capacity());
        }

        if (index + length > buffer.capacity()) {
            throw new IndexOutOfBoundsException("Invalid combined index of "
                    + (index + length) + ", maximum is " + buffer.capacity());
        }

        this.buffer = buffer;
        adjustment = index;
        this.length = length;
        writerIndex(length);
    }

    public Buffer unwrap() {
        return buffer;
    }

    public BufferFactory factory() {
        return buffer.factory();
    }

    public ByteOrder order() {
        return buffer.order();
    }

    public boolean isDirect() {
        return buffer.isDirect();
    }

    public int capacity() {
        return length;
    }

    public boolean hasArray() {
        return buffer.hasArray();
    }

    public byte[] array() {
        return buffer.array();
    }

    public int arrayOffset() {
        return buffer.arrayOffset() + adjustment;
    }

    public byte getByte(int index) {
        checkIndex(index);
        return buffer.getByte(index + adjustment);
    }

    public short getShort(int index) {
        checkIndex(index, 2);
        return buffer.getShort(index + adjustment);
    }


    public int getInt(int index) {
        checkIndex(index, 4);
        return buffer.getInt(index + adjustment);
    }

    public long getLong(int index) {
        checkIndex(index, 8);
        return buffer.getLong(index + adjustment);
    }

    public Buffer duplicate() {
        Buffer duplicate = new SlicedBuffer(buffer, adjustment, length);
        duplicate.setIndex(readerIndex(), writerIndex());
        return duplicate;
    }

    public Buffer copy(int index, int length) {
        checkIndex(index, length);
        return buffer.copy(index + adjustment, length);
    }

    public Buffer slice(int index, int length) {
        checkIndex(index, length);
        if (length == 0) {
            return BufferHelpler.EMPTY_BUFFER;
        }
        return new SlicedBuffer(buffer, index + adjustment, length);
    }

    public void getBytes(int index, Buffer dst, int dstIndex, int length) {
        checkIndex(index, length);
        buffer.getBytes(index + adjustment, dst, dstIndex, length);
    }

    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        checkIndex(index, length);
        buffer.getBytes(index + adjustment, dst, dstIndex, length);
    }

    public void getBytes(int index, ByteBuffer dst) {
        checkIndex(index, dst.remaining());
        buffer.getBytes(index + adjustment, dst);
    }

    public void setByte(int index, int value) {
        checkIndex(index);
        buffer.setByte(index + adjustment, value);
    }

    public void setShort(int index, int value) {
        checkIndex(index, 2);
        buffer.setShort(index + adjustment, value);
    }


    public void setInt(int index, int value) {
        checkIndex(index, 4);
        buffer.setInt(index + adjustment, value);
    }

    public void setLong(int index, long value) {
        checkIndex(index, 8);
        buffer.setLong(index + adjustment, value);
    }

    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        checkIndex(index, length);
        buffer.setBytes(index + adjustment, src, srcIndex, length);
    }

    public void setBytes(int index, Buffer src, int srcIndex, int length) {
        checkIndex(index, length);
        buffer.setBytes(index + adjustment, src, srcIndex, length);
    }

    public void setBytes(int index, ByteBuffer src) {
        checkIndex(index, src.remaining());
        buffer.setBytes(index + adjustment, src);
    }

    public void getBytes(int index, OutputStream out, int length)
            throws IOException {
        checkIndex(index, length);
        buffer.getBytes(index + adjustment, out, length);
    }

    public int getBytes(int index, GatheringByteChannel out, int length)
            throws IOException {
        checkIndex(index, length);
        return buffer.getBytes(index + adjustment, out, length);
    }

    public int setBytes(int index, InputStream in, int length)
            throws IOException {
        checkIndex(index, length);
        return buffer.setBytes(index + adjustment, in, length);
    }

    public int setBytes(int index, ScatteringByteChannel in, int length)
            throws IOException {
        checkIndex(index, length);
        return buffer.setBytes(index + adjustment, in, length);
    }

    public ByteBuffer toByteBuffer(int index, int length) {
        checkIndex(index, length);
        return buffer.toByteBuffer(index + adjustment, length);
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= capacity()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index
                    + ", maximum is " + capacity());
        }
    }

    private void checkIndex(int startIndex, int length) {
        if (length < 0) {
            throw new IllegalArgumentException(
                    "length is negative: " + length);
        }
        if (startIndex < 0) {
            throw new IndexOutOfBoundsException("startIndex cannot be negative");
        }
        if (startIndex + length > capacity()) {
            throw new IndexOutOfBoundsException("Index too big - Bytes needed: "
                    + (startIndex + length) + ", maximum is " + capacity());
        }
    }
}
