package fnug;

public abstract class AbstractAggregatedResource extends AbstractResource implements AggregatedResource {

    private static final Resource[] EMPTY_RESOURCES = new Resource[] {};

    private Resource[] aggregates;
    private Resource[] dependencies;

    protected AbstractAggregatedResource(String basePath, String path, Resource[] aggregates, Resource[] dependencies) {
        super(basePath, path);
        this.aggregates = aggregates == null ? EMPTY_RESOURCES : aggregates;
        this.dependencies = dependencies == null ? EMPTY_RESOURCES : dependencies;
    }

    @Override
    public Resource[] getAggregates() {
        return aggregates;
    }

    @Override
    public Resource[] getDependencies() {
        return dependencies;
    }

    @Override
    protected Entry readEntry() {
        return new Entry(readLastModified(), buildAggregate());
    }

    protected abstract byte[] buildAggregate();

    @Override
    protected long readLastModified() {
        long lastModified = 0l;
        for (Resource res : getAggregates()) {
            long l = res.getLastModified();
            if (l > lastModified) {
                lastModified = l;
            }
        }
        for (Resource res : getDependencies()) {
            long l = res.getLastModified();
            if (l > lastModified) {
                lastModified = l;
            }
        }
        return lastModified;
    }

}
