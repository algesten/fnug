package fnug;

public interface Bundle {

    BundleConfig getConfig();

    String getName();

    Resource resolve(String path);

    BundleResourceCollection[] getResources();

}
