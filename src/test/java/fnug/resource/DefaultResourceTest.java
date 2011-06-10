package fnug.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fnug.util.IOUtils;

public class DefaultResourceTest {

    @Test
    public void testPaths() {
        try {
            new DefaultResource("", "foo.js");
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // ok
        }
        try {
            new DefaultResource("/", "/foo.js");
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // ok
        }
        new DefaultResource("/", "foo.js");
    }


    @Test
    public void testUTF8Path() throws UnsupportedEncodingException {

        DefaultResource r = new DefaultResource("/", "ignored");

        URL url = r.doGetResourceURL("/utf8-resource-å+ Ä=ö%[Ż.js");

        Assert.assertNotNull(url);

        String s = url.toExternalForm();

        // ensure that url.toExternalForm() actually encodes using utf-8.
        Assert.assertTrue(s.endsWith("/utf8-resource-%c3%a5+%20%c3%84%3d%c3%b6%25%5b%c5%bb.js"));

        s = r.decode(s);

        Assert.assertTrue(s, s.endsWith("/utf8-resource-å+ Ä=ö%[Ż.js"));

    }


    @Test
    public void testGetters() {

        DefaultResource r = new DefaultResource("/some/base/", "foo.js");

        Assert.assertEquals("foo.js", r.getPath());
        Assert.assertEquals("/some/base/foo.js", r.getFullPath());

    }


    @Test
    public void testContentType() {

        Assert.assertEquals("text/javascript", (new DefaultResource("/", "foo.js")).getContentType());
        Assert.assertEquals("text/css", (new DefaultResource("/", "foo.css")).getContentType());
        Assert.assertEquals("image/gif", (new DefaultResource("/", "foo.gif")).getContentType());
        Assert.assertEquals("application/octet-stream", (new DefaultResource("/", "foo.qwerty")).getContentType());

        Assert.assertTrue((new DefaultResource("/", "foo.js")).isJs());
        Assert.assertFalse((new DefaultResource("/", "foo.css")).isJs());
        Assert.assertTrue((new DefaultResource("/", "foo.css")).isCss());
        Assert.assertFalse((new DefaultResource("/", "foo.js")).isCss());

    }


    @Test
    public void testGetBytes() {

        DefaultResource r = new DefaultResource("/some/base/", "nonexistant.js");

        byte[] b = r.getBytes();

        Assert.assertNotNull(b);
        Assert.assertEquals(0, b.length);

        r = new DefaultResource("/", "notinjar.js");

        b = r.getBytes();

        Assert.assertNotNull(b);
        Assert.assertEquals(21, b.length);

        String s = new String(b);

        Assert.assertEquals("Not hidden in jar...\n", s);

        r = makeJarResource("dir/injar.js", 0);

        b = r.getBytes();

        Assert.assertNotNull(b);
        Assert.assertEquals(23, b.length);

        s = new String(b);

        Assert.assertEquals("Hidden inside a jar...\n", s);

    }


    @Test
    public void testRemoveExtractedFile() throws Exception {

        File tmp = makeTmpJar();

        DefaultResource r = makeJarResource(tmp.getAbsolutePath(), "dir/injar.js", 1);

        // this triggers file extraction.
        r.getBytes();

        Field field = DefaultResource.class.getDeclaredField("file");
        field.setAccessible(true);

        Method m = DefaultResource.class.getDeclaredMethod("getExtractDir", URL.class);
        m.setAccessible(true);

        URL resourceUrl = r.doGetResourceURL(r.getFullPath());
        URL jarUrl = new URL(resourceUrl.toExternalForm().substring(4, resourceUrl.toExternalForm().indexOf("!")));
        
        File extractDir = (File) m.invoke(r, jarUrl);

        File f = (File) field.get(r);
        Assert.assertTrue(f.exists());

        Thread.sleep(10);
        r.checkModified();

        IOUtils.rm(extractDir);
        Assert.assertFalse(extractDir.exists());

        // should do nothing.
        Assert.assertNotNull(r.getBytes());

        f = (File) field.get(r);
        Assert.assertFalse(f.exists());

        // this should trigger another extract...
        Thread.sleep(10);
        r.checkModified();

        f = (File) field.get(r);

        Assert.assertTrue(f.exists());

    }


