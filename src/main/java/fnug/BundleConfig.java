package fnug;

import java.util.regex.Pattern;

public interface BundleConfig {

    static final boolean DEFAULT_CHECK_MODIFIED = true;
    static final boolean DEFAULT_JS_LINT = true;

    String name();

    String basePath();

    Pattern[] matches();

    boolean jsLint();

    boolean checkModified();

    String[] jsCompileArgs();

    String[] files();

}
