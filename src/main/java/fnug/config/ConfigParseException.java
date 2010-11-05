package fnug.config;

@SuppressWarnings("serial")
public class ConfigParseException extends RuntimeException {

    public ConfigParseException(String msg) {
        super(msg);
    }

    public ConfigParseException(String msg, Exception ex) {
        super(msg, ex);
    }

}
