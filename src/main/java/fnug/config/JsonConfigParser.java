package fnug.config;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fnug.resource.DefaultResource;
import fnug.resource.Resource;

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
 * Implementation of {@link ConfigParser} that reads json config files.
 * 
 * @author Martin Algesten
 * 
 */
public class JsonConfigParser implements ConfigParser {

    private static final Logger LOG = LoggerFactory.getLogger(JsonConfigParser.class);

    private static final String KEY_JS_LINT = "jsLint";
    private static final String KEY_CHECK_MODIFIED = "checkModified";
    private static final String KEY_JS_COMPILER_ARGS = "jsCompilerArgs";
    private static final String KEY_FILES = "files";
    private static final String KEY_BASE_PATH = "basePath";
    private static final HashSet<String> ALL_KEYS = new HashSet<String>();

    static {
        ALL_KEYS.add(KEY_JS_LINT);
        ALL_KEYS.add(KEY_CHECK_MODIFIED);
        ALL_KEYS.add(KEY_JS_COMPILER_ARGS);
        ALL_KEYS.add(KEY_FILES);
        ALL_KEYS.add(KEY_BASE_PATH);
    }

    private static final String[] EMPTY_STRINGS = new String[] {};

    private static final Pattern jsonPattern = Pattern.compile(".*?(\\{.*\\}).*", Pattern.DOTALL);
    
    private ObjectMapper mapper;
    private JsonFactory jsonFactory;

    public JsonConfigParser() {
        configureJsonParser();
    }

    private void configureJsonParser() {
        mapper = new ObjectMapper();

        // relax the default oh so anal json defaults.
        mapper.configure(Feature.ALLOW_COMMENTS, true);
        mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        jsonFactory = mapper.getJsonFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Config parse(Resource res) {

        if (res == null) {
            throw new IllegalArgumentException("Null resource not allowed");
        }
        if (res.getLastModified() == -1) {
            throw new IllegalArgumentException("Resource not found: " + res.getFullPath());
        }

        JsonParser parser = null;

        try {

        	/* 
        	 * Make sure the parser is fed JSON and not JavaScript
        	 * 1. Fetch the bytes from the resource
        	 * 2. Create a String from the bytes using UTF-8 encoding (convention)
        	 * 3. Strip away everything before first { and after last }
        	 */
        	byte[] jsonBytes = res.getBytes();
        	String json = new String(jsonBytes, "UTF-8");
        	Matcher m = jsonPattern.matcher(json);
        	if (m.matches()) {
        		json = m.group(1);
        	} else {
        		throw new IllegalArgumentException("Invalid JSON or JavaScript configuration '" + res.getPath() + "'");
        	}
        	
            parser = jsonFactory.createJsonParser(json);

            LinkedList<BundleConfig> bundleConfigs = new LinkedList<BundleConfig>();

            if (parser.nextToken() == null) {
                throw new IllegalArgumentException("Empty config file '" + res.getPath() + "'");
            }

            if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException("Config file is not a json object");
            }

            JsonNode root = parser.readValueAsTree();

            Iterator<String> iter = root.getFieldNames();
            while (iter.hasNext()) {
                String name = iter.next();
                DefaultBundleConfig config = buildConfig(root.get(name), name, parser.getCurrentLocation(),
                        res);

                LOG.info("Configured bundle: " + config.name());

                bundleConfigs.add(config);
            }

            if (bundleConfigs.isEmpty()) {
                throw new IllegalArgumentException("Failed to read at least " +
                        "one bundle config in '" + res.getPath() + "'");
            }

            return new DefaultConfig(bundleConfigs);

        } catch (Exception e) {
            if (parser != null) {
                throw new JsonConfigParseException(parser.getCurrentLocation(), e);
            } else {
                throw new ConfigParseException("Failed to parse", e);
            }
        }

    }

    private DefaultBundleConfig buildConfig(JsonNode node, String name, JsonLocation loc, Resource configResource)
            throws JsonParseException, IOException {

        Iterator<String> iter = node.getFieldNames();
        while (iter.hasNext()) {
            String s = iter.next();
            if (!ALL_KEYS.contains(s)) {
                throw new JsonConfigParseException(loc, "Unknown config key: " + s);
            }
        }

        String[] jsLintArgs = parseJsLintArgs(node, KEY_JS_LINT, loc);
        int checkModifiedInterval = parseInt(node, KEY_CHECK_MODIFIED, loc,
                DefaultResource.DEFAULT_CHECK_MODIFIED_INTERVAL);
        String[] jsCompileArgs = parseStringArray(node, KEY_JS_COMPILER_ARGS, loc, EMPTY_STRINGS);
        String[] files = parseStringArray(node, KEY_FILES, loc, EMPTY_STRINGS);
        String basePath = parseString(node, KEY_BASE_PATH, loc, configResource.getBasePath());

        if (!basePath.endsWith("/")) {
            throw new JsonConfigParseException(loc, "'basePath' must end with slash: " + basePath);
        } else {
            
            URL url = DefaultResource.getResourceURL(basePath);
            
            if (url == null) {
                throw new JsonConfigParseException(loc, "No directory found for 'basePath': " + basePath);
            }

        }

        for (String file : files) {
            if (file.startsWith("/")) {
                throw new JsonConfigParseException(loc, "File path must not start with slash: " + file);
            }
        }

        return new DefaultBundleConfig(configResource, name, basePath, jsLintArgs,
                checkModifiedInterval, jsCompileArgs,
                files);
    }

    private final static Pattern JSLINT_ARG_PAT = Pattern.compile("\\w+:\\s*\\w+");

    private String[] parseJsLintArgs(JsonNode node, String key, JsonLocation loc) {

        String s = parseString(node, key, loc, "");

        Matcher m = JSLINT_ARG_PAT.matcher(s);

        LinkedList<String> args = new LinkedList<String>();

        while (m.find()) {
            args.add(m.group());
        }

        return args.toArray(new String[args.size()]);
    }

    private String parseString(JsonNode node, String key, JsonLocation loc, String def) {

        if (!node.has(key)) {
            return def;
        }

        JsonNode m = node.get(key);
        if (m.isNull()) {
            return def;
        }
        if (!m.isTextual()) {
            throw new JsonConfigParseException(loc, "Key '" + key + "' is not an string value");
        }

        return m.asText();

    }

    private int parseInt(JsonNode node, String key, JsonLocation loc, int def) {

        if (!node.has(key)) {
            return def;
        }

        JsonNode m = node.get(key);
        if (m.isNull()) {
            return def;
        }
        if (!m.isInt()) {
            throw new JsonConfigParseException(loc, "Key '" + key + "' is not an integer value");
        }

        return m.getIntValue();

    }

    private String[] parseStringArray(JsonNode node, String key, JsonLocation loc, String[] def) {

        if (!node.has(key)) {
            if (def == null) {
                throw new JsonConfigParseException(loc, "Missing key '" + key + "'");
            } else {
                return def;
            }
        }

        JsonNode m = node.get(key);
        if (m.isNull()) {
            return def;
        }
        if (!m.isArray()) {
            throw new JsonConfigParseException(loc, "Key '" + key + "' is not array");
        }
        String[] vals = new String[m.size()];
        int i = 0;
        for (JsonNode j : m) {
            vals[i++] = j.asText();
        }
        return vals;

    }

}
