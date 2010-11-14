package fnug.servlet;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

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
