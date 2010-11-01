package fnug;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class JsonConfigParser implements ConfigParser {

    private static final String KEY_MATCHES = "matches";
    private static final String KEY_JS_LINT = "jsLint";
    private static final String KEY_CHECK_MODIFIED = "checkModified";
    private static final String KEY_JS_COMPILER_ARGS = "jsCompilerArgs";
    private static final String KEY_FILES = "files";
    private static final String[] EMPTY_STRINGS = new String[] {};
    private JsonFactory jsonFactory = new JsonFactory();

    @Override
    public List<Config> parse(Resource res) {

        LinkedList<Config> result = new LinkedList<Config>();

        JsonParser parser = null;

        try {

            parser = jsonFactory.createJsonParser(res.getBytes());

            String basePath = extractBasePath(res);

            LinkedList<BundleConfig> bundleConfigs = new LinkedList<BundleConfig>();

            while (parser.nextToken() != null) {
                DefaultBundleConfig config = buildConfig(parser, basePath);
                bundleConfigs.add(config);
            }

            result.add(new DefaultConfig(bundleConfigs));

        } catch (Exception e) {
            if (parser != null) {
                throw new JsonConfigParseException("Failed to parse", parser.getCurrentLocation(), e);
            } else {
                throw new ConfigParseException("Failed to parse", e);
            }
        }

        return result;

    }

    private String extractBasePath(Resource res) {

        // may not point to a file in the file syste, but handy to pick out
        // path.
        File f = new File(res.getPath());

        return f.getParent();

    }

    private DefaultBundleConfig buildConfig(JsonParser parser, String basePath) throws JsonParseException, IOException {

        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new JsonConfigParseException("Expected object", parser.getCurrentLocation());
        }

        String name = parser.getCurrentName();

        JsonLocation loc = parser.getCurrentLocation();
        JsonNode node = parser.readValueAsTree();

        Pattern[] matches = parsePatternArray(node, KEY_MATCHES, loc);
        boolean jsLint = parseBoolean(node, KEY_JS_LINT, loc, DefaultBundleConfig.DEFAULT_JS_LINT);
        boolean checkModified = parseBoolean(node, KEY_CHECK_MODIFIED, loc, DefaultBundleConfig.DEFAULT_CHECK_MODIFIED);
        String[] jsCompileArgs = parseStringArray(node, KEY_JS_COMPILER_ARGS, loc, EMPTY_STRINGS);
        String[] files = parseStringArray(node, KEY_FILES, loc, null);

        return new DefaultBundleConfig(name, basePath, matches, jsLint, checkModified, jsCompileArgs, files);
    }

    private String[] parseStringArray(JsonNode node, String key, JsonLocation loc, String[] def) {

        if (!node.has(key)) {
            if (def == null) {
                throw new JsonConfigParseException("Missing key '" + key + "'", loc);
            } else {
                return def;
            }
        }

        JsonNode m = node.get(key);
        if (!m.isArray()) {
            throw new JsonConfigParseException("Key '" + key + "' is not array", loc);
        }
        String[] vals = new String[m.size()];
        int i = 0;
        for (JsonNode j : m) {
            vals[i++] = j.getValueAsText();
        }
        return vals;

    }

    private boolean parseBoolean(JsonNode node, String key, JsonLocation loc, boolean def) {

        if (!node.has(key)) {
            return def;
        }

        JsonNode m = node.get(key);
        if (!m.isBoolean()) {
            throw new JsonConfigParseException("Key '" + key + "' is not a boolean value", loc);
        }

        return m.getBooleanValue();

    }

    private Pattern[] parsePatternArray(JsonNode node, String key, JsonLocation loc) {

        String[] vals = parseStringArray(node, key, loc, null);

        Pattern[] pats = new Pattern[vals.length];

        int i = 0;
        for (String s : vals) {
            pats[i++] = Pattern.compile(s);
        }

        return pats;

    }
}
