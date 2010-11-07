package fnug.resource;

import java.util.List;

public class DefaultCompressedResource extends AbstractResource implements HasBundle {

    private Bundle bundle;
    private byte[] bytes;
    private long lastModified;
    private Compressor compressor;

    public DefaultCompressedResource(Bundle bundle, String basePath, String path, byte[] bytes, long lastModified,
            Compressor compressor) {
        super(basePath, path);
        this.bundle = bundle;
        this.bytes = bytes;
        this.lastModified = lastModified;
        this.compressor = compressor;
    }

    @Override
    public Bundle getBundle() {
        return bundle;
    }

    @Override
    protected Entry readEntry() {
        return new Entry(readLastModified(), compressor.compress(bytes));
    }

    @Override
    protected long readLastModified() {
        return lastModified;
    }

    @Override
    public List<String> findRequiresTags() {
        throw new UnsupportedOperationException("Can't find @requires in byte resource");
    }

}
