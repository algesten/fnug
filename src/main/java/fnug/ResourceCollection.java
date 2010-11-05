package fnug;

public interface ResourceCollection extends AggregatedResource {

    byte[] getJs();

    byte[] getCss();

    byte[] getCompressedJs();

    byte[] getCompressedCss();

}
