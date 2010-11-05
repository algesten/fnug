package fnug;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

public class TarjanTest {

    private static HashMap<String, String[]> model = new HashMap<String, String[]>();
    static {
        model.put("a1", new String[] { "b1", "c1" });
        model.put("b1", new String[] { "c1" });
        model.put("a2", new String[] { "b2" });
        model.put("b2", new String[] { "a2" });
        model.put("a3", new String[] { "b3" });
        model.put("b3", new String[] { "c3" });
        model.put("c3", new String[] { "a3" });
    }

    @Test
    public void testSimpleDeps() {
        Tarjan tarjan = new Tarjan(makeResource("b1"));
        Assert.assertEquals("[[c1], [b1]]", tarjan.getResult() + "");
        tarjan = new Tarjan(makeResource("a1"));
        Assert.assertEquals("[[c1], [b1], [a1]]", tarjan.getResult() + "");
    }

    @Test
    public void testCyclicDeps() {
        Tarjan tarjan = new Tarjan(makeResource("a2"));
        Assert.assertEquals("[[b2, a2]]", tarjan.getResult() + "");
        tarjan = new Tarjan(makeResource("b3"));
        Assert.assertEquals("[[a3, c3, b3]]", tarjan.getResult() + "");
    }

    @BeforeClass
    public static void beforeClass() {
        ResourceResolver.setThreadLocal(new ResourceResolver() {
            @Override
            public Resource getResource(String path) {
                return makeResource(path);
            }
        });
    }

    private static Resource makeResource(final String path) {
        return new Resource() {

            @Override
            @SuppressWarnings("unchecked")
            public List<String> parseRequires() {
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
        };
    }

}
