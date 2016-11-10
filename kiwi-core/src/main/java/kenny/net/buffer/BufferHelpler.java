package kenny.net.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kennylbj on 16/10/4.
 */
public class BufferHelpler {

    /**
     * Big endian byte order.
     */
    public static final ByteOrder BIG_ENDIAN = ByteOrder.BIG_ENDIAN;

    /**
     * Little endian byte order.
     */
    public static final ByteOrder LITTLE_ENDIAN = ByteOrder.LITTLE_ENDIAN;

    /**
     * A buffer whose capacity is {@code 0}.
     */
    public static final Buffer EMPTY_BUFFER = new EmptyBuffer();

    public static Buffer buffer(int capacity) {
        return buffer(BIG_ENDIAN, capacity);
    }

    /**
     * Creates a new Java heap buffer with the specified {@code endianness}
     * and {@code capacity}.  The new buffer's {@code readerIndex} and
     * {@code writerIndex} are {@code 0}.
     */
    public static Buffer buffer(ByteOrder endianness, int capacity) {
        if (endianness == BIG_ENDIAN) {
            if (capacity == 0) {
                return EMPTY_BUFFER;
            }
            return new BigEndianHeapBuffer(capacity);
        } else if (endianness == LITTLE_ENDIAN) {
            if (capacity == 0) {
                return EMPTY_BUFFER;
            }
            return new LittleEndianHeapBuffer(capacity);
        } else {
            throw new NullPointerException("endianness");
        }
    }


    /**
     * Creates a new big-endian dynamic buffer whose estimated data length is
     * {@code 256} bytes.  The new buffer's {@code readerIndex} and
     * {@code writerIndex} are {@code 0}.
     */
    public static Buffer dynamicBuffer() {
        return dynamicBuffer(BIG_ENDIAN, 256);
    }

    public static Buffer dynamicBuffer(BufferFactory factory) {
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        return new DynamicBuffer(factory.getDefaultOrder(), 256, factory);
    }

    /**
     * Creates a new big-endian dynamic buffer with the specified estimated
     * data length.  More accurate estimation yields less unexpected
     * reallocation overhead.  The new buffer's {@code readerIndex} and
     * {@code writerIndex} are {@code 0}.
     */
    public static Buffer dynamicBuffer(int estimatedLength) {
        return dynamicBuffer(BIG_ENDIAN, estimatedLength);
    }

    /**
     * Creates a new dynamic buffer with the specified endianness and
     * the specified estimated data length.  More accurate estimation yields
     * less unexpected reallocation overhead.  The new buffer's
     * {@code readerIndex} and {@code writerIndex} are {@code 0}.
     */
    public static Buffer dynamicBuffer(ByteOrder endianness, int estimatedLength) {
        return new DynamicBuffer(endianness, estimatedLength);
    }

    /**
     * Creates a new big-endian dynamic buffer with the specified estimated
     * data length using the specified factory.  More accurate estimation yields
     * less unexpected reallocation overhead.  The new buffer's {@code readerIndex}
     * and {@code writerIndex} are {@code 0}.
     */
    public static Buffer dynamicBuffer(int estimatedLength, BufferFactory factory) {
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        return new DynamicBuffer(factory.getDefaultOrder(), estimatedLength, factory);
    }


    /**
     * Creates a new dynamic buffer with the specified endianness and
     * the specified estimated data length using the specified factory.
     * More accurate estimation yields less unexpected reallocation overhead.
     * The new buffer's {@code readerIndex} and {@code writerIndex} are {@code 0}.
     */
    public static Buffer dynamicBuffer(ByteOrder endianness, int estimatedLength, BufferFactory factory) {
        return new DynamicBuffer(endianness, estimatedLength, factory);
    }

    /**
     * Creates a new big-endian buffer which wraps the specified {@code array}.
     * A modification on the specified array's content will be visible to the
     * returned buffer.
     */
    public static Buffer wrappedBuffer(byte[] array) {
        return wrappedBuffer(BIG_ENDIAN, array);
    }

    /**
     * Creates a new buffer which wraps the specified {@code array} with the
     * specified {@code endianness}.  A modification on the specified array's
     * content will be visible to the returned buffer.
     */
    public static Buffer wrappedBuffer(ByteOrder endianness, byte[] array) {
        if (endianness == BIG_ENDIAN) {
            if (array.length == 0) {
                return EMPTY_BUFFER;
            }
            return new BigEndianHeapBuffer(array);
        } else if (endianness == LITTLE_ENDIAN) {
            if (array.length == 0) {
                return EMPTY_BUFFER;
            }
            return new LittleEndianHeapBuffer(array);
        } else {
            throw new NullPointerException("endianness");
        }
    }

