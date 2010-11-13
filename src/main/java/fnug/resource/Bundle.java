package fnug.resource;

import java.util.regex.Pattern;

import javax.naming.spi.Resolver;

import fnug.config.BundleConfig;

/**
 * Bundles are entities responsible for resolving and building {@link Resource}.
 * A bundle can be though of as a package where several resources that logically
 * form a unit are contained. It is envisages that bundles are typically shipped
 * as jars with the bundle configuration as part of the jar.
 * 
 * @author Martin Algesten
 * 
 */
public interface Bundle {

    /**
     * Chars allowed in a bundle name [a-zA-Z0-9_]+.
     */
    public final static Pattern BUNDLE_ALLOWED_CHARS = Pattern.compile("[a-zA-Z0-9_]+");

    /**
     * Returns the config that this bundle was built from.
     * 
     * @return the config of the bundle.
     */
    BundleConfig getConfig();

    /**
     * The name of this bundle.
     * 
     * @return the name
     */
    String getName();

    /**
     * Resolves a resource under this bundle's base path (see
     * {@link BundleConfig#basePath()} {@link Resource#getBasePath()}. The
     * bundle returns the corresponding {@link Resource} regardless of whether
     * it exists or not (never return null). If the underlying file does not
     * exist, the resource returned have an empty array of bytes in
     * {@link Resource#getBytes()} and a {@link Resource#getLastModified()} of
     * -1.
     * 
     * @param path
     *            The path to resolve under this bundle. This must never start
     *            with /. See {@link Resource#getPath()}.
     * @return the resolved resource, never null.
     */
    Resource resolve(String path);

    /**
     * Each bundle configures up a number of resources (files) that comprises
     * the bundle, see {@link BundleConfig#files()}. Each of these resources may
     * in turn have dependencies on other resources, potentially from other
     * bundles. This method returns an array where each element holds resolved
     * dependent resources for a bundle, the order is important, since that's
     * the resolved order in which the resources/bundles must be loaded to
     * satisfy the dependency chains.
     * 
     * @return An ordered array of a resource collection per bundle for resolved
     *         file dependencies.
     */
    ResourceCollection[] getResourceCollections();

    /**
     * Returns the last modified date of this bundle which comprises the most
     * recent date of the {@link #getConfig()} and all the
     * {@link #getResourceCollections()} resources last modified dates.
     * 
     * @return when the bundle was last modified.
     */
    long getLastModified();

    /**
     * Checks whether this bundle is modified by calling checkModified() on the
     * {@link #getResourceCollections()}. This does not call checkModified() on
     * the {@link BundleConfig#configResource()} since that is being checked by
     * the {@link Resolver}. If any resource is found to have changed, the built
     * {@link #getResourceCollections()} are dropped and (lazily) rebuilt.
     * 
     * @return true if any resource has changed.
     */
    boolean checkModified();

}
