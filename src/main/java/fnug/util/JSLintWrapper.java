package fnug.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

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

    private String args;


    /**
     * Construct passing the configuration arguments.
     * 
     * @param args
     *            configs
     */
    public JSLintWrapper(String args) {

        // change lint string to that which wro4j expects
        if (args != null) {

            if (args.indexOf("jslint ") == 0) {
                args = args.substring(7);
            }

            args = args.replaceAll(" ", "");

            args = args.replaceAll(":", "=");
            
        }
        
        this.args = args;

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
    public String lint(String systemId, String javaScript) {

        StringReader reader = new StringReader(javaScript);
        StringWriter writer = new StringWriter();

        @SuppressWarnings("unchecked")
        final Collection<LinterError>[] errors = new Collection[1];

        JsLintProcessor processor = new JsLintProcessor() {

            @Override
            protected void onLinterException(LinterException e, Resource resource) {
                errors[0] = e.getErrors();
            }
        };

        processor.setOptions(args);

        try {
            processor.process(Resource.create(systemId, ResourceType.JS), reader, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder builder = null;

        if (errors[0] != null) {
            builder = new StringBuilder();
            Collection<LinterError> errs = errors[0];
            builder.append("<div id='errors'>");
            for (LinterError err : errs) {
                if (err == null) {
                    continue;
                }
                builder.append("<p>Line ");
                builder.append(err.getLine());
                builder.append(" char ");
                builder.append(err.getCharacter());
                builder.append(": ");
                builder.append(err.getReason());
                if (err.getEvidence() != null) {
                    builder.append("</p><p class='evidence'>");
                    builder.append(err.getEvidence());
                }
                builder.append("</p>");
            }
            builder.append("</div>");
        }

        return builder == null ? null : builder.toString();

    }

    // <div id=errors>
    // <i>Error:</i><p>Problem at line 18 character 5: Expected an assignment or function call and instead saw an expression.</p>
    //                <p class=evidence> dfg</p>
    //              <p>Problem at line 18 character 8: Expected ';' and instead saw 'var'.</p>
    //                 <p class=evidence> dfg</p><p>
    // <i>Undefined variable:</i> <code><u>dfg</u></code>&nbsp;<i>10 </i> <small>quote</small></p>
    // <p><i>Unused variable:</i> <code><u>zxcqwd</u></code>&nbsp;<i>10 </i> <small>quote</small></p>
    // </div>

}
