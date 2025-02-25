public class Utilities {
    /**
     * Converts a byte array (8 bytes) into a 64-bit long value.
     * <p>
     * This method assumes the byte array contains the bytes in big-endian order, meaning
     * the most significant byte is at index 0 of the array.
     * </p>
     *
     * @param bytes The byte array to convert. It must be exactly 8 bytes long.
     * @return The 64-bit long value corresponding to the byte array.
     * @throws IllegalArgumentException If the byte array is not exactly 8 bytes long.
     */
    public static long bytesToLong(final byte[] bytes) {
        if (bytes.length != 8) {
            throw new IllegalArgumentException("Byte array must be 8 bytes long.");
        }
        long out = 0;
        for (int i = 0; i < 8; i++) {
            out |= ((long) bytes[i] & 0xFF) << (56 - i * 8);
        }
        return out;
    }

    /**
     * Converts a 64-bit long value into a byte array (8 bytes).
     * <p>
     * The byte array is filled with the individual bytes of the long value, in big-endian order.
     * </p>
     *
     * @param value The long value to convert.
     * @param bytes The byte array to store the result. It must have a length of at least 8 bytes.
     */
    public static void longToBytes(final long value, byte[] bytes) {
        if (bytes.length < 8) {
            throw new IllegalArgumentException("Byte array must be at least 8 bytes long.");
        }
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (value >> (56 - i * 8) & 0xFF);
        }
    }
}
