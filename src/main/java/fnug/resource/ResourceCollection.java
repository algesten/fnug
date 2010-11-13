package fnug.resource;

import java.util.List;

import fnug.config.BundleConfig;

/**
 * Collection of resources that are associated with a {@link Bundle}. Returned
 * by {@link Bundle#getResourceCollections()}. The resource is an aggregation of
 * all files found in {@link BundleConfig#files()} and all dependencies
 * discovered looking for <code>@ requires</code> tags. The collection is
 * specifically an aggregation only of the discovered javascript and css.
 * 
 * @author Martin Algesten
 * 
 */
public interface ResourceCollection extends AggregatedResource, HasBundle {

    /**
     * Returns the dependent javascript files, concatenated in dependency order.
     * This is the same as calling {@link #getBytes()} on the resource.
     * 
     * @return the uncompressed bytes of all the javascript resources.
     */
    byte[] getJs();

    /**
     * Returns the dependent css files, concatenated in dependency order.
     * 
     * @return the uncompressed bytes of all the css resources.
     */
    byte[] getCss();

    /**
     * Returns a resource that is the compressed version of the {@link #getJs()}
     * .
     * 
     * @return compressed resource.
     */
    Resource getCompressedJs();

    /**
     * Returns a resource that is the compressed version of the
     * {@link #getCss()}.
     * 
     * @return compressed resource.
     */
    Resource getCompressedCss();

    /**
     * Returns the discovered dependent javascript files that actually exists
     * (doesn't return -1 for {@link Resource#getLastModified()}).
     * 
     * @return existing dependent javascript files.
     */
    List<Resource> getExistingJsAggregates();

    /**
     * Returns the discovered dependent css files that actually exists (doesn't
     * return -1 for {@link Resource#getLastModified()}).
     * 
     * @return existing dependent css files.
     */
    List<Resource> getExistingCssAggregates();

}
