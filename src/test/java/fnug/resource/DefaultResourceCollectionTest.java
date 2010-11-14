package fnug.resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jslint4java.JSLint;

import fnug.config.BundleConfig;

public class DefaultResourceCollectionTest {

    protected int readLastModifiedCount;

    @Before
    public void before() {
        readLastModifiedCount = 0;
    }

    @Test
    public void testDefaultResourceCollection() throws Exception {

        Bundle bundle = makeBundle(0);

        DefaultResourceCollection c = new DefaultResourceCollection(bundle, "/" + bundle.getConfig().name() + "/",
                new Resource[] {
                        makeResource("test/js-resource2.js", false),
                        makeResource("test/js-resource1.js", false),
                        makeResource("nonexistant.js", false),
                        makeResource("test/css-resource1.css", false),
                        makeResource("test/css-resource2.css", false)
        }, null) {
            @Override
            protected long readLastModified() {
                readLastModifiedCount++;
                return super.readLastModified();
            }
        };

        String fullPath = c.getFullPath();

        Assert.assertEquals(-559262445, new String(c.getBytes()).hashCode());
        Assert.assertSame(c.getBytes(), c.getJs());

        Assert.assertEquals(4593760, new String(c.getCss()).hashCode());

        byte[] js = c.getJs();
        byte[] css = c.getCss();
        Resource compressedJs = c.getCompressedJs();
        Resource compressedCss = c.getCompressedCss();

        Assert.assertEquals("/testbundleconfig/", compressedJs.getBasePath());
        Assert.assertEquals(c.getPath() + ".js", compressedJs.getPath());
        Assert.assertEquals("var a=function(){alert(\"this is jozt a test\")},b=function(){a()},c=function(){b()};\n",
                new String(compressedJs.getBytes()));
        Assert.assertTrue(c.getBundle().getConfig().configResource().getLastModified() <= compressedJs
                .getLastModified());
        Assert.assertSame(compressedJs, c.getCompressedJs());

        Assert.assertEquals("\n" +
                "a{color:red}\n" +
                "p{margin-top:14px 14px 14px 14px}body{background:black;color:white;font-size:14em}",
                new String(compressedCss.getBytes()));
        Assert.assertEquals("/testbundleconfig/", compressedCss.getBasePath());
        Assert.assertEquals(c.getPath() + ".css", compressedCss.getPath());
        Assert.assertTrue(c.getBundle().getConfig().configResource().getLastModified() <= compressedCss
                .getLastModified());
        Assert.assertSame(compressedCss, c.getCompressedCss());

        Assert.assertSame(js, c.getJs());
        Assert.assertSame(css, c.getCss());

        Assert.assertEquals(1, readLastModifiedCount);

        Assert.assertFalse(c.checkModified());
        Assert.assertFalse(c.checkModified());
        Assert.assertFalse(c.checkModified());

        Assert.assertEquals(fullPath, c.getFullPath());

        Assert.assertEquals(4, readLastModifiedCount);

    }

    @Test
    public void testCheckModified() throws Exception {

        Bundle bundle = makeBundle(1);

        DefaultResourceCollection c = new DefaultResourceCollection(bundle, "/" + bundle + "/", new Resource[] {
                makeResource("test/js-resource2.js", true),
                makeResource("test/js-resource1.js", false),
                makeResource("nonexistant.js", false),
                makeResource("test/css-resource1.css", false),
                makeResource("test/css-resource2.css", false)
        }, null) {
            @Override
            protected long readLastModified() {
                readLastModifiedCount++;
                return super.readLastModified();
            }
        };

        String fullPath = c.getFullPath();

        byte[] js = c.getJs();
        byte[] css = c.getCss();
        Resource compressedJs = c.getCompressedJs();
        Resource compressedCss = c.getCompressedCss();

        Assert.assertEquals(1, readLastModifiedCount);

        Thread.sleep(1100);
        Assert.assertTrue(c.checkModified());
        Thread.sleep(10);
        Assert.assertTrue(c.checkModified());
        Thread.sleep(10);
        Assert.assertTrue(c.checkModified());

        Assert.assertEquals(4, readLastModifiedCount);

        Assert.assertNotSame(js, c.getJs());
        Assert.assertNotSame(css, c.getCss());
        Assert.assertNotSame(compressedJs, c.getCompressedJs());
        Assert.assertNotSame(compressedCss, c.getCompressedCss());

        Assert.assertFalse(fullPath.equals(c.getFullPath()));

    }

    private Resource makeResource(String path, final boolean forceModified) {
        return new DefaultResource("/", path, forceModified ? 1 : 0) {
            @Override
            protected long readLastModified() {
                return forceModified ? (long) (System.currentTimeMillis() + (Math.random() * 100000)) :
                        super.readLastModified();
            }
        };
    }

    private Bundle makeBundle(final int checkModifiedInterval) {
        return new Bundle() {

            @Override
            public BundleConfig getConfig() {
                return new BundleConfig() {

                    @Override
                    public String name() {
                        return "testbundleconfig";
                    }

                    @Override
                    public String[] jsLintArgs() {
                        return null;
                    }

                    @Override
                    public String[] jsCompileArgs() {
                        return null;
                    }

                    @Override
                    public String[] files() {
                        return null;
                    }

                    @Override
                    public Resource configResource() {
                        return new DefaultResource("/", "testconfig1-simple.js");
                    }

                    @Override
                    public int checkModifiedInterval() {
                        return checkModifiedInterval;
                    }

                    @Override
                    public String basePath() {
                        return "/";
                    }
                };
            }

            @Override
            public String getName() {
                return "testbundle";
            }

            @Override
            public Resource resolve(String path) {
                return null;
            }

            @Override
            public ResourceCollection[] getResourceCollections() {
                return null;
            }

            @Override
            public long getLastModified() {
                return -1;
            }

            @Override
            public boolean checkModified() {
                return checkModifiedInterval != 0;
            }

            @Override
            public JSLint getJsLinter() {
                return null;
            }

        };
    }

}
