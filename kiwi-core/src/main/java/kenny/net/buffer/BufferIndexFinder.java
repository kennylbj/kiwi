package kenny.net.buffer;

/**
 * Locates an index of data in a {@link Buffer}.
 * <p>
 * This interface enables the sequential search for the data which meets more
 * complex and dynamic condition than just a simple value matching.  Please
 * refer to {@link Buffer#indexOf(int, int, BufferIndexFinder)} and
 * {@link Buffer#bytesBefore(int, int, BufferIndexFinder)}
 * for more explanation.
 *
 */
public interface BufferIndexFinder {

    /**
     * Returns {@code true} if and only if the data is found at the specified
     * {@code guessedIndex} of the specified {@code buffer}.
     * <p>
     * The implementation should not perform an operation which raises an
     * exception such as {@link IndexOutOfBoundsException} nor perform
     * an operation which modifies the content of the buffer.
     */
    boolean find(Buffer buffer, int guessedIndex);

    /**
     * Index finder which locates a {@code NUL (0x00)} byte.
     */
    BufferIndexFinder NUL = (buffer, guessedIndex) -> buffer.getByte(guessedIndex) == 0;

    /**
     * Index finder which locates a non-{@code NUL (0x00)} byte.
     */
    BufferIndexFinder NOT_NUL = (buffer, guessedIndex) -> buffer.getByte(guessedIndex) != 0;

    /**
     * Index finder which locates a {@code CR ('\r')} byte.
     */
    BufferIndexFinder CR = (buffer, guessedIndex) -> buffer.getByte(guessedIndex) == '\r';

    /**
     * Index finder which locates a non-{@code CR ('\r')} byte.
     */
    BufferIndexFinder NOT_CR = (buffer, guessedIndex) -> buffer.getByte(guessedIndex) != '\r';

    /**
     * Index finder which locates a {@code LF ('\n')} byte.
     */
    BufferIndexFinder LF = (buffer, guessedIndex) -> buffer.getByte(guessedIndex) == '\n';

    /**
     * Index finder which locates a non-{@code LF ('\n')} byte.
     */
    BufferIndexFinder NOT_LF = (buffer, guessedIndex) -> buffer.getByte(guessedIndex) != '\n';

    /**
     * Index finder which locates a {@code CR ('\r')} or {@code LF ('\n')}.
     */
    BufferIndexFinder CRLF = (buffer, guessedIndex) -> {
        byte b = buffer.getByte(guessedIndex);
        return b == '\r' || b == '\n';
    };

    /**
     * Index finder which locates a byte which is neither a {@code CR ('\r')}
     * nor a {@code LF ('\n')}.
     */
    BufferIndexFinder NOT_CRLF = (buffer, guessedIndex) -> {
        byte b = buffer.getByte(guessedIndex);
        return b != '\r' && b != '\n';
    };

    /**
     * Index finder which locates a linear whitespace
     * ({@code ' '} and {@code '\t'}).
     */
    BufferIndexFinder LINEAR_WHITESPACE = (buffer, guessedIndex) -> {
        byte b = buffer.getByte(guessedIndex);
        return b == ' ' || b == '\t';
    };

    /**
     * Index finder which locates a byte which is not a linear whitespace
     * (neither {@code ' '} nor {@code '\t'}).
     */
    BufferIndexFinder NOT_LINEAR_WHITESPACE = (buffer, guessedIndex) -> {
        byte b = buffer.getByte(guessedIndex);
        return b != ' ' && b != '\t';
    };
}
