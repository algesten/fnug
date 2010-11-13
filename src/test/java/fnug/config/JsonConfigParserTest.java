package fnug.config;

import org.junit.Assert;
import org.junit.Test;

import fnug.config.JsonConfigParser;
import fnug.resource.DefaultResource;

public class JsonConfigParserTest {

    @Test
    public void testNonExistantConfigFile() {

        JsonConfigParser parser = new JsonConfigParser();

        try {
            parser.parse(null);
        } catch (Exception e) {
            Assert.assertEquals("Null resource not allowed", e.getMessage());
        }

        DefaultResource res = new DefaultResource("/", "does-not-exist.js");

        try {
            parser.parse(res);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals("Resource not found: /does-not-exist.js", e.getMessage());
        }

    }

    @Test
    public void testJsonConfig() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig1-simple.js");

        JsonConfigParser parser = new JsonConfigParser();

        Config config = parser.parse(res);

        Assert.assertNotNull(config);

        BundleConfig[] bcfgs = config.getBundleConfigs();

        Assert.assertNotNull(bcfgs);
        Assert.assertEquals(1, bcfgs.length);

        BundleConfig bcfg = bcfgs[0];

        Assert.assertNotNull(bcfg);

        Assert.assertEquals("testbundle1", bcfg.name());
        Assert.assertEquals("/", bcfg.basePath());
        Assert.assertEquals(42, bcfg.checkModifiedInterval());
        Assert.assertFalse(bcfg.jsLint());
        Assert.assertNotNull(bcfg.jsCompileArgs());
        Assert.assertEquals(1, bcfg.jsCompileArgs().length);
        Assert.assertEquals("--debug", bcfg.jsCompileArgs()[0].toString());
        Assert.assertNotNull(bcfg.files());
        Assert.assertEquals(2, bcfg.files().length);
        Assert.assertEquals("test/file1.js", bcfg.files()[0].toString());
        Assert.assertEquals("test/file2.js", bcfg.files()[1].toString());

    }

    @Test
    public void testEmptyJsonConfig() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig2-empty.js");

        JsonConfigParser parser = new JsonConfigParser();

        try {
            parser.parse(res);
            Assert.fail("");
        } catch (Exception e) {
            Assert.assertEquals("At line 5 col 69: Empty config file 'testconfig2-empty.js'", e.getMessage());
        }

    }

    @Test
    public void testNoBundles() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig3-nobundles.js");

        JsonConfigParser parser = new JsonConfigParser();

        try {
            parser.parse(res);
            Assert.fail("");
        } catch (Exception e) {
            Assert.assertEquals(
                    "At line 2 col 2: Failed to read at least one bundle " +
                            "config in 'testconfig3-nobundles.js'", e.getMessage());
        }

    }

    @Test
    public void testBadCheckModified() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig4-badcheckmodified.js");

        JsonConfigParser parser = new JsonConfigParser();

        try {
            parser.parse(res);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals("At line 11 col 2: At line 11 col 2: Key 'checkModified' is not an integer value",
                    e.getMessage());
        }

    }

    @Test
    public void testNoFiles() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig5-nofiles.js");

        JsonConfigParser parser = new JsonConfigParser();

        parser.parse(res);

    }

}
