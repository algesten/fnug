package fnug.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import fnug.util.IOUtils;

public class DefaultResourceCollection extends AbstractAggregatedResource
        implements ResourceCollection, HasBundle {

    private static final String SUPER_NAME = "__CALCULATED__";

    private static final Resource[] EMPTY_RESOURCES = new Resource[] {};

    private Bundle bundle;
    private Resource[] aggregates;
    private Resource[] dependencies;
    private JsCompressor jsCompressor;
    private CssCompressor cssCompressor;

    private byte[] css;
    private volatile Resource compressedJs;
    private volatile Resource compressedCss;

    public DefaultResourceCollection(Bundle bundle, Resource[] aggregates, Resource[] dependencies) {
        super(bundle.getConfig().basePath(), SUPER_NAME);
        this.bundle = bundle;
        this.aggregates = aggregates == null ? EMPTY_RESOURCES : aggregates;
        this.dependencies = dependencies == null ? EMPTY_RESOURCES : dependencies;
        jsCompressor = new JsCompressor(bundle.getConfig().jsCompileArgs());
        cssCompressor = new CssCompressor();
    }

    private static int hash(Resource[] aggregates) {
        int i = DefaultResourceCollection.class.getName().hashCode();
        for (Resource r : aggregates) {
            i = 31 * i + r.getPath().hashCode();
            i = 31 * i + (new Long(r.getLastModified()).hashCode());
        }
        return i;
    }

    @Override
    public String getPath() {
        return IOUtils.md5("" + hash(getAggregates()));
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
    public Resource getCompressedJs() {
        if (compressedJs == null) {
            synchronized (this) {
                if (compressedJs == null) {
                    compressedJs = new DefaultCompressedResource(getBundle(), getPath() + ".js", getJs(),
                            getLastModified(), jsCompressor);
                }
            }
        }
        return compressedJs;
    }

    @Override
    public Resource getCompressedCss() {
        if (compressedCss == null) {
            synchronized (this) {
                if (compressedCss == null) {
                    compressedCss = new DefaultCompressedResource(getBundle(), getPath() + ".css", getCss(),
                            getLastModified(), cssCompressor);
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

    @Override
    public List<Resource> getExistingJsAggregates() {
        return getExisting(CONTENT_TYPE_TEXT_JAVASCRIPT);
    }

    @Override
    public List<Resource> getExistingCssAggregates() {
        return getExisting(CONTENT_TYPE_TEXT_CSS);
    }

    private List<Resource> getExisting(String contentType) {
        LinkedList<Resource> res = new LinkedList<Resource>();
        for (Resource r : getAggregates()) {
            if (r.getContentType().equals(contentType) &&
                    r.getLastModified() > 0) {
                res.add(r);
            }
        }
        return res;
    }

}
