package fnug.resource;

public interface AggregatedResource extends Resource {

    Resource[] getAggregates();

    Resource[] getDependencies();

}
