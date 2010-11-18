package fnug.resource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fnug.config.BundleConfig;
import fnug.util.JSLintWrapper;

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
 * Default implementation of {@link Bundle}.
 * 
 * @author Martin Algesten
 * 
 */
public class DefaultBundle implements Bundle {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultBundle.class);

    private static final String SUFFIX_CSS = "css";
    private static final String SUFFIX_JS = "js";

    /**
     * Arbitrary max size for cached resources. We want to avoid filling the
     * heap space with resources pointing to non-existing files. If we hit this
     * limit, something is probably wrong.
     */
    private static final int MAX_CACHE = 10000;


    private static final String PREFIX_BUNDLE = "bundle:";

    private BundleConfig config;

    private volatile HashMap<String, Resource> cache = new HashMap<String, Resource>();

    private volatile ResourceCollection[] resourceCollections;
    private HashMap<String, ResourceCollection> previousResourceCollections = new HashMap<String, ResourceCollection>();

    private Pattern bundlePattern;

    private volatile JSLintWrapper jsLintWrapper;

    /**
     * Constructs a bundle from the given config object.
     * 
     * @param config
     *            config to construct from.
     */
    public DefaultBundle(BundleConfig config) {
        this.config = config;
        bundlePattern = Pattern.compile(getName() + "/" + 
                Bundle.BUNDLE_ALLOWED_CHARS.pattern() + "-[a-f0-9]+\\.(js|css)");
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

            String collFile = path;
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
        ResourceCollection[] result = resourceCollections;
        if (result == null) {
            synchronized (this) {
                result = resourceCollections;
                if (result == null) {
                    resourceCollections = buildResourceCollections();
                    result = resourceCollections;
                }
            }
        }
        return result;
    }

    private ResourceCollection getResourceCollection(String collPath) {
        ResourceCollection[] colls = getResourceCollections();
        for (ResourceCollection coll : colls) {
            if (coll.getFullPath().equals(collPath)) {
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

    private ResourceCollection[] buildResourceCollections() {

        List<Resource> l = collectFilesToBuildFrom(config);

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

            ResourceCollection newColl = new DefaultResourceCollection(this, b, alr, null);

            // now we double check the newly built resource collection against
            // ones that were built previously. if we find a previous, we prefer
            // that one, since it may have already compiled javascript/css.
            if (previousResourceCollections.containsKey(newColl.getPath())) {
                newColl = previousResourceCollections.get(newColl.getPath());
            }

            result[i++] = newColl;
        }

        previousResourceCollections.clear();

        return result;

    }

    private LinkedList<Resource> collectFilesToBuildFrom(BundleConfig config) {
        LinkedList<Resource> l = new LinkedList<Resource>();

        for (String file : config.files()) {

            if (file.startsWith(PREFIX_BUNDLE)) {
                
                String bundleName = file.substring(PREFIX_BUNDLE.length()).trim();

                Bundle bundle = ResourceResolver.getInstance().getBundle(bundleName);
                
                if (bundle == null) {
                    LOG.warn("No bundle configured for name '"+bundleName+"'. Ignoring resource");
                }
                
                l.addAll(collectFilesToBuildFrom(bundle.getConfig()));
                
            } else {

                Resource r = ResourceResolver.getInstance().resolve(file);
    
                if (r == null) {
                    LOG.warn("No bundle configured to resolve '" + file + "'. Ignoring file.");
                    continue;
                }
                
                l.add(r);
            
            }

        }

        return l;
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
                if (resourceCollections != null) {
                    // save resource collections to perhaps be reused when
                    // rebuilding.
                    for (ResourceCollection rc : resourceCollections) {
                        previousResourceCollections.put(rc.getPath(), rc);
                    }
                    resourceCollections = null;
                }
            }
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSLintWrapper getJsLinter() {
        if (getConfig().jsLintArgs() == null || getConfig().jsLintArgs().length == 0) {
            return null;
        }
        JSLintWrapper result = jsLintWrapper;
        if (result == null) {
            synchronized (this) {
                result = jsLintWrapper;
                if (result == null) {
                    jsLintWrapper = new JSLintWrapper(getConfig().jsLintArgs());
                    result = jsLintWrapper;
                }
            }
        }
        return result;
    }

}
