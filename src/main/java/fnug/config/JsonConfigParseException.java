package fnug.config;

import org.codehaus.jackson.JsonLocation;

/**
 * Specialisation of {@link ConfigParseException} for JsonConfigParser.
 * 
 * @author Martin Algesten
 * 
 */
@SuppressWarnings("serial")
public class JsonConfigParseException extends ConfigParseException {

    private JsonConfigParseException(String msg, JsonLocation loc, Exception ex) {
        super("At line " + loc.getLineNr() + " col " + loc.getColumnNr() + ": " + msg, ex);
    }

    /**
     * Constructs with message and location.
     * 
     * @param msg
     *            Message
     * @param loc
     *            Location
     */
    public JsonConfigParseException(String msg, JsonLocation loc) {
        this(msg, loc, null);
    }

    /**
     * Constructs with location and wrapped exception.
     * 
     * @param loc
     *            Location
     * @param ex
     *            Wrapped exception.
     */
    public JsonConfigParseException(JsonLocation loc, Exception ex) {
        this(ex.getMessage(), loc, ex);
    }

}
