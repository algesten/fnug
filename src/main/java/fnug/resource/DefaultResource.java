package fnug.resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fnug.util.IOUtils;

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
 * Default file resource implementation. Relies on {@link Class#getResource(String)} to do its
 * magic. The class also handles the case where the class loader resolved resource is inside a
 * jar-file. That jar file is then unpacked into a temporary directory and the file last modified is
 * read from there.
 * 
 * @author Martin Algesten
 * 
 */
public class DefaultResource extends AbstractResource {

    /**
     * Default interval for checking the modified time. Set to {@value} .
     */
    public static final int DEFAULT_CHECK_MODIFIED_INTERVAL = 3000;

    private static final String CONTENT_TYPE_TEXT = "text/";
    private static final Logger LOG = LoggerFactory.getLogger(DefaultResource.class);
    private static final File TMP_EXTRACT_DIR;

    private static final byte[] EMPTY_BYTES = new byte[] {};
    private final static Pattern REQUIRES_PAT = Pattern.compile("\\s*[*]\\s*@requires\\s+([^ \\t\\n\\x0B\\f\\r]+)");

    private File jarFile;
    private File file;

    private long lastModifiedCheck;
    private int checkModifiedInterval;
    private Long cachedLastModified;

    static {
        try {
            File f;
            f = File.createTempFile("extract_", ".dir");
            f.delete();
            if (!f.mkdirs()) {
                throw new Exception("Failed to create dir: " + f.getAbsolutePath());
            }
            TMP_EXTRACT_DIR = f;
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    LOG.info("Removing extract dir: " + TMP_EXTRACT_DIR.getAbsolutePath());
                    IOUtils.rm(TMP_EXTRACT_DIR);
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create extract dir", e);
        }
    }


    /**
     * Constructs setting base path path and interval for checking.
     * 
     * @param basePath
     *            The base path of the resource. See {@link #getBasePath()}.
     * @param path
     *            The path of the resource. See {@link #getPath()}.
     * @param checkModifiedInterval
     *            the interval in millisecond that we are to check the resource last modified date.
     *            Any sooner checks will just returned the previously read value. A value of 0 will
     *            disable modified checks.
     */
    public DefaultResource(String basePath, String path, int checkModifiedInterval) {
        super(basePath, path);
        this.checkModifiedInterval = checkModifiedInterval;
    }


    /**
     * Creates a new resource setting the base path and path. Assumed
     * {@link #DEFAULT_CHECK_MODIFIED_INTERVAL} for checking modified times.
     * 
     * @param basePath
     *            The base path of the resource. See {@link #getBasePath()}.
     * @param path
     *            The path of the resource. See {@link #getPath()}.
     */
    public DefaultResource(String basePath, String path) {
        this(basePath, path, DEFAULT_CHECK_MODIFIED_INTERVAL);
    }


    /**
     * Reads the entry from the class loader using {@link Class#getResource(String)}. Also handles
     * the case where that resource is in a jar file.
     */
    @Override
    protected Entry readEntry() {
        URL url = doGetResourceURL(getFullPath());
        if (url == null) {
            return new Entry(-1l, EMPTY_BYTES);
        } else {
            File f = getFileForUrl(url);
            return readFileEntry(f);
        }
    }


    /**
     * Returns the URL of the resource.
     * 
     * @return the url of the resource or null if not found.
     */
    protected URL doGetResourceURL(String fullPath) {
        return getResourceURL(fullPath);
    }


    /**
     * Returns the URL of the resource.
     * 
     * @return the url of the resource or null if not found.
     */
    public static URL getResourceURL(String fullPath) {

        URL url = null;

        if (Thread.currentThread().getContextClassLoader() != null) {
            String clPath = fullPath;
            // an initial slash will always result in a null resource
            if (clPath.startsWith("/")) {
                clPath = clPath.substring(1);
            }
            url = Thread.currentThread().getContextClassLoader().getResource(clPath);
        }

        if (url == null) {
            url = DefaultResource.class.getResource(fullPath);
        }

        return url;
    }


    /**
     * Returns the last modified date of the file. If the file was inside a jar-file, the jar file
     * time stamp is also checked, and potentially extracted again.
     */
    @Override
    protected long readLastModified() {
        assert file != null : "Call to readLastModified() before readEntry()";
        if (!readLastModifiedAllowed()) {
            return cachedLastModified;
        }
        if (jarFile != null) {
            checkJarFile();
        }
        cachedLastModified = file.lastModified();
        lastModifiedCheck = System.currentTimeMillis();
        return cachedLastModified;
    }


    /**
     * Tells if we are allowed to check the last modified date. This looks at the check interval to
     * assert whether checking is allowed.
     * 
     * @return whether a last modified date is allowed to be checked.
     */
    protected boolean readLastModifiedAllowed() {
        return cachedLastModified == null ||
                (checkModifiedInterval > 0 &&
                        (System.currentTimeMillis() - lastModifiedCheck) > checkModifiedInterval);
    }


    private void checkJarFile() {
        try {
            extractJarFile(doGetResourceURL(getFullPath()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to check jar file", e);
        }
    }


    private File getFileForUrl(URL url) {
        if (file != null && file.isFile() && file.canRead()) {
            return file;
        }
        file = extractFile(url);
        if (file == null || !file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Can't read file: " + file.getAbsolutePath());
        }
        return file;
    }


    private Entry readFileEntry(File file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // not happening
        }
        try {
            IOUtils.spool(fis, baos);
            return new Entry(readLastModified(), baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file: " + file.getAbsolutePath(), e);
        }
    }


    private File extractFile(URL url) {

        String s = url.toExternalForm();

        if (s.startsWith("file:")) {

            s = s.substring(5);

            // decode utf-8 url encoding taking '+' into account
            s = decode(s);

            return new File(s);

        } else if (s.startsWith("jar:file:")) {
            try {
                return extractJarFile(url);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to extract jar file", e);
            }
        }

        throw new IllegalArgumentException("Unable to extract file from: " + s);
    }


    /**
     * The URLClassLoader.getResource() encodes the returned URL as UTF-8, but it will not encode
     * the '+' sign (encoding done via internal sun.net.www.ParseUtil). URLDecoder however will
     * replace any '+' with space ' '.
     * 
     * @param url
     *            url to decode
     * @return the decoded url
     */
    protected String decode(String url) {
        url = url.replace("+", "%2b");
        try {
            return URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // not happening
            throw new RuntimeException();
        }
    }


    protected File extractJarFile(URL url) throws IOException {

        String s = url.toExternalForm();

        String jarPath = s.substring(9, s.indexOf("!"));
        String filePath = s.substring(s.indexOf("!") + 1);

        // decode utf-8 url encoded paths taking '+' into account.
        jarPath = decode(jarPath);
        filePath = decode(filePath);

        if (filePath.startsWith(File.separator)) {
            filePath = filePath.substring(1);
        }

        // we only need one extract dir per jar
        URL jarURL = new URL("file:" + jarPath);
        File extractDir = getExtractDir(jarURL);

        File extractFile = new File(extractDir, filePath);

        if (jarFile == null) {
            jarFile = new File(jarPath);
            if (!jarFile.canRead()) {
                throw new IllegalStateException("Unable to read jar file at: " + jarFile.getAbsolutePath());
            }
        }

        // 1000 ms tolerance for windoze
        if (Math.abs(extractDir.lastModified() - jarFile.lastModified()) > 1000) {

            FileInputStream fis = new FileInputStream(jarFile);
            ZipInputStream zip = new ZipInputStream(fis);

            if (extractDir.exists()) {
                IOUtils.rm(extractDir);
            }
            extractDir.mkdirs();

            LOG.info("Extracting '" + jarFile.getAbsolutePath() + "' to: " + extractDir.getAbsolutePath());

            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith(File.separator)) {
                    name = name.substring(1);
                }
                File f = new File(extractDir, entry.getName());
                if (entry.isDirectory()) {
                    f.mkdirs();
                } else {
                    f.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(f);
                    IOUtils.spool(zip, fos);
                    fos.close();
                }
                f.setLastModified(entry.getTime());
            }

            extractDir.setLastModified(jarFile.lastModified());

        }

        return extractFile;

    }


    private File getExtractDir(URL url) {

        String s = url.toExternalForm();
        
        String md5sig = IOUtils.md5(s);
        return new File(TMP_EXTRACT_DIR, md5sig);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findRequiresTags() {
        LinkedList<String> result = new LinkedList<String>();
        if (isText()) {
            try {
                String s = new String(getBytesForFindRequires(), "utf-8");
                Matcher m = REQUIRES_PAT.matcher(s);
                while (m.find()) {
                    result.add(m.group(1));
                }
            } catch (UnsupportedEncodingException e) {
                // not happening.
            }
        }
        return result;
    }


    /**
     * Overridable method for getting the bytes to use for finding requires tags.
     * 
     * @return the bytes to use. Defaults to {@link #getBytes()}
     */
    protected byte[] getBytesForFindRequires() {
        return getBytes();
    }


    private boolean isText() {
        return getContentType().startsWith(CONTENT_TYPE_TEXT);
    }

}
