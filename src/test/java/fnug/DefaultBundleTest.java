package fnug;

import java.util.Arrays;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import fnug.config.BundleConfig;

public class DefaultBundleTest {

    private int makeResourceCount;

    @Before
    public void before() {
        makeResourceCount = 0;
    }

    @Test
    public void testDefaultBundle() {

        final DefaultBundle b = new DefaultBundle(makeBundleConfig("mybundle", new String[] { "test/js-resource1.js" })) {

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

        Assert.assertEquals("mybundle", b.getName());
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

        final DefaultBundle b = new DefaultBundle(makeBundleConfig("mybundle", new String[] { "test/js-resource1.js" }));

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
                "test/js-resource1.js]", Arrays.asList(coll.getAggregates()).toString());

        Assert.assertEquals("[test/js-requiredbycss.js, test/js-resource2.js, test/js-resource1.js]", coll
                .getExistingJsAggregates().toString());
        Assert.assertEquals("[test/css-resource2.css, test/css-resource1.css]", coll.getExistingCssAggregates()
                .toString());

    }

    @Test
    public void testCyclic() {

        final DefaultBundle b = new DefaultBundle(makeBundleConfig("mybundle", new String[] { "test/js-cyclic1.js" }));

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
        Assert.assertEquals("[test/js-inbundle1.js]", Arrays.asList(colls[1].getAggregates()).toString());

    }

    private BundleConfig makeBundleConfig(final String bundleName, final String[] files) {
        return new BundleConfig() {
            @Override
            public String basePath() {
                return "/";
            }

            @Override
            public boolean checkModified() {
                return true;
            }

            @Override
            public Resource configResource() {
                return null;
            }

            @Override
            public String[] files() {
                return files;
            }

            @Override
            public String name() {
                return bundleName;
            }

            @Override
            public Pattern[] matches() {
                return null;
            }

            @Override
            public boolean jsLint() {
                return true;
            }

            @Override
            public String[] jsCompileArgs() {
                return null;
            }
        };
    }

}
