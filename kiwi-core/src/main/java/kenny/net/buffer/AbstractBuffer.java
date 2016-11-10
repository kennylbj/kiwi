package kenny.net.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

/**
 * Created by kennylbj on 16/9/25.
 */
public abstract class AbstractBuffer implements Buffer {
    private int readerIndex;
    private int writerIndex;
    private int markedReaderIndex;
    private int markedWriterIndex;

    @Override
    public BufferFactory factory() {
        return null;
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public ByteOrder order() {
        return null;
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public int readerIndex() {
        return readerIndex;
    }

    @Override
    public void readerIndex(int readerIndex) {
        if (readerIndex < 0 || readerIndex > writerIndex) {
            throw new IndexOutOfBoundsException();
        }
        this.readerIndex = readerIndex;
    }

    @Override
    public int writerIndex() {
        return writerIndex;
    }

    @Override
    public void writerIndex(int writerIndex) {
        if (writerIndex < readerIndex || writerIndex > capacity()) {
            throw new IndexOutOfBoundsException("Invalid readerIndex: "
                    + readerIndex + " - Maximum is " + writerIndex);
        }
        this.writerIndex = writerIndex;
    }

    @Override
    public void setIndex(int readerIndex, int writerIndex) {
        if (readerIndex < 0 || readerIndex > writerIndex || writerIndex > capacity()) {
            throw new IndexOutOfBoundsException("Invalid writerIndex: "
                    + writerIndex + " - Maximum is " + readerIndex + " or " + capacity());
        }
        this.readerIndex = readerIndex;
        this.writerIndex = writerIndex;
    }

    @Override
    public int readableBytes() {
        return writerIndex - readerIndex;
    }

    @Override
    public int writableBytes() {
        return capacity() - writerIndex;
    }

    @Override
    public boolean readable() {
        return readableBytes() > 0;
    }

    @Override
    public boolean writable() {
        return writableBytes() > 0;
    }

    @Override
    public void clear() {
        readerIndex = writerIndex = 0;
    }

    @Override
    public void markReaderIndex() {
        markedReaderIndex = readerIndex;
    }

    @Override
    public void resetReaderIndex() {
        readerIndex(markedReaderIndex);
    }

    @Override
    public void markWriterIndex() {
        markedWriterIndex = writerIndex;
    }

    @Override
    public void resetWriterIndex() {
        writerIndex(markedWriterIndex);
    }

    @Override
    public void discardReadBytes() {
        if (readerIndex == 0) {
            return;
        }
        setBytes(0, this, readerIndex, writerIndex - readerIndex);
        writerIndex -= readerIndex;
        markedReaderIndex = Math.max(markedReaderIndex - readerIndex, 0);
        markedWriterIndex = Math.max(markedWriterIndex - readerIndex, 0);
        readerIndex = 0;
    }

    @Override
    public void ensureWritableBytes(int writableBytes) {
        if (writableBytes > writableBytes()) {
            throw new IndexOutOfBoundsException("Writable bytes exceeded: Got "
                    + writableBytes + ", maximum is " + writableBytes());
        }
    }

    @Override
    public byte getByte(int index) {
        return 0;
    }

    @Override
    public short getUnsignedByte(int index) {
        return (short) (getByte(index) & 0xFF);
    }

    @Override
    public short getShort(int index) {
        return 0;
    }

    @Override
    public int getUnsignedShort(int index) {
        return getShort(index) & 0xFFFF;
    }

    @Override
    public int getInt(int index) {
        return 0;
    }

    @Override
    public long getUnsignedInt(int index) {
        return getInt(index) & 0xFFFFFFFFL;
    }

    @Override
    public long getLong(int index) {
        return 0;
    }

    @Override
    public char getChar(int index) {
        return (char) getShort(index);
    }

    @Override
    public float getFloat(int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    @Override
    public double getDouble(int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    @Override
    public void getBytes(int index, Buffer dst) {
        getBytes(index, dst, dst.writableBytes());

    }

    @Override
    public void getBytes(int index, Buffer dst, int length) {
        if (length > dst.writableBytes()) {
            throw new IndexOutOfBoundsException("Too many bytes to be read: Need "
                    + length + ", maximum is " + dst.writableBytes());
        }
        getBytes(index, dst, dst.writerIndex(), length);
        dst.writerIndex(dst.writerIndex() + length);
    }


    @Override
    public void getBytes(int index, byte[] dst) {
        getBytes(index, dst, 0, dst.length);
    }


    @Override
    public void setByte(int index, int value) {

    }

    @Override
    public void setShort(int index, int value) {

    }

    @Override
    public void setInt(int index, int value) {

    }

    @Override
    public void setLong(int index, long value) {

    }

    @Override
    public void setChar(int index, int value) {
        setShort(index, value);
    }

    @Override
    public void setFloat(int index, float value) {
        setInt(index, Float.floatToRawIntBits(value));
    }

    @Override
    public void setDouble(int index, double value) {
        setLong(index, Double.doubleToRawLongBits(value));
    }

    @Override
    public void setBytes(int index, Buffer src) {
        setBytes(index, src, src.readableBytes());
    }

    @Override
    public void setBytes(int index, Buffer src, int length) {
        if (length > src.readableBytes()) {
            throw new IndexOutOfBoundsException("Too many bytes to write: Need "
                    + length + ", maximum is " + src.readableBytes());
        }
        setBytes(index, src, src.readerIndex(), length);
        src.readerIndex(src.readerIndex() + length);
    }

    @Override
    public void setBytes(int index, Buffer src, int srcIndex, int length) {

    }

    @Override
    public void setBytes(int index, byte[] src) {
        setBytes(index, src, 0, src.length);
    }

    @Override
    public void setBytes(int index, byte[] src, int srcIndex, int length) {

    }

    @Override
    public void setBytes(int index, ByteBuffer src) {

    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        return 0;
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return 0;
    }

    @Override
    public void setZero(int index, int length) {
        if (length == 0) {
            return;
        }
        if (length < 0) {
            throw new IllegalArgumentException(
                    "length must be 0 or greater than 0.");
        }

        int nLong = length >>> 3;
        int nBytes = length & 7;
        for (int i = nLong; i > 0; i --) {
            setLong(index, 0);
            index += 8;
        }
        if (nBytes == 4) {
            setInt(index, 0);
        } else if (nBytes < 4) {
            for (int i = nBytes; i > 0; i --) {
                setByte(index, (byte) 0);
                index ++;
            }
        } else {
            setInt(index, 0);
            index += 4;
            for (int i = nBytes - 4; i > 0; i --) {
                setByte(index, (byte) 0);
                index ++;
            }
        }
    }

    @Override
    public byte readByte() {
        if (readerIndex == writerIndex) {
            throw new IndexOutOfBoundsException("Readable byte limit exceeded: "
                    + readerIndex);
        }
        return getByte(readerIndex ++);
    }

    @Override
    public short readUnsignedByte() {
        return (short) (readByte() & 0xFF);
    }

    @Override
    public short readShort() {
        checkReadableBytes(2);
        short value = getShort(readerIndex);
        readerIndex += 2;
        return value;
    }

    @Override
    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    @Override
    public int readInt() {
        checkReadableBytes(4);
        int v = getInt(readerIndex);
        readerIndex += 4;
        return v;
    }

    @Override
    public long readUnsignedInt() {
        return readInt() & 0xFFFFFFFFL;
    }

    @Override
    public long readLong() {
        checkReadableBytes(8);
        long v = getLong(readerIndex);
        readerIndex += 8;
        return v;
    }

    @Override
    public char readChar() {
        return (char) readShort();
    }

    @Override
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public Buffer readBytes(int length) {
        checkReadableBytes(length);
        if (length == 0) {
            return BufferHelpler.EMPTY_BUFFER;
        }
        Buffer buf = factory().getBuffer(order(), length);
        buf.writeBytes(this, readerIndex, length);
        readerIndex += length;
        return buf;
    }

    @Override
    public Buffer readSlice(int length) {
        Buffer slice = slice(readerIndex, length);
        readerIndex += length;
        return slice;
    }

    @Override
    public void readBytes(Buffer dst) {
        readBytes(dst, dst.writableBytes());
    }

    @Override
    public void readBytes(Buffer dst, int length) {
        if (length > dst.writableBytes()) {
            throw new IndexOutOfBoundsException("Too many bytes to be read: Need "
                    + length + ", maximum is " + dst.writableBytes());
        }
        readBytes(dst, dst.writerIndex(), length);
        dst.writerIndex(dst.writerIndex() + length);
    }

    @Override
    public void readBytes(Buffer dst, int dstIndex, int length) {
        checkReadableBytes(length);
        getBytes(readerIndex, dst, dstIndex, length);
        readerIndex += length;
    }

    @Override
    public void readBytes(byte[] dst) {
        readBytes(dst, 0, dst.length);
    }

    @Override
    public void readBytes(byte[] dst, int dstIndex, int length) {
        checkReadableBytes(length);
        getBytes(readerIndex, dst, dstIndex, length);
        readerIndex += length;
    }

    @Override
    public void readBytes(ByteBuffer dst) {
        int length = dst.remaining();
        checkReadableBytes(length);
        getBytes(readerIndex, dst);
        readerIndex += length;
    }

    @Override
    public void readBytes(OutputStream out, int length) throws IOException {
        checkReadableBytes(length);
        getBytes(readerIndex, out, length);
        readerIndex += length;
    }

    @Override
    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        checkReadableBytes(length);
        int readBytes = getBytes(readerIndex, out, length);
        readerIndex += readBytes;
        return readBytes;
    }

    @Override
    public void skipBytes(int length) {
        int newReaderIndex = readerIndex + length;
        if (newReaderIndex > writerIndex) {
            throw new IndexOutOfBoundsException("Readable bytes exceeded - Need "
                    + newReaderIndex + ", maximum is " + writerIndex);
        }
        readerIndex = newReaderIndex;
    }

    @Override
    public void writeByte(int value) {
        setByte(writerIndex, value);
        writerIndex++;
    }

    @Override
    public void writeShort(int value) {
        setShort(writerIndex, value);
        writerIndex += 2;
    }

    @Override
    public void writeInt(int value) {
        setInt(writerIndex, value);
        writerIndex += 4;
    }

    @Override
    public void writeLong(long value) {
        setLong(writerIndex, value);
        writerIndex += 8;
    }

    @Override
    public void writeChar(int value) {
        writeShort(value);
    }

    @Override
    public void writeFloat(float value) {
        writeInt(Float.floatToRawIntBits(value));
    }

    @Override
    public void writeDouble(double value) {
        writeLong(Double.doubleToRawLongBits(value));
    }

    @Override
    public void writeBytes(Buffer src) {
        writeBytes(src, src.readableBytes());
    }

    @Override
    public void writeBytes(Buffer src, int length) {
        if (length > src.readableBytes()) {
            throw new IndexOutOfBoundsException("Too many bytes to write - Need "
                    + length + ", maximum is " + src.readableBytes());
        }
        writeBytes(src, src.readerIndex(), length);
        src.readerIndex(src.readerIndex() + length);
    }

    @Override
    public void writeBytes(Buffer src, int srcIndex, int length) {
        setBytes(writerIndex, src, srcIndex, length);
        writerIndex += length;
    }

    @Override
    public void writeBytes(byte[] src) {
        writeBytes(src, 0, src.length);
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        setBytes(writerIndex, src, srcIndex, length);
        writerIndex += length;
    }

    @Override
    public void writeBytes(ByteBuffer src) {
        int length = src.remaining();
        setBytes(writerIndex, src);
        writerIndex += length;
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        int writtenBytes = setBytes(writerIndex, in, length);
        if (writtenBytes > 0) {
            writerIndex += writtenBytes;
        }
        return writtenBytes;
    }

    @Override
    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        int writtenBytes = setBytes(writerIndex, in, length);
        if (writtenBytes > 0) {
            writerIndex += writtenBytes;
        }
        return writtenBytes;
    }

    @Override
    public void writeZero(int length) {
        if (length == 0) {
            return;
        }
        if (length < 0) {
            throw new IllegalArgumentException(
                    "length must be 0 or greater than 0.");
        }
        int nLong = length >>> 3;
        int nBytes = length & 7;
        for (int i = nLong; i > 0; i --) {
            writeLong(0);
        }
        if (nBytes == 4) {
            writeInt(0);
        } else if (nBytes < 4) {
            for (int i = nBytes; i > 0; i --) {
                writeByte((byte) 0);
            }
        } else {
            writeInt(0);
            for (int i = nBytes - 4; i > 0; i --) {
                writeByte((byte) 0);
            }
        }
    }

    @Override
    public int indexOf(int fromIndex, int toIndex, byte value) {
        return 0;
    }

    @Override
    public int indexOf(int fromIndex, int toIndex, BufferIndexFinder indexFinder) {
        return 0;
    }

    @Override
    public int bytesBefore(byte value) {
        return 0;
    }

    @Override
    public int bytesBefore(BufferIndexFinder indexFinder) {
        return 0;
    }

    @Override
    public int bytesBefore(int length, byte value) {
        return 0;
    }

    @Override
    public int bytesBefore(int length, BufferIndexFinder indexFinder) {
        return 0;
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        return 0;
    }

    @Override
    public int bytesBefore(int index, int length, BufferIndexFinder indexFinder) {
        return 0;
    }

    @Override
    public Buffer copy() {
        return copy(readerIndex, readableBytes());
    }

    @Override
    public Buffer copy(int index, int length) {
        return null;
    }

    @Override
    public Buffer slice() {
        return slice(readerIndex, readableBytes());
    }

    @Override
    public Buffer slice(int index, int length) {
        return null;
    }

    @Override
    public Buffer duplicate() {
        return null;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return toByteBuffer(readerIndex, readableBytes());
    }

    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        return null;
    }

    @Override
    public ByteBuffer[] toByteBuffers() {
        return toByteBuffers(readerIndex, readableBytes());
    }

    @Override
    public ByteBuffer[] toByteBuffers(int index, int length) {
        return new ByteBuffer[] { toByteBuffer(index, length) };
    }

    @Override
    public boolean hasArray() {
        return false;
    }

    @Override
    public byte[] array() {
        return new byte[0];
    }

    @Override
    public int arrayOffset() {
        return 0;
    }

    @Override
    public String toString(Charset charset) {
        return toString(readerIndex, readableBytes(), charset);
    }

    @Override
    public String toString(int index, int length, Charset charset) {
        return "";
    }

    @Override
    public int compareTo(Buffer buffer) {
        return 0;
    }

    /**
     * Throws an {@link IndexOutOfBoundsException} if the current
     * {@linkplain #readableBytes() readable bytes} of this buffer is less
     * than the specified value.
     */
    protected void checkReadableBytes(int minimumReadableBytes) {
        if (readableBytes() < minimumReadableBytes) {
            throw new IndexOutOfBoundsException("Not enough readable bytes - Need "
                    + minimumReadableBytes + ", maximum is " + readableBytes());
        }
    }
}