    /**
     * Creates a new big-endian buffer which wraps the sub-region of the
     * specified {@code array}.  A modification on the specified array's
     * content will be visible to the returned buffer.
     */
    public static Buffer wrappedBuffer(byte[] array, int offset, int length) {
        return wrappedBuffer(BIG_ENDIAN, array, offset, length);
    }

    /**
     * Creates a new buffer which wraps the sub-region of the specified
     * {@code array} with the specified {@code endianness}.  A modification on
     * the specified array's content will be visible to the returned buffer.
     */
    public static Buffer wrappedBuffer(ByteOrder endianness, byte[] array, int offset, int length) {
        if (endianness == null) {
            throw new NullPointerException("endianness");
        }
        if (offset == 0) {
            if (length == array.length) {
                return wrappedBuffer(endianness, array);
            } else {
                if (length == 0) {
                    return EMPTY_BUFFER;
                } else {
                    return new TruncatedBuffer(wrappedBuffer(endianness, array), length);
                }
            }
        } else {
            if (length == 0) {
                return EMPTY_BUFFER;
            } else {
                return new SlicedBuffer(wrappedBuffer(endianness, array), offset, length);
            }
        }
    }


    /**
     * Creates a new buffer which wraps the specified NIO buffer's current
     * slice.  A modification on the specified buffer's content will be
     * visible to the returned buffer.
     */
    public static Buffer wrappedBuffer(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return EMPTY_BUFFER;
        }
        if (buffer.hasArray()) {
            return wrappedBuffer(
                    buffer.order(), buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
        } else {
            //TODO return new ByteBufferBackedBuffer(buffer);
            return null;
        }
    }

    /**
     * Creates a new buffer which wraps the specified buffer's readable bytes.
     * A modification on the specified buffer's content will be visible to the
     * returned buffer.
     */
    public static Buffer wrappedBuffer(Buffer buffer) {
        if (buffer.readable()) {
            return buffer.slice();
        } else {
            return EMPTY_BUFFER;
        }
    }

    /**
     * Creates a new big-endian composite buffer which wraps the specified
     * arrays without copying them.  A modification on the specified arrays'
     * content will be visible to the returned buffer.
     */
    public static Buffer wrappedBuffer(byte[]... arrays) {
        return wrappedBuffer(BIG_ENDIAN, arrays);
    }

    /**
     * Creates a new composite buffer which wraps the specified arrays without
     * copying them.  A modification on the specified arrays' content will be
     * visible to the returned buffer.
     *
     * @param endianness the endianness of the new buffer
     */
    public static Buffer wrappedBuffer(ByteOrder endianness, byte[]... arrays) {
        switch (arrays.length) {
            case 0:
                break;
            case 1:
                if (arrays[0].length != 0) {
                    return wrappedBuffer(endianness, arrays[0]);
                }
                break;
            default:
                // Get the list of the component, while guessing the byte order.
                /*
                final List<Buffer> components = new ArrayList<>(arrays.length);
                for (byte[] a : arrays) {
                    if (a == null) {
                        break;
                    }
                    if (a.length > 0) {
                        components.add(wrappedBuffer(endianness, a));
                    }
                }*/

                final List<Buffer> components = Arrays.stream(arrays)
                        .filter(a -> a != null && a.length > 0)
                        .collect(ArrayList::new, (comp, array) -> comp.add(wrappedBuffer(endianness, array)),
                                ArrayList::addAll);

                return compositeBuffer(endianness, components, false);
        }

        return EMPTY_BUFFER;
    }

    private static Buffer compositeBuffer(ByteOrder endianness, List<Buffer> components, boolean gathering) {
        switch (components.size()) {
            case 0:
                return EMPTY_BUFFER;
            case 1:
                return components.get(0);
            default:
                return new CompositeBuffer(endianness, components, gathering);
        }
    }

    /**
     * Creates a new composite buffer which wraps the readable bytes of the
     * specified buffers without copying them.  A modification on the content
     * of the specified buffers will be visible to the returned buffer.
     *
     * @throws IllegalArgumentException
     *         if the specified buffers' endianness are different from each
     *         other
     */
    public static Buffer wrappedBuffer(Buffer... buffers) {
        return wrappedBuffer(false, buffers);
    }

