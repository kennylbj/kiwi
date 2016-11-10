package kenny.net.buffer;

import java.nio.ByteOrder;

/**
 * Created by kennylbj on 16/10/4.
 */
public class BigEndianHeapBuffer extends HeapBuffer {

    /**
     * Creates a new big-endian heap buffer with a newly allocated byte array.
     *
     * @param length the length of the new byte array
     */
    public BigEndianHeapBuffer(int length) {
        super(length);
    }

    /**
     * Creates a new big-endian heap buffer with an existing byte array.
     *
     * @param array the byte array to wrap
     */
    public BigEndianHeapBuffer(byte[] array) {
        super(array);
    }

    private BigEndianHeapBuffer(byte[] array, int readerIndex, int writerIndex) {
        super(array, readerIndex, writerIndex);
    }

    public BufferFactory factory() {
        return HeapBufferFactory.getInstance(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.BIG_ENDIAN;
    }

    @Override
    public short getShort(int index) {
        return (short) (array[index] << 8 | array[index + 1] & 0xFF);
    }

    @Override
    public int getInt(int index) {
        return  (array[index]     & 0xff) << 24 |
                (array[index + 1] & 0xff) << 16 |
                (array[index + 2] & 0xff) <<  8 |
                array[index + 3] & 0xff;
    }

    @Override
    public long getLong(int index) {
        return  ((long) array[index]     & 0xff) << 56 |
                ((long) array[index + 1] & 0xff) << 48 |
                ((long) array[index + 2] & 0xff) << 40 |
                ((long) array[index + 3] & 0xff) << 32 |
                ((long) array[index + 4] & 0xff) << 24 |
                ((long) array[index + 5] & 0xff) << 16 |
                ((long) array[index + 6] & 0xff) <<  8 |
                (long) array[index + 7] & 0xff;
    }

    @Override
    public void setShort(int index, int value) {
        array[index]     = (byte) (value >>> 8);
        array[index + 1] = (byte) value;
    }

    @Override
    public void setInt(int index, int   value) {
        array[index]     = (byte) (value >>> 24);
        array[index + 1] = (byte) (value >>> 16);
        array[index + 2] = (byte) (value >>> 8);
        array[index + 3] = (byte) value;
    }

    @Override
    public void setLong(int index, long  value) {
        array[index]     = (byte) (value >>> 56);
        array[index + 1] = (byte) (value >>> 48);
        array[index + 2] = (byte) (value >>> 40);
        array[index + 3] = (byte) (value >>> 32);
        array[index + 4] = (byte) (value >>> 24);
        array[index + 5] = (byte) (value >>> 16);
        array[index + 6] = (byte) (value >>> 8);
        array[index + 7] = (byte) value;
    }

    @Override
    public Buffer duplicate() {
        return new BigEndianHeapBuffer(array, readerIndex(), writerIndex());
    }

    @Override
    public Buffer copy(int index, int length) {
        if (index < 0 || length < 0 || index + length > array.length) {
            throw new IndexOutOfBoundsException("Too many bytes to copy - Need "
                    + (index + length) + ", maximum is " + array.length);
        }

        byte[] copiedArray = new byte[length];
        System.arraycopy(array, index, copiedArray, 0, length);
        return new BigEndianHeapBuffer(copiedArray);
    }
}
