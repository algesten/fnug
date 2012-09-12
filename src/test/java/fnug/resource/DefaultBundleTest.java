package fnug.resource;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import fnug.config.BundleConfig;
import fnug.config.DefaultBundleConfig;

public class DefaultBundleTest {

    private int makeResourceCount;

    @Before
    public void before() {
        makeResourceCount = 0;
        ResourceResolver.setThreadLocal(null);
    }

    @Test
    public void testBundleName() throws Exception {

        new DefaultBundle(makeBundleConfig("mybundle", new String[] { "test/js-resource1.js" }));
        new DefaultBundle(makeBundleConfig("my_bundle", new String[] { "test/js-resource1.js" }));
        new DefaultBundle(makeBundleConfig("myBUNDLE", new String[] { "test/js-resource1.js" }));
        new DefaultBundle(makeBundleConfig("myBUNDLE0123456789", new String[] { "test/js-resource1.js" }));
        new DefaultBundle(makeBundleConfig("___myBUNDLE0123456789", new String[] { "test/js-resource1.js" }));

        try {
            new DefaultBundle(makeBundleConfig("my bundle", new String[] { "test/js-resource1.js" }));
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // great
        }

        try {
            new DefaultBundle(makeBundleConfig("my-bundle", new String[] { "test/js-resource1.js" }));
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // great
        }

    }

    @Test
    public void testDefaultBundle() {

        final DefaultBundle b = new DefaultBundle(makeBundleConfig("test", new String[] { "test/js-resource1.js" })) {

            @Override
            protected Resource makeResource(String path) {
                makeResourceCount++;
                return super.makeResource(path);
            }
        };

        ResourceResolver.setThreadLocal(new ResourceResolver() {
            @Override
            public Resource resolve(String path) {
                return b.resolve(path);
            }
        });

        Assert.assertEquals("test", b.getName());
        Assert.assertEquals(0, makeResourceCount);

        Resource res = b.resolve("test/notthere.js");

        Assert.assertNotNull(res);

        Assert.assertEquals(-1l, res.getLastModified());
        Assert.assertNotNull(res.getBytes());
        Assert.assertEquals(0, res.getBytes().length);

        Assert.assertEquals(1, makeResourceCount);

        Assert.assertSame(res, b.resolve("test/notthere.js"));
        Assert.assertSame(res, b.resolve("test/notthere.js"));
        Assert.assertSame(res, b.resolve("test/notthere.js"));

        Assert.assertEquals(1, makeResourceCount);

    }

    @Test
    public void testResourceCollections() {

        final DefaultBundle b = new DefaultBundle(makeBundleConfig("test", new String[] { "test/js-resource1.js" }));

        ResourceResolver.setThreadLocal(new ResourceResolver() {
            @Override
            public Resource resolve(String path) {
                return b.resolve(path);
            }
        });

        ResourceCollection[] colls = b.getResourceCollections();

        Assert.assertNotNull(colls);
        Assert.assertEquals(1, colls.length);

        ResourceCollection coll = colls[0];

        Assert.assertEquals("[test/js-nonexistant.js, test/js-requiredbycss.js, " +
                "test/css-resource2.css, test/css-resource1.css, " +
                "test/css-nonexistant.css, test/js-resource2.js, " +
                "test/bbee.dusttest2.dust, test/js-resource1.js]", Arrays.asList(coll.getAggregates()).toString());

        Assert.assertEquals("[test/js-requiredbycss.js, test/js-resource2.js, test/js-resource1.js]", coll
                .getExistingJsAggregates().toString());
        Assert.assertEquals("[test/css-resource2.css, test/css-resource1.css]", coll.getExistingCssAggregates()
                .toString());

        Resource r = b.resolve(coll.getFullPath() + ".js");
        Assert.assertNotNull(r);
        Assert.assertEquals(DefaultCompressedResource.class, r.getClass());

        r = b.resolve(coll.getFullPath() + ".css");
        Assert.assertNotNull(r);
        Assert.assertEquals(DefaultCompressedResource.class, r.getClass());

    }

