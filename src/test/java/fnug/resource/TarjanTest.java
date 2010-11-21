package fnug.resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import fnug.config.BundleConfig;
import fnug.resource.Resource;
import fnug.resource.ResourceResolver;
import fnug.resource.Tarjan;
import fnug.util.JSLintWrapper;

public class TarjanTest {

    private static HashMap<String, String[]> model = new HashMap<String, String[]>();
    private static HashMap<String, String> bundles = new HashMap<String, String>();
    static {

        model.put("a1", new String[] { "b1", "c1" });
        model.put("b1", new String[] { "c1" });
        model.put("a2", new String[] { "b2" });
        model.put("b2", new String[] { "a2" });
        model.put("a3", new String[] { "b3" });
        model.put("b3", new String[] { "c3" });
        model.put("c3", new String[] { "a3" });

        bundles.put("a1", "bundle");
        bundles.put("b1", "bundle");
        bundles.put("c1", "bundle");
        bundles.put("a2", "bundle");
        bundles.put("b2", "bundle");
        bundles.put("a3", "bundle");
        bundles.put("b3", "bundle");
        bundles.put("c3", "bundle");

        model.put("a4", new String[] { "b4", "c4" });
        model.put("b4", new String[] { "c4" });
        model.put("a5", new String[] { "b5", "c5" });
        model.put("b5", new String[] { "c5" });

        bundles.put("a4", "bundle1");
        bundles.put("b4", "bundle2");
        bundles.put("c4", "bundle2");
        bundles.put("a5", "bundle1");
        bundles.put("b5", "bundle2");
        bundles.put("c5", "bundle1");

    }

    @BeforeClass
    public static void beforeClass() {
        ResourceResolver.setThreadLocal(new ResourceResolver() {
            @Override
            public Resource resolve(String path) {
                return makeResource(path);
            }
        });
    }

    @Test
    public void testSimpleDeps() {
        Tarjan tarjan = new Tarjan(makeResource("b1"));
        Assert.assertEquals("[c1, b1]", tarjan.getResult() + "");
        tarjan = new Tarjan(makeResource("a1"));
        Assert.assertEquals("[c1, b1, a1]", tarjan.getResult() + "");
    }

    @Test
    public void testCyclicDeps() {
        Tarjan tarjan = new Tarjan(makeResource("a2"));
        try {
            tarjan.getResult();
            Assert.fail();
        } catch (IllegalStateException ise) {
            Assert.assertEquals("Found cyclic dependency: b2 -> a2 -> b2", ise.getMessage());
        }
        tarjan = new Tarjan(makeResource("b3"));
        try {
            tarjan.getResult();
            Assert.fail();
        } catch (IllegalStateException ise) {
            Assert.assertEquals("Found cyclic dependency: a3 -> c3 -> b3 -> a3", ise.getMessage());
        }
    }

    @Test
    public void testBundleDeps() {

        Tarjan tarjan = new Tarjan(makeResource("a4"));

        Assert.assertEquals("[c4, b4, a4]", tarjan.getResult().toString());

    }

    @Test
    public void testCyclicBundleDeps() {

        Tarjan tarjan = new Tarjan(makeResource("a5"));

        try {
            tarjan.getResult();
            Assert.fail();
        } catch (IllegalStateException ise) {
            Assert.assertEquals("Found cyclic bundle dependency: bundle2 -> bundle1 -> bundle2", ise.getMessage());
        }

    }

    private static Resource makeResource(String path) {
        String bundle = bundles.get(path);
        if (bundle == null) {
            throw new RuntimeException("Null bundle: " + path);
        }
        return new TestResource(bundle, path);
    }

    static class TestResource implements Resource, HasBundle {

        String bundle;
        String path;

        public TestResource(String bundle, String path) {
            this.bundle = bundle;
            this.path = path;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> findRequiresTags() {
            String[] reqs = model.get(path);
            return (List<String>) (reqs == null ? Collections.emptyList() : Arrays.asList(reqs));
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
        public String getPath() {
            return path;
        }

        @Override
        public String getBasePath() {
            return null;
        }

        @Override
        public long getLastModified() {
            return 0;
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

        @Override
        public String toString() {
            return path;
        }

        @Override
        public String getFullPath() {
            return null;
        }

        @Override
        public Bundle getBundle() {
            return new Bundle() {

                @Override
                public BundleConfig getConfig() {
                    return null;
                }

                @Override
                public String getName() {
                    return bundle;
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
                    return 0;
                }

                @Override
                public boolean checkModified() {
                    return false;
                }

                @Override
                public JSLintWrapper getJsLinter() {
                    return null;
                }

            };
        }
    }

}
