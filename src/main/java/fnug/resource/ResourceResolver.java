package fnug.resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import fnug.config.BundleConfig;
import fnug.config.Config;
import fnug.config.ConfigParser;
import fnug.config.JsonConfigParser;

public class ResourceResolver {

    private static final String SEPARATOR = "/";
    private List<Resource> configResources;
    private volatile List<Config> configs = new LinkedList<Config>();
    private ConfigParser configParser = new JsonConfigParser();
    private HashMap<String, Bundle> bundles = new HashMap<String, Bundle>();
    private LinkedHashMap<Pattern, Bundle> patterns = new LinkedHashMap<Pattern, Bundle>();

    private static ThreadLocal<ResourceResolver> instance = new ThreadLocal<ResourceResolver>();

    public void setThreadLocal() {
        setThreadLocal(this);
    }

    protected static void setThreadLocal(ResourceResolver resolver) {
        instance.set(resolver);
    }

    public static ResourceResolver getInstance() {
        return instance.get();
    }

    public ResourceResolver(List<Resource> configResources) {
        if (configResources == null || configResources.isEmpty()) {
            throw new IllegalArgumentException("Need at least one config resource");
        }
        this.configResources = configResources;
    }

    protected ResourceResolver() {
    }

    protected void setConfigs(Config... configs) {
        this.configs = Arrays.asList(configs);
        initBundles();
    }

    public Resource resolve(String path) {
        if (configs.isEmpty()) {
            initConfigs();
        }
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Can't resolve empty path");
        }
        if (path.startsWith(SEPARATOR)) {
            throw new IllegalArgumentException("Path must not start with '" + SEPARATOR + "'");
        }
        if (path.endsWith(SEPARATOR)) {
            throw new IllegalArgumentException("Path must not end with '" + SEPARATOR + "'");
        }
        for (Bundle bundle : bundles.values()) {
            if (path.startsWith(bundle.getName() + "/")) {
                return bundle.resolve(path);
            }
        }
        for (Pattern pat : patterns.keySet()) {
            if (pat.matcher(path).matches()) {
                Bundle bundle = patterns.get(pat);
                return bundle.resolve(path);
            }
        }
        return null;
    }

    public Bundle getBundle(String name) {
        if (configs.isEmpty()) {
            initConfigs();
        }
        return bundles.get(name);
    }

    private void initConfigs() {
        synchronized (this) {
            if (configs.isEmpty()) {
                bundles.clear();
                patterns.clear();
                for (Resource configResource : configResources) {
                    Config parsedConfig = configParser.parse(configResource);
                    configs.add(parsedConfig);
                    initBundles();
                }
            }
        }
    }

    private void initBundles() {
        for (Config cfg : configs) {
            for (BundleConfig bcfg : cfg.getBundleConfigs()) {
                Bundle bundle = new DefaultBundle(bcfg);
                bundles.put(bundle.getName(), bundle);
                for (Pattern pat : bcfg.matches()) {
                    patterns.put(pat, bundle);
                }
            }
        }
    }

}
