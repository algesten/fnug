package fnug;

public interface AggregatedResource extends Resource {

    Resource[] getAggregates();

    Resource[] getDependencies();

}
