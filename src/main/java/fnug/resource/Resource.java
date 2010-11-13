package fnug.resource;

import java.util.List;

import javax.activation.FileTypeMap;

import fnug.config.BundleConfig;

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
 * Abstraction of a file with a name, bytes and last modified date. Resources
 * can either be a direct representation of a file or be produced by name other
 * resources. Resource instances exist also for non-existant files, in which
 * case {@link #getBytes()} returns an empty array, and
 * {@link #getLastModified()} returns -1.
 * 
 * <p>
 * Resources are cached in memory.
 * 
 * @author Martin Algesten
 * 
 */
public interface Resource extends HasLastModifiedBytes {

    /**
     * Base path is the fist half of a {@link #getFullPath()}. It is tightly
     * connected to the concept of a {@link Bundle}, where the bundle is
     * "rooted" at a base path and any resource is resolved from this directory.
     * 
     * <p>
     * A base path <em>always</em> ends with a /.
     * 
     * <p>
     * For example, a bundle may be {@link BundleConfig#basePath()} to be rooted
     * at <code>/my/bundle/</code>. In this case we get:<br>
     * <br>
     * <table border="1px" cellpadding="5x">
     * <tr>
     * <td colspan="2"><code>/my/bundle/path/to/resource.js</code></td>
     * </tr>
     * <tr>
     * <td>{@link #getBasePath()}</td>
     * <td><code>/my/bundle/</code></td>
     * </tr>
     * <tr>
     * <td>{@link #getPath()}</td>
     * <td><code>path/to/resource.js</code></td>
     * </tr>
     * <tr>
     * <td>{@link #getFullPath()}</td>
     * <td><code>/my/bundle/path/to/resource.js</code></td>
     * </tr>
     * </table>
     * 
     * @return Base path part of the resource full path.
     */
    String getBasePath();

    /**
     * The second part of a full path, that is the part after the base path.
     * 
     * <p>
     * A path <em>never</em> starts with a /.
     * 
     * <p>
     * See {@link #getBasePath()} for full example.
     * 
     * @return Path part of a resource full path.
     */
    String getPath();

    /**
     * The full path, which is simply two strings concatenated as
     * {@link #getBasePath()} + {@link #getPath()}.
     * 
     * @return Full path of the resource.
     */
    String getFullPath();

    /**
     * The mime type of the resource such as <code>text/javascript</code> or
     * <code>text/css</code>. The implementation uses a {@link FileTypeMap} to
     * map the file suffix to content type. Notice that this content type never
     * includes the encoding (which is assumed to be UTF-8).
     * 
     * @return the content type.
     */
    String getContentType();

    /**
     * Shortcut to determine if this resource is javascript. Typically checked
     * by looking at the {@link #getContentType()}.
     * 
     * @return true if this resource is javascript.
     */
    boolean isJs();

    /**
     * Shortcut to determine if this resource is css. Typically checked by
     * looking at the {@link #getContentType()}.
     * 
     * @return true if this resource is css.
     */
    boolean isCss();

    /**
     * The bytes that is this resource data. Notice that resource data is held
     * in memory. This method only triggers an actual file system read if this
     * is the first time the resource is being accessed. Likewise, aggregated
     * resources are only built on first access, and then held. To trigger
     * rebuilding call {@link #checkModified()}.
     * <p>
     * Notice that for resources pointing to non-existant files
     * {@link #getBytes()} return an empty array, never null (but
     * {@link #getLastModified()} returns -1).
     */
    byte[] getBytes();

    /**
     * Tells this resource last modified date. This only reads the file system
     * on the first access, subsequent calls returns the in memory held data. A
     * reread from disk can only be triggered using {@link #checkModified()}.
     * <p>
     * For non-existing resource, {@link #getLastModified()} returns -1.
     */
    long getLastModified();

    /**
     * Compares this resource in memory held last modified date with the one on
     * disk (or for aggregated resource, all the aggregates are checked). If the
     * last modified on disk is found to be newer than the one in memory, the in
     * memory data is dropped and reread on next {@link #getBytes()} or
     * {@link #getLastModified()}.
     * 
     * @return true if the file system resource (or aggregates in case of
     *         aggregated resources) has a newer last modified date which caused
     *         the in memory held data to be dropped.
     */
    boolean checkModified();

    /**
     * Scans the resource for the sequence "<code>* @requires</code>" which
     * makes this resource depend on others. This will only happen if the
     * resource is found to be text, that is {@link #getContentType()} must be
     * <code>text/xxx</code>, such as <code>text/javascript</code>.
     * 
     * <ul>
     * <li>Notice that the sequence <em>must</em> contain an asterisk * before
     * the @.
     * <li>It's possible to mix resource types. Javascript files can include CSS
     * files. CSS files can include Javascript. Any text file can be used in the
     * dependency chain.
     * </ul>
     * <p>
     * Example: <code><pre>
     * &#47;**
     *  * @requires some/other/javascript.js
     *  * @requires some/other/styles.css
     *  *&#47;</pre></code>
     * 
     * @return The list of parsed resources found in the file.
     */
    List<String> findRequiresTags();

}
