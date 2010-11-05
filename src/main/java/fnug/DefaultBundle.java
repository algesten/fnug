package fnug;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class DefaultBundle implements Bundle {

    private BundleConfig config;

    private volatile HashMap<String, Resource> cache = new HashMap<String, Resource>();

    private volatile BundleResourceCollection[] resources;

    public DefaultBundle(BundleConfig config) {
        this.config = config;
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
        Resource res = cache.get(path);
        if (res == null) {
            synchronized (this) {
                res = cache.get(path);
                if (res == null) {
                    res = new DefaultBundleResource(this, path);
                    cache.put(path, res);
                }
            }
        }
        return res;
    }

    @Override
    public BundleResourceCollection[] getResources() {
        if (resources == null) {
            synchronized (this) {
                if (resources == null) {
                    resources = buildResources();
                }
            }
        }
        return resources;
    }

    private BundleResourceCollection[] buildResources() {

        LinkedList<Resource> l = new LinkedList<Resource>();
        for (String file : config.files()) {
            l.add(resolve(file));
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
                // remove last " -> "
                bld.delete(bld.length() - 4, bld.length());
                throw new IllegalStateException("Found cyclic dependency: " + bld.toString());
            }
            Resource r = cur.get(0);
            if (!(r instanceof BundleResource)) {
                throw new IllegalStateException("Can only resolve dependencies to BundleResource");
            }
            Bundle b = ((BundleResource) r).getBundle();
            List<Resource> lr = bundleResources.get(b);
            if (lr == null) {
                lr = new LinkedList<Resource>();
                bundleResources.put(b, lr);
            }
            lr.add(r);
        }

        BundleResourceCollection[] result = new BundleResourceCollection[bundleResources.size()];

        int i = 0;
        for (Bundle b : bundleResources.keySet()) {
            List<Resource> lr = bundleResources.get(b);
            Resource[] alr = lr.toArray(new Resource[lr.size()]);
            result[i++] = new DefaultBundleResourceCollection(b, alr, null);
        }

        return result;

    }

}
