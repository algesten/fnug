package fnug.util;

import java.io.IOException;
import java.lang.reflect.Field;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jslint4java.JSLint;
import com.googlecode.jslint4java.JSLintBuilder;
import com.googlecode.jslint4java.JSLintResult;
import com.googlecode.jslint4java.Option;

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
 * Wrapper around JSLint4Java since that package doesn't manage the rhino Context thread local appropriately.
 */
public class JSLintWrapper {

    private final static Logger LOG = LoggerFactory.getLogger(JSLintWrapper.class);

    private static ContextFactory ctxFactory;
    private JSLintBuilder jsLintBuilder;
    private Context ctx;
    private JSLint jsLint;

    /**
     * Construct passing the configuration arguments.
     * 
     * @param args
     *            configs
     */
    public JSLintWrapper(String... args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Null or empty config args");
        }

        try {

            // this calls Context.enterContext(), which means the thread now has
            // a thread associated Context.
            jsLintBuilder = new JSLintBuilder();
            initCtx();

            try {
                jsLint = jsLintBuilder.fromDefault();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            configure(args);

        } finally {
            // now we exit the context to make sure the thread is not polluted.
            Context.exit();
        }

    }

    private void initCtx() {
        try {
            Field fCtx = jsLintBuilder.getClass().getDeclaredField("ctx");
            fCtx.setAccessible(true);
            ctx = (Context) fCtx.get(jsLintBuilder);
            if (ctxFactory == null) {
                Field fCtxFactory = jsLintBuilder.getClass().getDeclaredField("CONTEXT_FACTORY");
                fCtxFactory.setAccessible(true);
                ctxFactory = (ContextFactory) fCtxFactory.get(null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void configure(String[] args) {

        for (String arg : args) {

            String[] split = arg.split("\\s*:\\s*");

            Option opt;
            try {
                opt = Option.valueOf(split[0].toUpperCase());
            } catch (Exception e) {
                LOG.warn("Ignoring unknown JSLint option: " + arg);
                continue;
            }

            if (split.length == 1 || split[1].equalsIgnoreCase("true")) {
                jsLint.addOption(opt);
            } else {
                jsLint.addOption(opt, split[1]);
            }

        }

    }

    /**
     * Check for problems in JavaScript source.
     * 
     * @param systemId
     *            a filename
     * @param javaScript
     *            a String of JavaScript source code.
     * 
     * @return a {@link JSLintResult}.
     */
    public JSLintResult lint(String systemId, String javaScript) {
        try {

            // reassociate the context with the thread.
            ctxFactory.enterContext(ctx);

            return jsLint.lint(systemId, javaScript);

        } finally {

            // and remove it.
            Context.exit();

        }
    }

}
