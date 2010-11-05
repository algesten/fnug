package fnug.config;

import java.util.regex.Pattern;

import fnug.resource.Resource;

public interface BundleConfig {

    static final boolean DEFAULT_CHECK_MODIFIED = true;
    static final boolean DEFAULT_JS_LINT = true;

    Resource configResource();

    String name();

    String basePath();

    Pattern[] matches();

    boolean jsLint();

    boolean checkModified();

    String[] jsCompileArgs();

    String[] files();

}
