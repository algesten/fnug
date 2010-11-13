package fnug.resource;

/**
 * Implementors have bytes that are time stamped.
 * 
 * @author Martin Algesten
 * 
 */
public interface HasLastModifiedBytes {

    /**
     * The bytes.
     * 
     * @return the timestamped bytes.
     */
    byte[] getBytes();

    /**
     * The timestamp when the bytes were last changed.
     * 
     * @return the last modified of the {@link #getBytes()}.
     */
    long getLastModified();

}
