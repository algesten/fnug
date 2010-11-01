package fnug;

import java.util.regex.Pattern;

public class DefaultBundleConfig implements BundleConfig {

    private String name;
    private String basePath;
    private Pattern[] matches;
    private boolean jsLint;
    private boolean checkModified;
    private String[] jsCompileArgs;
    private String[] files;

    public DefaultBundleConfig(String name, String basePath, Pattern[] matches, boolean jsLint, boolean checkModified,
            String[] jsCompileArgs, String[] files) {
        this.name = name;
        this.basePath = basePath;
        this.matches = matches;
        this.jsLint = jsLint;
        this.checkModified = checkModified;
        this.jsCompileArgs = jsCompileArgs;
        this.files = files;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String basePath() {
        return basePath;
    }

    @Override
    public Pattern[] matches() {
        return matches;
    }

    @Override
    public boolean jsLint() {
        return jsLint;
    }

    @Override
    public boolean checkModified() {
        return checkModified;
    }

    @Override
    public String[] jsCompileArgs() {
        return jsCompileArgs;
    }

    @Override
    public String[] files() {
        return files;
    }

}
