package fnug.resource;

import java.util.List;

public interface ResourceCollection extends AggregatedResource {

    byte[] getJs();

    byte[] getCss();

    Resource getCompressedJs();

    Resource getCompressedCss();

    List<Resource> getExistingJsAggregates();

    List<Resource> getExistingCssAggregates();

}
