package fnug;

import org.junit.Assert;
import org.junit.Test;

public class AbstractAggregatedResourceTest {

    @Test
    public void testConstructor() {

        try {
            new TestResource("", "foo.js", null, null);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // ok
        }

        try {
            new TestResource("/", "/foo.js", null, null);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // ok
        }

        TestResource r1 = new TestResource("/", "foo.js", null, null);

        Assert.assertNotNull(r1.getAggregates());
        Assert.assertNotNull(r1.getDependencies());

        Assert.assertEquals(0, r1.getAggregates().length);
        Assert.assertEquals(0, r1.getDependencies().length);

        r1 = new TestResource("/", "foo.js", new Resource[] { makeResource("1", 0), makeResource("2", 0) }, null);

        Assert.assertNotNull(r1.getAggregates());
        Assert.assertNotNull(r1.getDependencies());

        Assert.assertEquals(2, r1.getAggregates().length);
        Assert.assertEquals(0, r1.getDependencies().length);

        r1 = new TestResource("/", "foo.js", null, new Resource[] { makeResource("1", 0), makeResource("2", 0) });

        Assert.assertNotNull(r1.getAggregates());
        Assert.assertNotNull(r1.getDependencies());

        Assert.assertEquals(0, r1.getAggregates().length);
        Assert.assertEquals(2, r1.getDependencies().length);

    }

    @Test
    public void testLastModified() {

        TestResource r1 = new TestResource("/", "foo.js", null, new Resource[] { makeResource("1", 1),
                makeResource("2", 2) });

        Assert.assertEquals(2l, r1.getLastModified());

        r1 = new TestResource("/", "foo.js", new Resource[] { makeResource("1", 1),
                makeResource("2", 2) }, new Resource[] { makeResource("3", 3), makeResource("4", 4) });

        Assert.assertEquals(4l, r1.getLastModified());

    }

    private Resource makeResource(final String path, final long lastModified) {
        return new Resource() {

            @Override
            public boolean isJs() {
                return false;
            }

            @Override
            public boolean isCss() {
                return false;
            }

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public long getLastModified() {
                return lastModified;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public byte[] getBytes() {
                return null;
            }

            @Override
            public boolean checkModified() {
                return false;
            }
        };
    }

    private class TestResource extends AbstractAggregatedResource {

        protected TestResource(String basePath, String path, Resource[] aggregates, Resource[] dependencies) {
            super(basePath, path, aggregates, dependencies);
        }

        @Override
        protected byte[] buildAggregate() {
            return new byte[] { 1, 2, 3 };
        }
    }

}
