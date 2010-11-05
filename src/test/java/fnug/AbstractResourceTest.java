package fnug;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class AbstractResourceTest {

    @Test
    public void testConstructor() {
        try {
            new TestResource(null, "foo.js");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // yes
        }
        try {
            new TestResource("/", null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // yes
        }
        try {
            new TestResource("", null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // yes
        }
        try {
            new TestResource("/", "/foo.js");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // yes
        }
        new TestResource("/", "foo.js");
    }

    @Test
    public void testReads() {

        TestResource res = new TestResource("/", "foo.js", "somedata", 123l);

        Assert.assertEquals(0, res.readLastModifiedCount);
        Assert.assertEquals(0, res.readEntryCount);

        res.getContentType();
        res.getFullPath();
        res.getPath();
        res.isCss();
        res.isJs();

        Assert.assertEquals(0, res.readLastModifiedCount);
        Assert.assertEquals(0, res.readEntryCount);

        // trigger read.
        byte[] b = res.getBytes();

        Assert.assertNotNull(b);
        Assert.assertTrue(Arrays.equals(b, "somedata".getBytes()));
        Assert.assertEquals(0, res.readLastModifiedCount);
        Assert.assertEquals(1, res.readEntryCount);

        res.getBytes();
        res.getBytes();
        res.getBytes();

        Assert.assertEquals(0, res.readLastModifiedCount);
        Assert.assertEquals(1, res.readEntryCount);

        Assert.assertFalse(res.checkModified());
        Assert.assertFalse(res.checkModified());
        Assert.assertFalse(res.checkModified());

        Assert.assertEquals(3, res.readLastModifiedCount);
        Assert.assertEquals(1, res.readEntryCount);

        res.lastModified = 234l;

        Assert.assertFalse(res.checkModified()); // < 1000

        Assert.assertEquals(4, res.readLastModifiedCount);
        Assert.assertEquals(1, res.readEntryCount);

        res.lastModified = 1234l;

        Assert.assertTrue(res.checkModified()); // > 1000

        Assert.assertEquals(5, res.readLastModifiedCount);
        Assert.assertEquals(1, res.readEntryCount);

        res.getBytes();

        Assert.assertEquals(5, res.readLastModifiedCount);
        Assert.assertEquals(2, res.readEntryCount);

        Assert.assertFalse(res.checkModified());

        Assert.assertEquals(6, res.readLastModifiedCount);
        Assert.assertEquals(2, res.readEntryCount);

    }

    private class TestResource extends AbstractResource {

        int readLastModifiedCount = 0;
        int readEntryCount = 0;

        long lastModified;
        String data;

        protected TestResource(String basePath, String path) {
            super(basePath, path);
        }

        protected TestResource(String basePath, String path, String data, long lastModified) {
            super(basePath, path);
            this.data = data;
            this.lastModified = lastModified;
        }

        @Override
        protected Entry readEntry() {
            readEntryCount++;
            return new Entry(lastModified, data.getBytes());
        }

        @Override
        protected long readLastModified() {
            readLastModifiedCount++;
            return lastModified;
        }

        @Override
        public List<String> findRequiresTags() {
            return null;
        }
    }

}
