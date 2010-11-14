package fnug.servlet;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

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

@JsonPropertyOrder({ "fullPath", "html" })
@SuppressWarnings("unused")
public class JsonJSLintResult {

    @JsonProperty
    private String fullPath;
    @JsonProperty
    private String html;

    public JsonJSLintResult(String fullPath, String html) {
        this.fullPath = fullPath;
        this.html = filter(html);
    }

    private String filter(String html) {
        return html.replace("<br>", "");
    }

}
