package fnug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fnug.util.IOUtils;

public class DefaultResource extends AbstractResource {

    private static final String CONTENT_TYPE_TEXT = "text/";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResource.class);

    private static final byte[] EMPTY_BYTES = new byte[] {};
    private File jarFile;
    private File file;

    private static final File TMP_EXTRACT_DIR;

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

    public DefaultResource(String basePath, String path) {
        super(basePath, path);
    }

    @Override
    protected Entry readEntry() {
        URL url = getResourceURL();
        if (url == null) {
            return new Entry(-1l, EMPTY_BYTES);
        } else {
            File f = getFileForUrl(url);
            return readFileEntry(f);
        }
    }

    protected URL getResourceURL() {
        return getClass().getResource(getFullPath());
    }

    @Override
    protected long readLastModified() {
        assert file != null : "Call to readLastModified() before readEntry()";
        if (jarFile != null) {
            checkJarFile();
        }
        return file.lastModified();
    }

    private void checkJarFile() {
        try {
            extractJarFile(getResourceURL());
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
            return new File(s.substring(5));
        } else if (s.startsWith("jar:file:")) {
            try {
                return extractJarFile(url);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to extract jar file", e);
            }
        }
        throw new IllegalArgumentException("Unable to extract file from: " + s);
    }

    private File extractJarFile(URL url) throws IOException {

        String s = url.toExternalForm();

        String jarPath = s.substring(9, s.indexOf("!"));
        String filePath = s.substring(s.indexOf("!") + 1);

        if (filePath.startsWith(File.separator)) {
            filePath = filePath.substring(1);
        }

        File extractDir = getExtractDir(url);
        File extractFile = new File(extractDir, filePath);

        // 1000 ms tolerance for windoze
        if (jarFile == null || Math.abs(extractDir.lastModified() - jarFile.lastModified()) > 1000
                || !extractFile.exists()) {

            jarFile = new File(jarPath);
            if (!jarFile.canRead()) {
                throw new IllegalStateException("Unable to read jar file at: " + jarFile.getAbsolutePath());
            }

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

    private final static Pattern REQUIRES_PAT = Pattern.compile("\\s*[*]\\s*@requires\\s+\\([^ \\t\\n\\x0B\\f\\r]+\\)");

    @Override
    public List<String> findRequiresTags() {
        LinkedList<String> result = new LinkedList<String>();
        if (isText()) {
            try {
                String s = new String(getBytes(), "utf-8");
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

    private boolean isText() {
        return getContentType().startsWith(CONTENT_TYPE_TEXT);
    }

}
