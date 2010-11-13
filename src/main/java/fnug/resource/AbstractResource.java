package fnug.resource;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

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
 * Abstract supertype for {@link Resource} implementations. Reads a classpath
 * resource /fnug/mime.types to get a {@link FileTypeMap} for
 * {@link #getContentType()}.
 * 
 * @author Martin Algesten
 * 
 */
public abstract class AbstractResource implements Resource {

    /**
     * Content type <code>text/css</code>.
     */
    public static final String CONTENT_TYPE_TEXT_CSS = "text/css";

    /**
     * Content type <code>text/javascript</code>.
     */
    public static final String CONTENT_TYPE_TEXT_JAVASCRIPT = "text/javascript";

    private final static FileTypeMap TYPE_MAP = new MimetypesFileTypeMap(
            DefaultResource.class.getResourceAsStream("/fnug/mime.types"));

    private String basePath;
    private String path;
    private byte[] bytes;
    private volatile Long lastModified; // null = not read, -1 = not exist

    /**
     * Constructor setting necessary fields.
     * 
     * @param basePath
     *            The base path of the resource, see {@link #getBasePath()}.
     * @param path
     *            The path of the resource, see {@link #getPath()}.
     */
    protected AbstractResource(String basePath, String path) {
        if (basePath == null) {
            throw new IllegalArgumentException("basePath must no be null");
        }
        if (path == null) {
            throw new IllegalArgumentException("path must no be null");
        }
        if (!basePath.endsWith("/")) {
            throw new IllegalArgumentException("basePath must end with " + "/");
        }
        if (path.startsWith("/")) {
            throw new IllegalArgumentException("path must not start with " + "/");
        }
        this.basePath = basePath;
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBasePath() {
        return basePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullPath() {
        return getBasePath() + getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return TYPE_MAP.getContentType(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isJs() {
        return getContentType().startsWith(CONTENT_TYPE_TEXT_JAVASCRIPT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCss() {
        return getContentType().startsWith(CONTENT_TYPE_TEXT_CSS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getBytes() {
        ensureReadEntry();
        return bytes;
    }

    /**
     * Ensures the bytes and lastmodified is read from the underlying resource
     * (such as a file). This will in turn call {@link #readEntry()} which needs
     * to be implemented to do the actual reading.
     * 
     * @return true if the entry was read.
     */
    protected final boolean ensureReadEntry() {
        if (lastModified == null) {
            synchronized (this) {
                if (lastModified == null) {
                    doReadEntry();
                }
            }
            return true;
        } else {
            assert bytes != null : "Bytes null when lastModified isn't";
        }
        return false;
    }

    private void doReadEntry() {
        synchronized (this) {
            if (lastModified == null) {
                Entry e = readEntry();
                if (e.bytes == null) {
                    throw new IllegalStateException("Null bytes not allowed: " + getFullPath());
                }
                if (e.lastModified == 0) {
                    throw new IllegalStateException("0 lastModified not allowed " + getFullPath());
                }
                bytes = e.bytes;
                lastModified = e.lastModified;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastModified() {
        ensureReadEntry();
        return lastModified;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Reads the actual entry providing the bytes and lastModified date back to
     * the caller. This must be implemented in subclasses. It must just read the
     * actual data without checking whether bytes or lastModified has been read
     * already.
     * 
     * @return the bytes and last modified date.
     */
    protected abstract Entry readEntry();

    /**
     * Reads just the last modified date from the actual data source. This is
     * used by {@link #checkModified()} to compare the stored last modified with
     * the one on disk.
     * 
     * @return the last modified date of the underlying data source, such as a
     *         file.
     */
    protected abstract long readLastModified();

    /**
     * Wrapper class for returning data from
     * {@link AbstractResource#readEntry()}.
     * 
     * @author Martin Algesten
     * 
     */
    protected class Entry {
        long lastModified;
        byte[] bytes;

        public Entry(long lastModified, byte[] bytes) {
            this.lastModified = lastModified;
            this.bytes = bytes;
        }

    }

    /**
     * Returns {@link #getPath}.
     */
    @Override
    public String toString() {
        return getPath();
    }

}
