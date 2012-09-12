package fnug.config;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertNotNull(bcfg.jsLintArgs());
        Assert.assertEquals(149, bcfg.jsLintArgs().length());
        Assert.assertEquals("[white: true, onevar: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, " +
                "bitwise: true, regexp: true, newcap: true, immed: true, maxlen: 80]", Arrays.asList(bcfg.jsLintArgs())
                .toString());
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
        	Assert.assertEquals("Failed to parse", e.getMessage());
        	Assert.assertEquals("Invalid JSON or JavaScript configuration 'testconfig2-empty.js'", e.getCause().getMessage());
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
            Assert.assertEquals("fnug.config.JsonConfigParseException", e.getClass().getName());
            Assert.assertEquals("At line 10 col 2: Key 'checkModified' is not an integer value",
                    e.getMessage());
        }

    }

    @Test
    public void testNoFiles() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig5-nofiles.js");

        JsonConfigParser parser = new JsonConfigParser();

        parser.parse(res);

    }

    @Test
    public void testBasePath() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig7-basePath.js");

        JsonConfigParser parser = new JsonConfigParser();

        Config config = parser.parse(res);

        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getBundleConfigs());
        Assert.assertEquals(2, config.getBundleConfigs().length);
        Assert.assertEquals("/test/", config.getBundleConfigs()[0].basePath());
        Assert.assertEquals("/bundle2/deep/", config.getBundleConfigs()[1].basePath());

    }

    @Test
    public void testBasePathNoTrailing() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig8-basePathNoTrailing.js");

        JsonConfigParser parser = new JsonConfigParser();

        try {
            parser.parse(res);
            Assert.fail();
        } catch (JsonConfigParseException jcpe) {
            Assert.assertEquals("At line 5 col 2: 'basePath' must end with slash: /test",
                    jcpe.getMessage());
        }

    }

    @Test
    public void testBaseNotExisting() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig9-basePathNotExist.js");

        JsonConfigParser parser = new JsonConfigParser();

        try {
            parser.parse(res);
            Assert.fail();
        } catch (JsonConfigParseException jcpe) {
            Assert.assertEquals("At line 5 col 2: No directory found for 'basePath': /does/not/exist/",
                    jcpe.getMessage());
        }

    }
    
    @Test
    public void testJavaScriptVar() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig10-javascript-var.js");

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
        Assert.assertNotNull(bcfg.jsLintArgs());
        Assert.assertEquals(149, bcfg.jsLintArgs().length());
        Assert.assertEquals("[white: true, onevar: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, " +
                "bitwise: true, regexp: true, newcap: true, immed: true, maxlen: 80]", Arrays.asList(bcfg.jsLintArgs())
                .toString());
        Assert.assertNotNull(bcfg.files());
        Assert.assertEquals(2, bcfg.files().length);
        Assert.assertEquals("test/file1.js", bcfg.files()[0].toString());
        Assert.assertEquals("test/file2.js", bcfg.files()[1].toString());
    }
    
    @Test
    public void testJavaScriptFunction() throws Exception {

        DefaultResource res = new DefaultResource("/", "testconfig11-javascript-function.js");

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
        Assert.assertNotNull(bcfg.jsLintArgs());
        Assert.assertEquals(149, bcfg.jsLintArgs().length());
        Assert.assertEquals("[white: true, onevar: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, " +
                "bitwise: true, regexp: true, newcap: true, immed: true, maxlen: 80]", Arrays.asList(bcfg.jsLintArgs())
                .toString());
        Assert.assertNotNull(bcfg.files());
        Assert.assertEquals(2, bcfg.files().length);
        Assert.assertEquals("test/file1.js", bcfg.files()[0].toString());
        Assert.assertEquals("test/file2.js", bcfg.files()[1].toString());
    }    

}
