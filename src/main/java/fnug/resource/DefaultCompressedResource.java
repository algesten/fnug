package fnug.resource;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Special resource that only postpones compression of bytes to the call of
 * {@link #getBytes()}. Returned by {@link ResourceCollection#getCompressedJs()}
 * and {@link ResourceCollection#getCompressedCss()}. When a resource change,
 * this instance is dropped instead of reused since the change may make the
 * aggregation be comprised of other resources.
 * 
 * @author Martin Algesten
 * 
 */
public class DefaultCompressedResource extends AbstractResource implements HasBundle {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultCompressedResource.class);

    private Bundle bundle;
    private byte[] bytes;
    private byte[] compressedBytes;
    private long lastModified;
    private Compressor compressor;

    /**
     * Constructs setting all necessary fields.
     * 
     * @param bundle
     *            The associated bundle.
     * @param basePath
     *            Base path of the compressed resource. This is not always the
     *            same as the associated bundle's since a resource collection
     *            may be for another bundle.
     * @param path
     *            Path of the compressed resource. This is set to an md5 sum of
     *            all the aggregated paths and modified dates.
     * @param bytes
     *            the bytes to compress.
     * @param lastModified
     *            The last modified date of the bytes.
     * @param compressor
     *            The compressor to use when compressing.
     */
    public DefaultCompressedResource(Bundle bundle, String basePath, String path, byte[] bytes, long lastModified,
            Compressor compressor) {
        super(basePath, path);
        this.bundle = bundle;
        this.bytes = bytes;
        this.lastModified = lastModified;
        this.compressor = compressor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Returns {@link #readLastModified()} and the bytes given in the
     * constructor.
     */
    @Override
    protected Entry readEntry() {
        return new Entry(readLastModified(), bytes);
    }

    /**
     * Retrieves the bytes via the superclass
     * {@link AbstractResource#getBytes()} (which ultimately leads to
     * {@link #readEntry()}). After that the compressed bytes are reused, never
     * recompressed since the whole instance is dropped if any underlying
     * resource changes.
     */
    @Override
    public byte[] getBytes() {
        // delay compression until getBytes().
        if (compressedBytes == null) {
            synchronized (this) {
                if (compressedBytes == null) {
                    byte[] superBytes = super.getBytes();
                    LOG.info("Compiling " + compressor.name() + " of bundle '" + getBundle().getName()
                            + "' for basePath: " + getBasePath());
                    compressedBytes = compressor.compress(superBytes);
                }
            }
        }
        return compressedBytes;
    }

    /**
     * Returns the last modified passed into constructor.
     */
    @Override
    protected long readLastModified() {
        return lastModified;
    }

    /**
     * Always throws {@link UnsupportedOperationException}.
     */
    @Override
    public List<String> findRequiresTags() {
        throw new UnsupportedOperationException("Can't find @requires in byte resource");
    }

}
