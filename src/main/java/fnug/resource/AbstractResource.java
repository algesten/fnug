package fnug.resource;

import java.io.File;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

public abstract class AbstractResource implements Resource {

    protected static final String CONTENT_TYPE_TEXT_CSS = "text/css";
    protected static final String CONTENT_TYPE_TEXT_JAVASCRIPT = "text/javascript";
    private final static FileTypeMap TYPE_MAP = new MimetypesFileTypeMap(
            DefaultResource.class.getResourceAsStream("/mime.types"));

    private String basePath;
    private String path;
    private byte[] bytes;
    private volatile Long lastModified; // null = not read, -1 = not exist

    protected AbstractResource(String basePath, String path) {
        if (basePath == null) {
            throw new IllegalArgumentException("basePath must no be null");
        }
        if (path == null) {
            throw new IllegalArgumentException("path must no be null");
        }
        if (!basePath.endsWith(File.separator)) {
            throw new IllegalArgumentException("basePath must end with " + File.separator);
        }
        if (path.startsWith(File.separator)) {
            throw new IllegalArgumentException("path must not start with " + File.separator);
        }
        this.basePath = basePath;
        this.path = path;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getFullPath() {
        return getBasePath() + getPath();
    }

    @Override
    public String getContentType() {
        return TYPE_MAP.getContentType(path);
    }

    @Override
    public boolean isJs() {
        return getContentType().startsWith(CONTENT_TYPE_TEXT_JAVASCRIPT);
    }

    @Override
    public boolean isCss() {
        return getContentType().startsWith(CONTENT_TYPE_TEXT_CSS);
    }

    @Override
    public byte[] getBytes() {
        ensureReadEntry();
        return bytes;
    }

    protected boolean ensureReadEntry() {
        if (lastModified == null) {
            synchronized (this) {
                if (lastModified == null) {
                    doReadEntry();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public long getLastModified() {
        ensureReadEntry();
        return lastModified;
    }

    @Override
    public boolean checkModified() {
        // null means it's not read.
        if (lastModified == null) {
            return ensureReadEntry();
        }
        // -1 indicates non-existant.
        Long l = lastModified == -1l ? null : readLastModified();
        // 1 sec tolerance for windoze
        if (l == null || Math.abs(l - lastModified) > 1000) {
            synchronized (this) {
                lastModified = null;
                bytes = null;
            }
            return true;
        }
        return false;
    }

    private void doReadEntry() {
        synchronized (this) {
            if (lastModified == null) {
                Entry e = readEntry();
                bytes = e.bytes;
                lastModified = e.lastModified;
            }
        }
    }

    protected abstract Entry readEntry();

    protected abstract long readLastModified();

    protected class Entry {
        long lastModified;
        byte[] bytes;

        public Entry(long lastModified, byte[] bytes) {
            this.lastModified = lastModified;
            this.bytes = bytes;
        }

    }

    @Override
    public String toString() {
        return getPath();
    }

}
