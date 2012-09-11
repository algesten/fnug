package fnug.resource;

import org.junit.Assert;
import org.junit.Test;

import fnug.config.BundleConfig;
import fnug.util.JSLintWrapper;

public class DefaultBundleResourceTest {

    @Test
    public void testJSLint() {

        Bundle bundle = makeBundle(0);

        DefaultBundleResource r = new DefaultBundleResource(bundle, "doesntlint.js");

        String lint = r.getJSLintResult();

        Assert.assertEquals("<div id='errors'><p>Line 1 char 9: " +
        		"Expected exactly one space between 'function' and '('.</p><p class='evidence'>function() " +
        		"{</p><p>Line 1 char 9: Missing name in function statement.</p><p class='evidence'>function() " +
        		"{</p><p>Line 1 char 9: Stopping.  (33% scanned).</p></div>", lint);

    }


    private Bundle makeBundle(final int checkModifiedInterval) {
        return makeBundle("testbundle", checkModifiedInterval);
    }


    private Bundle makeBundle(final String bundleName, final int checkModifiedInterval) {
        return new Bundle() {

            @Override
            public BundleConfig getConfig() {
                return new BundleConfig() {

                    @Override
                    public String name() {
                        return bundleName + "config";
                    }


                    @Override
                    public String[] jsLintArgs() {
                        return null;
                    }


                    @Override
                    public String[] files() {
                        return null;
                    }


                    @Override
                    public Resource configResource() {
                        return new DefaultResource("/", "testconfig1-simple.js");
                    }


                    @Override
                    public int checkModifiedInterval() {
                        return checkModifiedInterval;
                    }


                    @Override
                    public String basePath() {
                        return "/test/";
                    }
                };
            }


            @Override
            public String getName() {
                return bundleName;
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
                return -1;
            }


            @Override
            public boolean checkModified() {
                return checkModifiedInterval != 0;
            }


            @Override
            public JSLintWrapper getJsLinter() {
                return new JSLintWrapper("jslint browser: true, continue: true, indent: 4, maxlen: 120, " +
                		"plusplus: true, sloppy: true, undef: true, unparam: true, vars: true".split(","));
            }

        };
    }

}
