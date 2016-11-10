package kenny.net.buffer;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * Created by kennylbj on 16/10/4.
 */
public abstract class HeapBuffer extends AbstractBuffer {

    /**
     * The underlying heap byte array that this buffer is wrapping.
     */
    protected final byte[] array;

    /**
     * Creates a new heap buffer with a newly allocated byte array.
     *
     * @param length the length of the new byte array
     */
    protected HeapBuffer(int length) {
        this(new byte[length], 0, 0);
    }

    /**
     * Creates a new heap buffer with an existing byte array.
     *
     * @param array the byte array to wrap
     */
    protected HeapBuffer(byte[] array) {
        this(array, 0, array.length);
    }

    /**
     * Creates a new heap buffer with an existing byte array.
     *
     * @param array        the byte array to wrap
     * @param readerIndex  the initial reader index of this buffer
     * @param writerIndex  the initial writer index of this buffer
     */
    protected HeapBuffer(byte[] array, int readerIndex, int writerIndex) {
        if (array == null) {
            throw new NullPointerException("array");
        }
        this.array = array;
        setIndex(readerIndex, writerIndex);
    }

    public boolean isDirect() {
        return false;
    }

    public int capacity() {
        return array.length;
    }

    public boolean hasArray() {
        return true;
    }

    public byte[] array() {
        return array;
    }

    public int arrayOffset() {
        return 0;
    }

    public byte getByte(int index) {
        return array[index];
    }

    public void getBytes(int index, Buffer dst, int dstIndex, int length) {
        if (dst instanceof HeapBuffer) {
            getBytes(index, ((HeapBuffer) dst).array, dstIndex, length);
        } else {
            dst.setBytes(dstIndex, array, index, length);
        }
    }

    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        System.arraycopy(array, index, dst, dstIndex, length);
    }

    public void getBytes(int index, ByteBuffer dst) {
        dst.put(array, index, Math.min(capacity() - index, dst.remaining()));
    }

    public void getBytes(int index, OutputStream out, int length)
            throws IOException {
        out.write(array, index, length);
    }

    public int getBytes(int index, GatheringByteChannel out, int length)
            throws IOException {
        return out.write(ByteBuffer.wrap(array, index, length));
    }

    public void setByte(int index, int value) {
        array[index] = (byte) value;
    }

    public void setBytes(int index, Buffer src, int srcIndex, int length) {
        if (src instanceof HeapBuffer) {
            setBytes(index, ((HeapBuffer) src).array, srcIndex, length);
        } else {
            src.getBytes(srcIndex, array, index, length);
        }
    }

    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        System.arraycopy(src, srcIndex, array, index, length);
    }

    public void setBytes(int index, ByteBuffer src) {
        src.get(array, index, src.remaining());
    }

    public int setBytes(int index, InputStream in, int length) throws IOException {
        int readBytes = 0;
        do {
            int localReadBytes = in.read(array, index, length);
            if (localReadBytes < 0) {
                if (readBytes == 0) {
                    return -1;
                } else {
                    break;
                }
            }
            readBytes += localReadBytes;
            index += localReadBytes;
            length -= localReadBytes;
        } while (length > 0);

        return readBytes;
    }

    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(array, index, length);
        int readBytes = 0;

        do {
            int localReadBytes;
            try {
                localReadBytes = in.read(buf);
            } catch (ClosedChannelException e) {
                localReadBytes = -1;
            }
            if (localReadBytes < 0) {
                if (readBytes == 0) {
                    return -1;
                } else {
                    break;
                }
            }
            if (localReadBytes == 0) {
                break;
            }
            readBytes += localReadBytes;
        } while (readBytes < length);

        return readBytes;
    }

    public Buffer slice(int index, int length) {
        if (index == 0) {
            if (length == 0) {
                return BufferHelpler.EMPTY_BUFFER;
            }
            if (length == array.length) {
                Buffer slice = duplicate();
                slice.setIndex(0, length);
                return slice;
            } else {
                return new TruncatedBuffer(this, length);
            }
        } else {
            if (length == 0) {
                return BufferHelpler.EMPTY_BUFFER;
            }
            return new SlicedBuffer(this, index, length);
        }
    }

    public ByteBuffer toByteBuffer(int index, int length) {
        return ByteBuffer.wrap(array, index, length).order(order());
    }

}
