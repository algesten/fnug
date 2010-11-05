package fnug;

import java.util.List;

public abstract class AbstractAggregatedResource extends AbstractResource implements AggregatedResource {

    protected AbstractAggregatedResource(String basePath, String path) {
        super(basePath, path);
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
            // trigger check of actual resource modified
            res.checkModified();
            long l = res.getLastModified();
            if (l > lastModified) {
                lastModified = l;
            }
        }
        for (Resource res : getDependencies()) {
            // trigger check of actual resource modified
            res.checkModified();
            long l = res.getLastModified();
            if (l > lastModified) {
                lastModified = l;
            }
        }
        return lastModified;
    }

    @Override
    public List<String> findRequiresTags() {
        throw new UnsupportedOperationException("Not possible to find @requires on aggregated resource");
    }

}
