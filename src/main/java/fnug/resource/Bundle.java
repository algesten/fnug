package fnug.resource;

import fnug.config.BundleConfig;

public interface Bundle {

    BundleConfig getConfig();

    String getName();

    Resource resolve(String path);

    ResourceCollection[] getResourceCollections();

}
