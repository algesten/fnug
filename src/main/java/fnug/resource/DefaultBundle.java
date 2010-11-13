package fnug.resource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import fnug.config.BundleConfig;

/**
 * Default implementation of {@link Bundle}.
 * 
 * @author Martin Algesten
 * 
 */
public class DefaultBundle implements Bundle {

    private static final String SUFFIX_CSS = "css";

    private static final String SUFFIX_JS = "js";

    /**
     * Arbitrary max size for cached resources. We want to avoid filling the
     * heap space with resources pointing to non-existing files. If we hit this
     * limit, something is probably wrong.
     */
    private static final int MAX_CACHE = 10000;

    private BundleConfig config;

    private volatile HashMap<String, Resource> cache = new HashMap<String, Resource>();

    private volatile ResourceCollection[] resourceCollections;

    private Pattern bundlePattern;

    /**
     * Constructs a bundle from the given config object.
     * 
     * @param config
     *            config to construct from.
     */
    public DefaultBundle(BundleConfig config) {
        this.config = config;
        bundlePattern = Pattern.compile(getName() + "/[a-f0-9]+\\.(js|css)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleConfig getConfig() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return config.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resolve(String path) {

        if (bundlePattern.matcher(path).matches()) {

            String collFile = path.substring(path.indexOf("/") + 1);
            int indx = collFile.indexOf(".");
            String collPath = collFile.substring(0, indx);
            String suffix = collFile.substring(indx + 1);

            ResourceCollection c = getResourceCollection(collPath);

            if (c != null) {
                return getCompressedBySuffix(c, suffix);
            }

            // do not return null here, but proceed to return "normal" resource.

        }

        Resource res = cache.get(path);
        if (res == null) {
            synchronized (this) {
                res = cache.get(path);
                if (res == null) {
                    if (cache.size() > MAX_CACHE) {
                        throw new IllegalStateException("Cache is larger than " + MAX_CACHE);
                    }
                    res = makeResource(path);
                    cache.put(path, res);
                }
            }
        }
        return res;

    }

    private ResourceCollection getResourceCollection(String collPath) {
        ResourceCollection[] colls = getResourceCollections();
        for (ResourceCollection coll : colls) {
            if (coll.getPath().equals(collPath)) {
                return coll;
            }
        }
        return null;
    }

    private Resource getCompressedBySuffix(ResourceCollection c, String suffix) {
        if (suffix.equals(SUFFIX_JS)) {
            return c.getCompressedJs();
        } else if (suffix.equals(SUFFIX_CSS)) {
            return c.getCompressedCss();
        }
        return null;
    }

    /**
     * Can be overridden to provide other implementations of {@link Resource}
     * than the {@link DefaultBundleResource}.
     * 
     * @param path
     *            the path to construct a resource around.
     * @return the constructed resource, which is an instance of
     *         {@link DefaultBundleResource}.
     */
    protected Resource makeResource(String path) {
        return new DefaultBundleResource(this, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceCollection[] getResourceCollections() {
        if (resourceCollections == null) {
            synchronized (this) {
                if (resourceCollections == null) {
                    resourceCollections = buildResourceCollections();
                }
            }
        }
        return resourceCollections;
    }

    private ResourceCollection[] buildResourceCollections() {

        LinkedList<Resource> l = new LinkedList<Resource>();
        for (String file : config.files()) {
            l.add(ResourceResolver.getInstance().resolve(file));
        }

        Tarjan tarjan = new Tarjan(l);

        List<List<Resource>> order = tarjan.getResult();

        LinkedHashMap<Bundle, List<Resource>> bundleResources = new LinkedHashMap<Bundle, List<Resource>>();

        for (List<Resource> cur : order) {
            if (cur.size() > 1) {
                StringBuilder bld = new StringBuilder();
                for (Resource r : cur) {
                    bld.append(r.getPath() + " -> ");
                }
                bld.append(cur.get(0));
                throw new IllegalStateException("Found cyclic dependency: " + bld.toString());
            }
            Resource r = cur.get(0);
            if (!(r instanceof HasBundle)) {
                throw new IllegalStateException("Can only resolve dependencies resources implementing HasBundle");
            }
            Bundle b = ((HasBundle) r).getBundle();
            List<Resource> lr = bundleResources.get(b);
            if (lr == null) {
                lr = new LinkedList<Resource>();
                bundleResources.put(b, lr);
            }
            lr.add(r);
        }

        ResourceCollection[] result = new ResourceCollection[bundleResources.size()];

        int i = 0;
        for (Bundle b : bundleResources.keySet()) {
            List<Resource> lr = bundleResources.get(b);
            Resource[] alr = lr.toArray(new Resource[lr.size()]);
            result[i++] = new DefaultResourceCollection(b, "/" + config.name() + "/", alr, null);
        }

        return result;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastModified() {
        long mostRecent = config.configResource().getLastModified();
        for (ResourceCollection rc : getResourceCollections()) {
            mostRecent = Math.max(mostRecent, rc.getLastModified());
        }
        return mostRecent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkModified() {
        boolean modified = false;
        for (ResourceCollection rc : getResourceCollections()) {
            modified = rc.checkModified() || modified;
        }
        if (modified) {
            synchronized (this) {
                resourceCollections = null;
            }
        }
        return modified;
    }

}
