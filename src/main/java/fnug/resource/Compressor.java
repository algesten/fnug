package fnug.resource;

/**
 * Interface for compressors of bytes, javascript or css.
 * 
 * @author Martin Algesten
 * 
 */
public interface Compressor {

    /**
     * Compresses the given input of bytes.
     * 
     * @param input
     *            bytes to compress
     * @return the compressed bytes.
     */
    byte[] compress(byte[] input);

}
