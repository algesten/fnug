package fnug.resource;

import java.io.UnsupportedEncodingException;

import googccwrap.CompilationFailedException;
import googccwrap.GoogleClosureCompilerWrapper;

public class JsCompressor {

    private GoogleClosureCompilerWrapper wrapper;

    public JsCompressor(String... args) {
        wrapper = new GoogleClosureCompilerWrapper(args);
    }

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
