package kenny.net.buffer;

import java.nio.ByteBuffer;

/**
 * Created by kennylbj on 16/10/4.
 */
public class EmptyBuffer extends BigEndianHeapBuffer {
    private static final byte[] BUFFER = {};

    EmptyBuffer() {
        super(BUFFER);
    }

    @Override
    public void clear() {
    }

    @Override
    public void readerIndex(int readerIndex) {
        if (readerIndex != 0) {
            throw new IndexOutOfBoundsException("Invalid readerIndex: "
                    + readerIndex + " - Maximum is 0");
        }
    }

    @Override
    public void writerIndex(int writerIndex) {
        if (writerIndex != 0) {
            throw new IndexOutOfBoundsException("Invalid writerIndex: "
                    + writerIndex + " - Maximum is 0");
        }
    }

    @Override
    public void setIndex(int readerIndex, int writerIndex) {
        if (writerIndex != 0 || readerIndex != 0) {
            throw new IndexOutOfBoundsException("Invalid writerIndex: "
                    + writerIndex + " - Maximum is " + readerIndex + " or "
                    + capacity());
        }
    }

    @Override
    public void markReaderIndex() {
    }

    @Override
    public void resetReaderIndex() {
    }

    @Override
    public void markWriterIndex() {
    }

    @Override
    public void resetWriterIndex() {
    }

    @Override
    public void discardReadBytes() {
    }

    @Override
    public Buffer readBytes(int length) {
        checkReadableBytes(length);
        return this;
    }

    @Override
    public Buffer readSlice(int length) {
        checkReadableBytes(length);
        return this;
    }

    @Override
    public void readBytes(byte[] dst, int dstIndex, int length) {
        checkReadableBytes(length);
    }

    @Override
    public void readBytes(byte[] dst) {
        checkReadableBytes(dst.length);
    }

    @Override
    public void readBytes(Buffer dst) {
        checkReadableBytes(dst.writableBytes());
    }

    @Override
    public void readBytes(Buffer dst, int length) {
        checkReadableBytes(length);
    }

    @Override
    public void readBytes(Buffer dst, int dstIndex, int length) {
        checkReadableBytes(length);
    }

    @Override
    public void readBytes(ByteBuffer dst) {
        checkReadableBytes(dst.remaining());
    }


    @Override
    public void skipBytes(int length) {
        checkReadableBytes(length);
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        checkWritableBytes(length);
    }

    @Override
    public void writeBytes(Buffer src, int length) {
        checkWritableBytes(length);
    }

    @Override
    public void writeBytes(Buffer src, int srcIndex, int length) {
        checkWritableBytes(length);
    }

    @Override
    public void writeBytes(ByteBuffer src) {
        checkWritableBytes(src.remaining());
    }

    @Override
    public void writeZero(int length) {
        checkWritableBytes(length);
    }

    /**
     * Throws an {@link IndexOutOfBoundsException} the length is not 0.
     */
    private void checkWritableBytes(int length) {
        if (length == 0) {
            return;
        }
        if (length > 0) {
            throw new IndexOutOfBoundsException("Writable bytes exceeded - Need "
                    + length + ", maximum is " + 0);
        } else {
            throw new IndexOutOfBoundsException("length < 0");
        }
    }

    /**
     * Throws an {@link IndexOutOfBoundsException} the length is not 0.
     */
    protected void checkReadableBytes(int length) {
        if (length == 0) {
            return;
        }
        if (length > 0) {
            throw new IndexOutOfBoundsException("Not enough readable bytes - Need "
                    + length + ", maximum is " + readableBytes());
        } else {
            throw new IndexOutOfBoundsException("length < 0");
        }
    }
}
