package fnug;

import java.util.List;

public interface ResourceCollection extends AggregatedResource {

    byte[] getJs();

    byte[] getCss();

    byte[] getCompressedJs();

    byte[] getCompressedCss();

    List<Resource> getExistingJsAggregates();

    List<Resource> getExistingCssAggregates();

}
