package fnug.resource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import fnug.config.BundleConfig;

public class DefaultBundle implements Bundle {

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

    public DefaultBundle(BundleConfig config) {
        this.config = config;
        bundlePattern = Pattern.compile(getName() + "/[a-f0-9]+\\.(js|css)");
    }

    @Override
    public BundleConfig getConfig() {
        return config;
    }

    @Override
    public String getName() {
        return config.name();
    }

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
        if (suffix.equals("js")) {
            return c.getCompressedJs();
        } else if (suffix.equals("css")) {
            return c.getCompressedCss();
        }
        return null;
    }

    protected Resource makeResource(String path) {
        return new DefaultBundleResource(this, path);
    }

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

    @Override
    public long getLastModified() {
        long mostRecent = -1;
        for (ResourceCollection rc : getResourceCollections()) {
            for (Resource r : rc.getAggregates()) {
                mostRecent = Math.max(mostRecent, r.getLastModified());
            }
        }
        return mostRecent;
    }

}
