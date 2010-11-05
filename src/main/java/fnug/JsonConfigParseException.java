package fnug;

import org.codehaus.jackson.JsonLocation;

@SuppressWarnings("serial")
public class JsonConfigParseException extends ConfigParseException {

    private JsonConfigParseException(String msg, JsonLocation loc, Exception ex) {
        super("At line " + loc.getLineNr() + " col " + loc.getColumnNr() + ": " + msg, ex);
    }

    public JsonConfigParseException(String msg, JsonLocation lock) {
        this(msg, lock, null);
    }

    public JsonConfigParseException(JsonLocation loc, Exception ex) {
        this(ex.getMessage(), loc, ex);
    }

}
