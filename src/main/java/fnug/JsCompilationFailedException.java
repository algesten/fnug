package fnug;

@SuppressWarnings("serial")
public class JsCompilationFailedException extends RuntimeException {

    public JsCompilationFailedException(String msg, Exception ex) {
        super(msg, ex);
    }

}
