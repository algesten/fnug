package fnug.resource;

import java.io.UnsupportedEncodingException;

import googccwrap.CompilationFailedException;
import googccwrap.GoogleClosureCompilerWrapper;

/*
 Copyright 2010 Martin Algesten

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

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
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "javascript";
    }

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
