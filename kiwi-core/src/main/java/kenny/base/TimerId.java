package kenny.base;


import java.util.Arrays;
import java.util.Random;

/**
 * Created by kennylbj on 16/9/14.
 * Identifier returned by Timer which can be used to cancel it.
 */
public final class TimerId {
    private static final int TIMER_ID_SIZE = 32;
    private static final Random randomGenerator = new Random(System.nanoTime());
    private final byte[] bytes;

    private TimerId(byte[] dataBytes) {
        assert dataBytes.length == TIMER_ID_SIZE;
        bytes = new byte[TIMER_ID_SIZE];
        System.arraycopy(dataBytes, 0, bytes, 0, dataBytes.length);
    }

    public static TimerId generate() {
        byte[] dataBytes = new byte[TIMER_ID_SIZE];
        randomGenerator.nextBytes(dataBytes);
        return new TimerId(dataBytes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(String.format("%02X", aByte));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TimerId)) {
            return false;
        }
        final TimerId other = (TimerId) obj;
        return Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    public static void main(String[] args) {
        TimerId id = TimerId.generate();
        System.out.println("toString " + id);
    }

}
