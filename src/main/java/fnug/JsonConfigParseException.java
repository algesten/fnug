package fnug;

import org.codehaus.jackson.JsonLocation;

@SuppressWarnings("serial")
public class JsonConfigParseException extends ConfigParseException {

    public JsonConfigParseException(String string, JsonLocation currentLocation) {
        this(string, currentLocation, null);
    }

    public JsonConfigParseException(String string, JsonLocation currentLocation, Exception ex) {
        super("At line " + currentLocation.getLineNr() + " col " + currentLocation.getColumnNr() + ": " + string, ex);
    }

}
