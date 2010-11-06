package fnug.resource;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import fnug.config.BundleConfig;
import fnug.config.Config;
import fnug.resource.HasBundle;
import fnug.resource.Resource;
import fnug.resource.ResourceResolver;

public class ResourceResolverTest {

    @Test
    public void testPatterns() {

        ResourceResolver rr = new ResourceResolver();
        rr.setConfigs(makeConfig("bundlecfg1", "cfg1/.*"), makeConfig("bundlecfg2", "cfg2/.*"));

        Resource r1 = rr.resolve("cfg1/test.js");
        Resource r2 = rr.resolve("cfg2/test.js");

        Assert.assertNotNull(r1);
        Assert.assertTrue(r1 instanceof HasBundle);

        Assert.assertEquals("bundlecfg1", ((HasBundle) r1).getBundle().getConfig().name());
        Assert.assertEquals("bundlecfg2", ((HasBundle) r2).getBundle().getConfig().name());

        Assert.assertNotNull(rr.getBundle("bundlecfg1"));
        Assert.assertNotNull(rr.getBundle("bundlecfg2"));
        Assert.assertNull(rr.getBundle("nosuchbundle"));

    }

    public void testResolve() {

        ResourceResolver rr = new ResourceResolver();
        rr.setConfigs(makeConfig("bundlecfg1", "cfg1/.*"), makeConfig("bundlecfg2", "cfg2/.*"));

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

        Assert.assertNull(rr.resolve("foo/bar/some.js"));
        Assert.assertNotNull(rr.resolve("cfg1/some/deep/path/some.js"));
        Assert.assertNull(rr.resolve("bundlecfg1"));
        Assert.assertNotNull(rr.resolve("bundlecfg1/file.js"));
        Assert.assertNotNull(rr.resolve("bundlecfg2/some/deep/path/file.js"));
        
    }

    private Config makeConfig(final String name, final String matches) {
        return new Config() {

            @Override
            public BundleConfig[] getBundleConfigs() {
                return new BundleConfig[] { new BundleConfig() {

                    @Override
                    public Resource configResource() {
                        return null;
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
                    public Pattern[] matches() {
                        return new Pattern[] { Pattern.compile(matches) };
                    }

                    @Override
                    public boolean jsLint() {
                        return false;
                    }

                    @Override
                    public boolean checkModified() {
                        return false;
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
