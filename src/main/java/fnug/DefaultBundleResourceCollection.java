package fnug;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import fnug.util.IOUtils;

public class DefaultBundleResourceCollection extends AbstractAggregatedResource implements BundleResourceCollection {

    private static final Resource[] EMPTY_RESOURCES = new Resource[] {};

    private Bundle bundle;
    private Resource[] aggregates;
    private Resource[] dependencies;
    private JsCompressor jsCompressor;
    private CssCompressor cssCompressor;

    private byte[] css;
    private volatile byte[] compressedJs;
    private volatile byte[] compressedCss;

    public DefaultBundleResourceCollection(Bundle bundle, Resource[] aggregates, Resource[] dependencies) {
        super(bundle.getConfig().basePath(), IOUtils.md5("" + hash(aggregates)));
        this.bundle = bundle;
        this.aggregates = aggregates == null ? EMPTY_RESOURCES : aggregates;
        this.dependencies = dependencies == null ? EMPTY_RESOURCES : dependencies;
        jsCompressor = new JsCompressor(bundle.getConfig().jsCompileArgs());
        cssCompressor = new CssCompressor();
    }

    private static int hash(Resource[] aggregates) {
        int i = DefaultBundleResourceCollection.class.getName().hashCode();
        for (Resource r : aggregates) {
            i = 31 * i + r.getPath().hashCode();
            i = 31 * i + (new Long(r.getLastModified()).hashCode());
        }
        return i;
    }

    @Override
    public Resource[] getAggregates() {
        return aggregates;
    }

    @Override
    public Resource[] getDependencies() {
        return dependencies;
    }

    @Override
    public Bundle getBundle() {
        return bundle;
    }

    @Override
    protected byte[] buildAggregate() {
        try {
            ByteArrayOutputStream jsbaos = new ByteArrayOutputStream();
            ByteArrayOutputStream cssbaos = new ByteArrayOutputStream();
            for (Resource r : getAggregates()) {
                if (r.isJs()) {
                    jsbaos.write(r.getBytes());
                } else if (r.isCss()) {
                    cssbaos.write(r.getBytes());
                }
            }
            css = cssbaos.toByteArray();
            return jsbaos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build aggregate", e);
        }
    }

    @Override
    public byte[] getJs() {
        return getBytes();
    }

    @Override
    public byte[] getCss() {
        ensureReadEntry();
        return css;
    }

    @Override
    public byte[] getCompressedJs() {
        if (compressedJs == null) {
            synchronized (this) {
                if (compressedJs == null) {
                    compressedJs = jsCompressor.compress(getJs());
                }
            }
        }
        return compressedJs;
    }

    @Override
    public byte[] getCompressedCss() {
        if (compressedCss == null) {
            synchronized (this) {
                if (compressedCss == null) {
                    compressedCss = cssCompressor.compress(getCss());
                }
            }
        }
        return compressedCss;
    }

    @Override
    public boolean checkModified() {
        if (!bundle.getConfig().checkModified()) {
            return false;
        }
        boolean modified = super.checkModified();
        if (modified) {
            synchronized (this) {
                css = null;
                compressedJs = null;
                compressedCss = null;
            }
        }
        return modified;
    }

}
