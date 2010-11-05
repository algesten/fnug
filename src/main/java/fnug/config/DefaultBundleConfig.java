package fnug.config;

import java.util.regex.Pattern;

import fnug.resource.Resource;

public class DefaultBundleConfig implements BundleConfig {

    private Resource configResource;
    private String name;
    private String basePath;
    private Pattern[] matches;
    private boolean jsLint;
    private boolean checkModified;
    private String[] jsCompileArgs;
    private String[] files;

    public DefaultBundleConfig(Resource configResource, String name, String basePath, Pattern[] matches,
            boolean jsLint, boolean checkModified,
            String[] jsCompileArgs, String[] files) {
        this.configResource = configResource;
        this.name = name;
        this.basePath = basePath;
        this.matches = matches;
        this.jsLint = jsLint;
        this.checkModified = checkModified;
        this.jsCompileArgs = jsCompileArgs;
        this.files = files;
    }

    @Override
    public Resource configResource() {
        return configResource;
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
