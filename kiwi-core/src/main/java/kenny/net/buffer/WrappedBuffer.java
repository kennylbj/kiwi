package kenny.net.buffer;

/**
 * Created by kennylbj on 16/10/18.
 */
public interface WrappedBuffer extends Buffer {
    /**
     * Returns this buffer's parent that this buffer is wrapping.
     */
    Buffer unwrap();
}
