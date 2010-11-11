package fnug.resource;

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
            lastModified = Math.max(lastModified, res.getLastModified());
        }
        for (Resource res : getDependencies()) {
            lastModified = Math.max(lastModified, res.getLastModified());
        }
        return lastModified;
    }

    @Override
    public boolean checkModified() {
        for (Resource res : getAggregates()) {
            res.checkModified();
        }
        for (Resource res : getDependencies()) {
            res.checkModified();
        }
        return super.checkModified();
    }

    @Override
    public List<String> findRequiresTags() {
        throw new UnsupportedOperationException("Not possible to find @requires on aggregated resource");
    }

}
