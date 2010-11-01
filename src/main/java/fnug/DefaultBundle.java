package fnug;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DefaultBundle extends AbstractAggregatedResource implements Bundle {

    private BundleConfig config;
    private byte[] compressedJs;
    private byte[] compressedCss;

    private JsCompressor jsCompressor;
    private CssCompressor cssCompressor;
    private byte[] cssBytes;

    public DefaultBundle(BundleConfig config, Resource[] aggregates, Resource[] dependencies) {
        super(config.basePath(), config.name(), aggregates, dependencies);
        this.jsCompressor = new JsCompressor(config.jsCompileArgs());
        this.cssCompressor = new CssCompressor();
    }

    @Override
    protected byte[] buildAggregate() {
        compressedJs = null;
        compressedCss = null;
        ByteArrayOutputStream jsBaos = new ByteArrayOutputStream();
        ByteArrayOutputStream cssBaos = new ByteArrayOutputStream();
        try {
            for (Resource res : getAggregates()) {
                if (res.isJs()) {
                    jsBaos.write(res.getBytes());
                } else if (res.isCss()) {
                    cssBaos.write(res.getBytes());
                }
            }
            this.cssBytes = cssBaos.toByteArray();
            return jsBaos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build aggregate", e);
        }
    }

    public byte[] getCssBytes() {
        // this will ultimately call buildAggregate which populates cssBytes.
        ensureReadEntry();
        assert cssBytes != null;
        return cssBytes;
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
    public Bundle[] getBundles() {
        return null;
    }

    @Override
    public byte[] getCompressedJs() {
        if (compressedJs == null) {
            synchronized (this) {
                if (compressedJs == null) {
                    compressedJs = jsCompressor.compress(getBytes());
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
                    compressedCss = cssCompressor.compress(getCssBytes());
                }
            }
        }
        return compressedCss;
    }

}
