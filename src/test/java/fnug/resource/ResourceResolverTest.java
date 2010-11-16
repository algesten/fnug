package fnug.resource;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fnug.config.BundleConfig;
import fnug.config.Config;

public class ResourceResolverTest {

    @Test
    public void testPatterns() {

        ResourceResolver rr = new ResourceResolver();
        rr.setConfigs(makeConfig("bundlecfg1"), makeConfig("bundlecfg2"));

        Resource r1 = rr.resolve("bundlecfg1/test.js");
        Resource r2 = rr.resolve("bundlecfg2/test.js");

        Assert.assertNotNull(r1);
        Assert.assertTrue(r1 instanceof HasBundle);

        Assert.assertEquals("bundlecfg1", ((HasBundle) r1).getBundle().getConfig().name());
        Assert.assertEquals("bundlecfg2", ((HasBundle) r2).getBundle().getConfig().name());

        Assert.assertNotNull(rr.getBundle("bundlecfg1"));
        Assert.assertNotNull(rr.getBundle("bundlecfg2"));
        Assert.assertNull(rr.getBundle("nosuchbundle"));

    }

    @Test
    public void testResolve() {

        ResourceResolver rr = new ResourceResolver();
        rr.setConfigs(makeConfig("bundlecfg1"), makeConfig("bundlecfg2"));

        try {
            rr.resolve(null);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // yay
        }

        try {
            rr.resolve("/foo.js");
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // yay
        }

        try {
            rr.resolve("foo/");
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // yay
        }

        try {
            rr.resolve("../foo");
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Relative path resolves outside bundle: ../foo", iae.getMessage());
        }

        try {
            rr.resolve("foo/..");
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("Relative path resolves empty: foo/..", iae.getMessage());
        }

        Assert.assertNull(rr.resolve("foo/bar/some.js"));
        Assert.assertNotNull(rr.resolve("bundlecfg1/some/deep/path/some.js"));
        Assert.assertNotNull(rr.resolve("bundlecfg1/some/../path/../with/../relatives.js"));
        Assert.assertNull(rr.resolve("bundlecfg1"));
        Assert.assertNotNull(rr.resolve("bundlecfg1/file.js"));
        Assert.assertNotNull(rr.resolve("bundlecfg2/some/deep/path/file.js"));

    }

    @Test
    public void testDuplicateBundles() {

        ResourceResolver rr = new ResourceResolver();

        try {
            rr.setConfigs(makeConfig("dupe"), makeConfig("dupe"));
            Assert.fail();
        } catch (IllegalStateException ise) {
            Assert.assertEquals("Duplicate definitions of bundle name 'dupe' in '0/path/to/dupe' and '1/path/to/dupe'",
                    ise.getMessage());
        }

    }

    @Test
    public void testReservedWordBundles() {

        ResourceResolver rr = new ResourceResolver();

        try {
            rr.setConfigs(makeConfig("ALL"));
            Assert.fail();
        } catch (IllegalStateException ise) {
            Assert.assertEquals("Bundle name 'ALL' is a reserved word.", ise.getMessage());
        }

        try {
            rr.setConfigs(makeConfig("1"));
            Assert.fail();
        } catch (IllegalStateException ise) {
            Assert.assertEquals("Bundle name '1' is a reserved word.", ise.getMessage());
        }

        try {
            rr.setConfigs(makeConfig("tRuE"));
            Assert.fail();
        } catch (IllegalStateException ise) {
            Assert.assertEquals("Bundle name 'tRuE' is a reserved word.", ise.getMessage());
        }

    }

    private int i = 0;

    private Config makeConfig(final String name) {
        return new Config() {

            @Override
            public BundleConfig[] getBundleConfigs() {
                return new BundleConfig[] { new BundleConfig() {

                    @Override
                    public Resource configResource() {
                        return new Resource() {

                            @Override
                            public String getBasePath() {
                                return null;
                            }

                            @Override
                            public String getPath() {
                                return null;
                            }

                            @Override
                            public String getFullPath() {
                                return (i++) + "/path/to/" + name;
                            }

                            @Override
                            public String getContentType() {
                                return null;
                            }

                            @Override
                            public boolean isJs() {
                                return false;
                            }

                            @Override
                            public boolean isCss() {
                                return false;
                            }

                            @Override
                            public byte[] getBytes() {
                                return null;
                            }

                            @Override
                            public long getLastModified() {
                                return 0;
                            }

                            @Override
                            public boolean checkModified() {
                                return false;
                            }

                            @Override
                            public List<String> findRequiresTags() {
                                return null;
                            }

                        };
                    }

                    @Override
                    public String name() {
                        return name;
                    }

                    @Override
                    public String basePath() {
                        return "/";
                    }

                    @Override
                    public String[] jsLintArgs() {
                        return null;
                    }

                    @Override
                    public int checkModifiedInterval() {
                        return 0;
                    }

                    @Override
                    public String[] jsCompileArgs() {
                        return null;
                    }

                    @Override
                    public String[] files() {
                        return null;
                    }
                } };
            }

        };
    }
}
