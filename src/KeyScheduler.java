public class KeyScheduler {
    private final long key;
    private final long[] roundKeys = new long[16];
    private int round;
    private final boolean encrypt;

    /**
     * Initializes the KeyScheduler and generates the round keys for DES encryption or decryption.
     * @param key      The 64-bit encryption key.
     * @param encrypt {@code true} if generating round keys for encryption,
     *                {@code false} if generating them for decryption.
     */
    public KeyScheduler(final long key, final boolean encrypt) {
        this.key = key;
        this.encrypt = encrypt;
        this.round = encrypt ? 0 : roundKeys.length - 1;
        generateRoundKeys();
    }

    /**
     * Retrieves the next round key for encryption or decryption.
     * @return The next round key.
     * @throws IllegalStateException if no more round keys are available.
     */
    public long getNextRoundKey() {
        // Ensure that we remain within the array bounds
        if (round < 0 || round > roundKeys.length - 1) {
            throw new IllegalStateException("No more round keys available.");
        }
        // Read round keys in reverse order if performing decryption
        return encrypt ? roundKeys[round++] : roundKeys[round--];
    }

    /**
     * Generates the 16 round keys by performing key scheduling.
     */
    private void generateRoundKeys() {
        // 1. Perform Permuted Choice 1
        long pc1Result = PermutationTables.permute(key, PermutationTables.PC_1);
        long leftHalf = (pc1Result >> 28) & 0xFFFFFFF;
        long rightHalf = pc1Result & 0xFFFFFFF;

        // 2. Perform Transform 1-16
        for (int i = 0; i < roundKeys.length; i++) {
            leftHalf = leftShift(leftHalf, i);
            rightHalf = leftShift(rightHalf, i);
            long combined = (leftHalf << 28) | rightHalf;
            roundKeys[i] = PermutationTables.permute(combined, PermutationTables.PC_2);
        }
    }

    /**
     * Left shifts a 28-bit half by the number of positions required.
     * The shift amount depends on the round number.
     * @param half The 28-bit half to be shifted.
     * @param round The round number (used to determine the shift count).
     * @return The shifted 28-bit value.
     */
    private long leftShift(final long half, final int round) {
        final int[] shiftAmounts = {1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2};
        int shiftAmount = shiftAmounts[round];
        return ((half << shiftAmount) | (half >> (28 - shiftAmount))) & 0xFFFFFFF;
    }
}
