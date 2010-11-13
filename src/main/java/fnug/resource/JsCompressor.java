package fnug.resource;

import java.io.UnsupportedEncodingException;

import googccwrap.CompilationFailedException;
import googccwrap.GoogleClosureCompilerWrapper;

/**
 * Implementation of {@link Compressor} for javascript. Uses a wrapped Google
 * Closure Compiler.
 * 
 * @author Martin Algesten
 * 
 */
public class JsCompressor implements Compressor {

    private GoogleClosureCompilerWrapper wrapper;

    /**
     * Constructs potentially sending configuration options to the wrapped
     * google closure compiler.
     * 
     * @param args
     *            arguments to send.
     */
    public JsCompressor(String... args) {
        wrapper = new GoogleClosureCompilerWrapper(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] compress(byte[] input) {
        try {
            String s = wrapper.compileString(new String(input, "utf-8"));
            return s.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            // not happening.
            return null;
        } catch (CompilationFailedException e) {
            throw new JsCompilationFailedException(e.getMessage(), e);
        }
    }

}
