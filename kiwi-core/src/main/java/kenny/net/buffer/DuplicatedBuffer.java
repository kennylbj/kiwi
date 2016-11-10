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
 * TODO @Override
 */
public class DuplicatedBuffer extends AbstractBuffer implements WrappedBuffer {
    private final Buffer buffer;

    public DuplicatedBuffer(Buffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        this.buffer = buffer;
        setIndex(buffer.readerIndex(), buffer.writerIndex());
    }

    private DuplicatedBuffer(DuplicatedBuffer buffer) {
        this.buffer = buffer.buffer;
        setIndex(buffer.readerIndex(), buffer.writerIndex());
    }

    @Override
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

    public Buffer duplicate() {
        return new DuplicatedBuffer(this);
    }

    public Buffer copy(int index, int length) {
        return buffer.copy(index, length);
    }

    public Buffer slice(int index, int length) {
        return buffer.slice(index, length);
    }

    public void getBytes(int index, Buffer dst, int dstIndex, int length) {
        buffer.getBytes(index, dst, dstIndex, length);
    }

    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        buffer.getBytes(index, dst, dstIndex, length);
    }

    public void getBytes(int index, ByteBuffer dst) {
        buffer.getBytes(index, dst);
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

    public void getBytes(int index, OutputStream out, int length)
            throws IOException {
        buffer.getBytes(index, out, length);
    }

    public int getBytes(int index, GatheringByteChannel out, int length)
            throws IOException {
        return buffer.getBytes(index, out, length);
    }

    public int setBytes(int index, InputStream in, int length)
            throws IOException {
        return buffer.setBytes(index, in, length);
    }

    public int setBytes(int index, ScatteringByteChannel in, int length)
            throws IOException {
        return buffer.setBytes(index, in, length);
    }

    public ByteBuffer toByteBuffer(int index, int length) {
        return buffer.toByteBuffer(index, length);
    }
}