    @Test
    public void testCyclic() {

        final DefaultBundle b = new DefaultBundle(makeBundleConfig("test", new String[] { "test/js-cyclic1.js" }));

        ResourceResolver.setThreadLocal(new ResourceResolver() {
            @Override
            public Resource resolve(String path) {
                return b.resolve(path);
            }
        });

        try {
            b.getResourceCollections();
            Assert.fail();
        } catch (IllegalStateException e) {
            Assert.assertEquals("Found cyclic dependency: test/js-cyclic2.js -> " +
                    "test/js-cyclic1.js -> test/js-cyclic2.js", e.getMessage());
        }

    }

    @Test
    public void testMultipleBundles() {

        final DefaultBundle b1 = new DefaultBundle(makeBundleConfig("bundle1",
                new String[] { "test/js-inbundle1.js" }));
        final DefaultBundle b2 = new DefaultBundle(makeBundleConfig("bundle2",
                new String[] { "bundle2/js-inbundle2.js" }));

        ResourceResolver.setThreadLocal(new ResourceResolver() {
            @Override
            public Resource resolve(String path) {
                if (path.startsWith("test")) {
                    return b1.resolve(path);
                } else if (path.startsWith("bundle2")) {
                    return b2.resolve(path);
                }
                throw new RuntimeException();
            }
        });

        ResourceCollection[] colls = b1.getResourceCollections();

        Assert.assertNotNull(colls);
        Assert.assertEquals(2, colls.length);

        Assert.assertSame(b2, ((DefaultResourceCollection) colls[0]).getBundle());
        Assert.assertSame(b1, ((DefaultResourceCollection) colls[1]).getBundle());

        Assert.assertEquals("[bundle2/js-inbundle2.js]", Arrays.asList(colls[0].getAggregates()).toString());
        Assert.assertEquals("[test/singlequote.js, test/js-inbundle1.js]", Arrays.asList(colls[1].getAggregates())
                .toString());

    }

    @Test
    public void testMaxCache() {

        DefaultBundle b = new DefaultBundle(makeBundleConfig("test", new String[] { "test/js-resource1.js" }));

        try {
            for (int i = 0; i < 100000; i++) {
                b.resolve("some/file" + i + ".js");
            }
            Assert.fail();
        } catch (IllegalStateException ise) {
            // expected
        }

    }

    @Test
    public void testNonResolvedResource() {

        DefaultBundle b = new DefaultBundle(makeBundleConfig("test", new String[] { "test/js-resource1.js" }));

        ResourceResolver.setThreadLocal(new ResourceResolver() {
            @Override
            public Resource resolve(String path) {
                return null;
            }
        });

        ResourceCollection[] c = b.getResourceCollections();

        Assert.assertNotNull(c);
        Assert.assertEquals(0, c.length);

    }

    @Test
    public void testBundleIncludes() {

        final DefaultBundle b1 = new DefaultBundle(makeBundleConfig("bundle1",
                new String[] { "bundle1/js-inbundle1.js", "bundle1/js-inbundle1_2.js" }));

        final DefaultBundle b2 = new DefaultBundle(makeBundleConfig("bundle2",
                new String[] { "bundle2/js-inbundle2.js", "bundle1/direct.js",
                        "bundle: bundle1", "bundle2/js-inbundle2_2" }));

        ResourceResolver.setThreadLocal(new ResourceResolver() {
            @Override
            public Resource resolve(String path) {
                if (path.startsWith("bundle1")) {
                    return b1.resolve(path);
                } else if (path.startsWith("bundle2")) {
                    return b2.resolve(path);
                }
                throw new RuntimeException();
            }

            @Override
            public Bundle getBundle(String name) {
                if (name.equals("bundle1")) {
                    return b1;
                } else if (name.equals("bundle2")) {
                    return b2;
                }
                return null;
            }
        });

        ResourceCollection[] colls = b2.getResourceCollections();

        Assert.assertNotNull(colls);
        Assert.assertEquals(2, colls.length);

        Assert.assertEquals("[bundle2/js-inbundle2.js, bundle2/js-inbundle2_2]", Arrays
                .asList(colls[0].getAggregates()).toString());
        Assert.assertEquals("[bundle1/direct.js, bundle1/js-inbundle1.js, bundle1/js-inbundle1_2.js]",
                Arrays.asList(colls[1].getAggregates()).toString());

    }

    private BundleConfig makeBundleConfig(final String bundleName, final String[] files) {
        return new DefaultBundleConfig(new DefaultResource("/", "testconfig1-simple.js"), bundleName, "/",
                "", 1, files);
    }
}
