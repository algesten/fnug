package fnug.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;

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
 * Implementation of {@link Compressor} for javascript. Uses a wrapped Google Closure Compiler.
 * 
 * @author Martin Algesten
 * 
 */
public class JsCompressor implements Compressor {

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "javascript";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] compress(byte[] input) {

        // thread local stuff
        ro.isdc.wro.config.Context wroContext = ro.isdc.wro.config.Context.standaloneContext();
        ro.isdc.wro.config.Context.set(wroContext, new WroConfiguration());

        try {

            GoogleClosureCompressorProcessor processor = new MyGoogleClosureCompressorProcessor();

            StringReader reader = new StringReader(new String(input, "utf-8"));
            StringWriter writer = new StringWriter();
            processor.process(ro.isdc.wro.model.resource.Resource.create("[concatenated]", ResourceType.JS), reader,
                    writer);

            return writer.toString().getBytes("utf-8");

        } catch (UnsupportedEncodingException e) {
            // not happening.
            return null;
        } catch (IOException e) {
            throw new JsCompilationFailedException(e.getMessage(), e);
        }
    }


    class MyGoogleClosureCompressorProcessor extends GoogleClosureCompressorProcessor {

        @Override
        public void process(final Resource resource, final Reader reader, final Writer writer)
                throws IOException {

            CompilationLevel compilationLevel;
            CompilerOptions compilerOptions;

            Field f;
            try {
                f = GoogleClosureCompressorProcessor.class.getDeclaredField("compilerOptions");
                f.setAccessible(true);
                compilerOptions = (CompilerOptions) f.get(this);

                f = GoogleClosureCompressorProcessor.class.getDeclaredField("compilationLevel");
                f.setAccessible(true);
                compilationLevel = (CompilationLevel) f.get(this);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            final String content = IOUtils.toString(reader);

            try {


                Compiler.setLoggingLevel(Level.SEVERE);
                final Compiler compiler = new Compiler();
                if (compilerOptions == null) {
                    compilerOptions = newCompilerOptions();
                }
                compilationLevel.setOptionsForCompilationLevel(compilerOptions);
                //make it play nice with GAE
                compiler.disableThreads();
                compiler.initOptions(compilerOptions);

                final JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
                final String fileName = resource == null ? "wro4j-processed-file.js" : resource.getUri();
                final JSSourceFile input = JSSourceFile.fromInputStream(
                        fileName,
                        new ByteArrayInputStream(content.getBytes(ro.isdc.wro.config.Context.get().getConfig()
                                .getEncoding())));
                final Result result = compiler.compile(extern, input, compilerOptions);
                if (result.success) {
                    writer.write(compiler.toSource());
                } else {

                    StringBuilder msg = new StringBuilder("Compilation failed");

                    if (result.errors != null) {
                        for (JSError error : result.errors) {
                            msg.append("\n");
                            msg.append(error.toString());
                        }
                    }

                    throw new JsCompilationFailedException(msg.toString());
                }
            } finally {
                reader.close();
                writer.close();
            }

        }

    }

}
