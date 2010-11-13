package fnug.resource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import andyr.compressor.AndyRYUICssCompressor;

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
 * Css variant of the compressor. Uses a modified version of the YUICompressor
 * that is included in the package.
 * 
 * @author Martin Algesten
 * 
 */
public class CssCompressor implements Compressor {

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] compress(byte[] input) {

        if (input == null) {
            return null;
        }

        StringReader reader = null;
        try {
            reader = new StringReader(new String(input, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            // unlikely.
        }

        StringWriter writer = new StringWriter();

        try {
            AndyRYUICssCompressor cmp = new AndyRYUICssCompressor(reader);
            cmp.compress(writer, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            return writer.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }
}
