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
public class TruncatedBuffer extends AbstractBuffer implements WrappedBuffer {
    private final Buffer buffer;
    private final int length;

    public TruncatedBuffer(Buffer buffer, int length) {
        if (length > buffer.capacity()) {
            throw new IndexOutOfBoundsException("Length is too large, got "
                    + length + " but can't go higher than " + buffer.capacity());
        }

        this.buffer = buffer;
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
        return buffer.arrayOffset();
    }

    public byte getByte(int index) {
        checkIndex(index);
        return buffer.getByte(index);
    }

    public short getShort(int index) {
        checkIndex(index, 2);
        return buffer.getShort(index);
    }


    public int getInt(int index) {
        checkIndex(index, 4);
        return buffer.getInt(index);
    }

    public long getLong(int index) {
        checkIndex(index, 8);
        return buffer.getLong(index);
    }

    public Buffer duplicate() {
        Buffer duplicate = new TruncatedBuffer(buffer, length);
        duplicate.setIndex(readerIndex(), writerIndex());
        return duplicate;
    }

    public Buffer copy(int index, int length) {
        checkIndex(index, length);
        return buffer.copy(index, length);
    }

    public Buffer slice(int index, int length) {
        checkIndex(index, length);
        if (length == 0) {
            return BufferHelpler.EMPTY_BUFFER;
        }
        return buffer.slice(index, length);
    }

    public void getBytes(int index, Buffer dst, int dstIndex, int length) {
        checkIndex(index, length);
        buffer.getBytes(index, dst, dstIndex, length);
    }

    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        checkIndex(index, length);
        buffer.getBytes(index, dst, dstIndex, length);
    }

    public void getBytes(int index, ByteBuffer dst) {
        checkIndex(index, dst.remaining());
        buffer.getBytes(index, dst);
    }

    public void setByte(int index, int value) {
        checkIndex(index);
        buffer.setByte(index, value);
    }

    public void setShort(int index, int value) {
        checkIndex(index, 2);
        buffer.setShort(index, value);
    }


    public void setInt(int index, int value) {
        checkIndex(index, 4);
        buffer.setInt(index, value);
    }

    public void setLong(int index, long value) {
        checkIndex(index, 8);
        buffer.setLong(index, value);
    }

    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        checkIndex(index, length);
        buffer.setBytes(index, src, srcIndex, length);
    }

    public void setBytes(int index, Buffer src, int srcIndex, int length) {
        checkIndex(index, length);
        buffer.setBytes(index, src, srcIndex, length);
    }

    public void setBytes(int index, ByteBuffer src) {
        checkIndex(index, src.remaining());
        buffer.setBytes(index, src);
    }

    public void getBytes(int index, OutputStream out, int length)
            throws IOException {
        checkIndex(index, length);
        buffer.getBytes(index, out, length);
    }

    public int getBytes(int index, GatheringByteChannel out, int length)
            throws IOException {
        checkIndex(index, length);
        return buffer.getBytes(index, out, length);
    }

    public int setBytes(int index, InputStream in, int length)
            throws IOException {
        checkIndex(index, length);
        return buffer.setBytes(index, in, length);
    }

    public int setBytes(int index, ScatteringByteChannel in, int length)
            throws IOException {
        checkIndex(index, length);
        return buffer.setBytes(index, in, length);
    }

    public ByteBuffer toByteBuffer(int index, int length) {
        checkIndex(index, length);
        return buffer.toByteBuffer(index, length);
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= capacity()) {
            throw new IndexOutOfBoundsException("Invalid index of " + index
                    + ", maximum is " + capacity());
        }
    }

    private void checkIndex(int index, int length) {
        if (length < 0) {
            throw new IllegalArgumentException(
                    "length is negative: " + length);
        }
        if (index + length > capacity()) {
            throw new IndexOutOfBoundsException("Invalid index of "
                    + (index + length) + ", maximum is " + capacity());
        }
    }
}
