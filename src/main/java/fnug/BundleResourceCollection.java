package fnug;

public interface BundleResourceCollection extends AggregatedResource {

    Bundle getBundle();

    byte[] getJs();

    byte[] getCss();

    byte[] getCompressedJs();

    byte[] getCompressedCss();

}
