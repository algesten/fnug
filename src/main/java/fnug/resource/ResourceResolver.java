package fnug.resource;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fnug.config.BundleConfig;
import fnug.config.Config;
import fnug.config.ConfigParser;
import fnug.config.JsonConfigParser;

public class ResourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceResolver.class);

    private static final String SEPARATOR = "/";
    private List<Resource> configResources;
    private volatile List<Config> configs = new LinkedList<Config>();
    private ConfigParser configParser = new JsonConfigParser();
    private LinkedHashMap<String, Bundle> bundles = new LinkedHashMap<String, Bundle>();
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
        initConfigs();
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

                    if (configResource.getLastModified() == -1) {
                        LOG.warn("Config file missing: " + configResource.getFullPath());
                        continue;
                    }

                    LOG.info("Reading config: " + configResource.getFullPath());

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

    public List<Bundle> getBundles() {
        return new LinkedList<Bundle>(bundles.values());
    }

    public long getLastModified() {
        long mostRecent = -1;
        for (Bundle b : getBundles()) {
            mostRecent = Math.max(mostRecent, b.getLastModified());
        }
        return mostRecent;
    }

}
