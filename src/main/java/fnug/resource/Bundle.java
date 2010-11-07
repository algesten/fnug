package fnug.resource;

import java.util.regex.Pattern;

import fnug.config.BundleConfig;

public interface Bundle {

    public final static Pattern BUNDLE_ALLOWED_CHARS = Pattern.compile("[a-zA-Z0-9_]+");

    BundleConfig getConfig();

    String getName();

    Resource resolve(String path);

    ResourceCollection[] getResourceCollections();

    long getLastModified();

}
