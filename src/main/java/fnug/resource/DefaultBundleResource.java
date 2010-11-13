package fnug.resource;

import fnug.config.BundleConfig;

/**
 * Extension of {@link DefaultResource} that implements {@link HasBundle}.
 * 
 * @author Martin Algesten
 * 
 */
public class DefaultBundleResource extends DefaultResource implements HasBundle {

    private Bundle bundle;

    /**
     * Constructs setting the necessary bundle and path. The
     * {@link BundleConfig#basePath()} will be used as {@link #getBasePath()}.
     * 
     * @param bundle
     *            the bundle to construct from and which base path to use.
     * @param path
     *            the local path. See {@link #getPath()}.
     */
    public DefaultBundleResource(Bundle bundle, String path) {
        super(bundle.getConfig().basePath(), path);
        this.bundle = bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getBundle() {
        return bundle;
    }

}
