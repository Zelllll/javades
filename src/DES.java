import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DES {
    private final byte[] fileBytes;
    private final KeyScheduler keyScheduler;
    private boolean complete = false;

    /**
     * Constructs a new DES object to either encrypt or decrypt a file.
     * <p>
     * This constructor initializes the DES object by loading the file to be processed,
     * validating the provided 56-bit key's parity, and initializing the key scheduler
     * for the encryption or decryption process.
     * </p>
     *
     * @param inPath The path to the input file that will be encrypted or decrypted.
     *               The file is read and processed byte by byte.
     * @param key The 56-bit DES key used for encryption or decryption. It is validated
     *            to ensure correct parity before use.
     * @param encrypt A boolean value indicating whether to encrypt (true) or decrypt (false) the file.
     *
     * @throws IOException If an error occurs while reading the file at the specified path.
     * @throws IllegalArgumentException If the key does not have the correct parity.
     */
    public DES(final String inPath, final long key, final boolean encrypt) throws IOException {
        // Load file to encrypt or decrypt
        File file = new File(inPath);
        fileBytes = Files.readAllBytes(file.toPath());

        // Validate the key and initialize the key scheduler
        validateKeyParity(key);
        keyScheduler = new KeyScheduler(key, encrypt);
    }

    // @TODO: Implement the f Function
    private long fFunction(long halfBlock, long roundKey) {
        return 0;
    }

    /**
     * Processes a 64-bit block of data through the DES encryption or decryption process.
     * The block is first permuted, split into left and right halves, and then processed through
     * 16 rounds of DES. After the rounds, the halves are combined and the final permutation is applied.
     *
     * @param block The 64-bit block to be processed. This can represent either a plaintext block (for encryption)
     *              or a ciphertext block (for decryption).
     * @return      The processed 64-bit block after applying the Initial Permutation (IP), 16 rounds of
     *              DES (using the provided round keys), and the Final Permutation (FP).
     */
    private long processBlock(long block) {
        // 1. Apply Initial Permutation (IP)
        block = PermutationTables.permute(block, PermutationTables.IP);

        // 2. Split into left and right halves
        long left = (block >> 32) & 0xFFFFFFFFL;
        long right = block & 0xFFFFFFFFL;

        // 3. Perform 16 rounds (the same logic for both encryption and decryption)
        for (int i = 0; i < 16; i++) {
            long roundKey = keyScheduler.getNextRoundKey();
            long newRight = left ^ fFunction(right, roundKey);

            // Update left and right halves
            left = right;
            right = newRight;
        }

        // 4. Combine the halves back together
        long combined = (left << 32) | right;

        // 5. Apply Final Permutation (FP)
        return PermutationTables.permute(combined, PermutationTables.IP_INV);
    }

    /**
     * Performs encryption or decryption on the loaded file data in blocks of 8 bytes.
     * The processed file data is then written to the specified output path.
     * <p>
     * The method processes the file block by block, where each block is encrypted or decrypted using the
     * DES algorithm. After processing all blocks, the modified file is saved to the provided output path.
     * </p>
     *
     * @param outPath The path where the processed file will be saved after encryption or decryption.
     * @return        {@code true} if the file was successfully processed and saved, {@code false} otherwise.
     */
    public boolean performAndSave(String outPath) {
        if (!complete) {
            byte[] blockAsBytes = new byte[8];

            // Perform encryption/decryption block by block
            for (int i = 0; i < fileBytes.length; i += 8) {
                // Isolate the next block to process
                System.arraycopy(fileBytes, i, blockAsBytes, 0, 8);

                // Convert it from an array of bytes to a long
                long blockAsLong = Utilities.bytesToLong(blockAsBytes);

                // Encipher or decipher each block
                long processedBlock = processBlock(blockAsLong);

                // Convert the processed block back to bytes
                Utilities.longToBytes(processedBlock, blockAsBytes);

                // Save the processed block back into the fileBytes array in place
                System.arraycopy(blockAsBytes, 0, fileBytes, i, 8);
            }
        }

        // Mark process as complete in case the file is attempted to be written multiple times
        complete = true;

        // Write the processed fileBytes to the output file
        try {
            Files.write(new File(outPath).toPath(), fileBytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validates the DES key parity by ensuring that each byte of the 56-bit key has an odd number of 1 bits.
     * DES requires each byte to have an odd number of 1s (odd parity).
     *
     * @param key A 56-bit key (8 bytes). The key should be passed as a 64-bit value where
     *            the least significant bit of each byte is used for parity (so the key is effectively 56-bits).
     */
    public static void validateKeyParity(final long key) {
        for (int i = 0; i < 8; i++) {
            byte currentByte = (byte) (key >> (i * 8));
            int onesCount = Integer.bitCount(currentByte & 0xFF);

            if (onesCount % 2 == 0) {
                throw new IllegalArgumentException("Invalid key parity: " +
                        "Key must have an odd number of 1 bits in each byte.");
            }
        }
    }
}
