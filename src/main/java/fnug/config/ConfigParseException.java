package fnug.config;

/**
 * Exception thrown when parsing a config.
 * 
 * @author Martin Algesten
 * 
 */
@SuppressWarnings("serial")
public class ConfigParseException extends RuntimeException {

    /**
     * Constructs with message.
     * 
     * @param msg
     *            Message.
     */
    public ConfigParseException(String msg) {
        super(msg);
    }

    /**
     * Constructs with message and wrapped exception.
     * 
     * @param msg
     *            Message
     * @param ex
     *            Wrapped exception.
     */
    public ConfigParseException(String msg, Exception ex) {
        super(msg, ex);
    }

}
