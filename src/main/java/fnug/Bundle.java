package fnug;

public interface Bundle extends AggregatedResource {

    BundleConfig getConfig();

    String getName();

    Bundle[] getBundles();

    byte[] getCompressedJs();

    byte[] getCompressedCss();

}
