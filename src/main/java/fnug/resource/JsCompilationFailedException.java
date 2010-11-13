package fnug.resource;

/**
 * Thrown when javascript compilation fails.
 * 
 * @author Martin Algesten
 * 
 */
@SuppressWarnings("serial")
public class JsCompilationFailedException extends RuntimeException {

    /**
     * Constructs setting args.
     * 
     * @param msg
     *            The message.
     * @param ex
     *            Wrapped exception.
     */
    public JsCompilationFailedException(String msg, Exception ex) {
        super(msg, ex);
    }

}