    @Test
    public void testUpdateJarFileTimestamp() throws Exception {

        File tmp = makeTmpJar();

        DefaultResource r = makeJarResource(tmp.getAbsolutePath(), "dir/injar.js", 1);

        // trigger extraction.
        r.getLastModified();

        Method m = DefaultResource.class.getDeclaredMethod("getExtractDir", URL.class);
        m.setAccessible(true);

        URL resourceUrl = r.doGetResourceURL(r.getFullPath());
        URL jarUrl = new URL(resourceUrl.toExternalForm().substring(4, resourceUrl.toExternalForm().indexOf("!")));
        
        File extractDir = (File) m.invoke(r, jarUrl);

        Assert.assertTrue(Math.abs(tmp.lastModified() - extractDir.lastModified()) < 1000);

        tmp.setLastModified(tmp.lastModified() + 2000);

        Assert.assertTrue(Math.abs(tmp.lastModified() - extractDir.lastModified()) > 1000);

        // should trigger second extraction
        Thread.sleep(10);
        r.checkModified();

        Assert.assertTrue(Math.abs(tmp.lastModified() - extractDir.lastModified()) < 1000);

    }


    @Test
    public void testFindRequiresTags() throws Exception {

        DefaultResource r = new DefaultResource("/", "nosuchresource.js");

        List<String> reqs = r.findRequiresTags();

        Assert.assertNotNull(reqs);
        Assert.assertEquals(0, reqs.size());

        r = new DefaultResource("/", "test/js-resource1.js");

        reqs = r.findRequiresTags();

        Assert.assertNotNull(reqs);
        Assert.assertEquals(4, reqs.size());

        Assert.assertEquals("[test/js-resource2.js, test/js-nonexistant.js, " +
                        "test/css-resource1.css, test/css-nonexistant.css]",
                reqs.toString());

    }


    @Test
    public void testCheckModifiedInterval() throws Exception {

        DefaultResource r = new DefaultResource("/", "notinjar.js", 0) {

            @Override
            protected long readLastModified() {
                return System.currentTimeMillis();
            }
        };

        long l = r.getLastModified();
        Assert.assertTrue(l > 0);

        Thread.sleep(10);

        Assert.assertEquals(l, r.getLastModified());
        r.checkModified();

        Assert.assertEquals(l, r.getLastModified());

        r = new DefaultResource("/", "notinjar.js", 500) {

            @Override
            protected long readLastModified() {
                return System.currentTimeMillis();
            }
        };

        l = r.getLastModified();
        Assert.assertTrue(l > 0);

        Thread.sleep(1100);

        Assert.assertEquals(l, r.getLastModified());
        r.checkModified();

        Assert.assertTrue(r.getLastModified() > l);

    }


    private File makeTmpJar() throws IOException {

        File tmp = File.createTempFile("tmp", ".jar");
        FileOutputStream fos = new FileOutputStream(tmp);

        InputStream is = getClass().getResourceAsStream("/test.jar");

        IOUtils.spool(is, fos);
        is.close();
        fos.close();
        return tmp;

    }


    private DefaultResource makeJarResource(String file, int checkModifiedInterval) {
        URL url = getClass().getResource("/test.jar");
        String jarPath = url.toExternalForm().substring(5);
        return makeJarResource(jarPath, file, checkModifiedInterval);
    }


    private DefaultResource makeJarResource(String jarPath, String file, int checkModifiedInterval) {
        return new DefaultResource("jar:file:" + jarPath + "!/", file, checkModifiedInterval) {

            @Override
            protected URL doGetResourceURL(String fullPath) {
                try {
                    return new URL(fullPath);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            };
        };
    }

}