    /**
     * Creates a new composite buffer which wraps the readable bytes of the
     * specified buffers without copying them.  A modification on the content
     * of the specified buffers will be visible to the returned buffer.
     * If gathering is {@code true} then gathering writes will be used when ever
     * possible.
     *
     * @throws IllegalArgumentException
     *         if the specified buffers' endianness are different from each
     *         other
     */
    public static Buffer wrappedBuffer(boolean gathering, Buffer... buffers) {
        switch (buffers.length) {
            case 0:
                break;
            case 1:
                if (buffers[0].readable()) {
                    return wrappedBuffer(buffers[0]);
                }
                break;
            default:
                ByteOrder order = null;
                final List<Buffer> components = new ArrayList<>(buffers.length);
                for (Buffer c: buffers) {
                    if (c == null) {
                        break;
                    }
                    if (c.readable()) {
                        if (order != null) {
                            if (!order.equals(c.order())) {
                                throw new IllegalArgumentException(
                                        "inconsistent byte order");
                            }
                        } else {
                            order = c.order();
                        }
                        if (c instanceof CompositeBuffer) {
                            // Expand nested composition.
                            components.addAll(
                                    ((CompositeBuffer) c).decompose(
                                            c.readerIndex(), c.readableBytes()));
                        } else {
                            // An ordinary buffer (non-composite)
                            components.add(c.slice());
                        }
                    }
                }
                return compositeBuffer(order, components, gathering);
        }
        return EMPTY_BUFFER;
    }

    /**
     * Creates a new composite buffer which wraps the slices of the specified
     * NIO buffers without copying them.  A modification on the content of the
     * specified buffers will be visible to the returned buffer.
     *
     * @throws IllegalArgumentException
     *         if the specified buffers' endianness are different from each
     *         other
     */
    public static Buffer wrappedBuffer(ByteBuffer... buffers) {
        return wrappedBuffer(false, buffers);
    }

    /**
     * Creates a new composite buffer which wraps the slices of the specified
     * NIO buffers without copying them.  A modification on the content of the
     * specified buffers will be visible to the returned buffer.
     * If gathering is {@code true} then gathering writes will be used when ever
     * possible.
     *
     * @throws IllegalArgumentException
     *         if the specified buffers' endianness are different from each
     *         other
     */
    public static Buffer wrappedBuffer(boolean gathering, ByteBuffer... buffers) {
        switch (buffers.length) {
            case 0:
                break;
            case 1:
                if (buffers[0].hasRemaining()) {
                    return wrappedBuffer(buffers[0]);
                }
                break;
            default:
                ByteOrder order = null;
                final List<Buffer> components = new ArrayList<>(buffers.length);
                for (ByteBuffer b: buffers) {
                    if (b == null) {
                        break;
                    }
                    if (b.hasRemaining()) {
                        if (order != null) {
                            if (!order.equals(b.order())) {
                                throw new IllegalArgumentException(
                                        "inconsistent byte order");
                            }
                        } else {
                            order = b.order();
                        }
                        components.add(wrappedBuffer(b));
                    }
                }
                return compositeBuffer(order, components, gathering);
        }

        return EMPTY_BUFFER;
    }

    /**
     * Creates a new big-endian buffer whose content is a copy of the
     * specified {@code array}.  The new buffer's {@code readerIndex} and
     * {@code writerIndex} are {@code 0} and {@code array.length} respectively.
     */
    public static Buffer copiedBuffer(byte[] array) {
        return copiedBuffer(BIG_ENDIAN, array);
    }

    /**
     * Creates a new buffer with the specified {@code endianness} whose
     * content is a copy of the specified {@code array}.  The new buffer's
     * {@code readerIndex} and {@code writerIndex} are {@code 0} and
     * {@code array.length} respectively.
     */
    public static Buffer copiedBuffer(ByteOrder endianness, byte[] array) {
        if (endianness == BIG_ENDIAN) {
            if (array.length == 0) {
                return EMPTY_BUFFER;
            }
            return new BigEndianHeapBuffer(array.clone());
        } else if (endianness == LITTLE_ENDIAN) {
            if (array.length == 0) {
                return EMPTY_BUFFER;
            }
            return new LittleEndianHeapBuffer(array.clone());
        } else {
            throw new NullPointerException("endianness");
        }
    }

}
