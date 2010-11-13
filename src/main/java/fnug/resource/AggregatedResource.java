package fnug.resource;

/**
 * Extension of {@link Resource} for resources that are products of other
 * resource.
 * 
 * @author Martin Algesten
 * 
 */
public interface AggregatedResource extends Resource {

    /**
     * Aggregates are the resources that actually make up the aggregated
     * resource - that are used to produce the {@link #getBytes()}.
     * 
     * @return the resources that are part of producing the aggregated resource
     *         bytes.
     */
    Resource[] getAggregates();

    /**
     * Dependencies are resources that are just dependent on for
     * {@link #getLastModified()} (along with {@link #getAggregates()}) but are
     * not part of making the bytes of the aggregate.
     * 
     * @return the dependencies used only for last modified date, not bytes.
     */
    Resource[] getDependencies();

}
