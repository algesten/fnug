package fnug.resource;

import java.util.List;

public class DefaultCompressedResource extends AbstractResource implements HasBundle {

    private Bundle bundle;
    private byte[] bytes;
    private byte[] compressedBytes;
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
        compressedBytes = null;
        return new Entry(readLastModified(), bytes);
    }

    @Override
    public byte[] getBytes() {
        // delay compression until getBytes().
        byte[] superBytes = super.getBytes();
        if (compressedBytes == null) {
            synchronized (this) {
                if (compressedBytes == null) {
                    compressedBytes = compressor.compress(superBytes);
                }
            }
        }
        return compressedBytes;
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
