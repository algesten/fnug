package fnug.config;

import java.util.Collection;

/**
 * Default implementation of {@link Config}
 * 
 * @author Martin Algesten
 * 
 */
public class DefaultConfig implements Config {

    private BundleConfig[] bundleConfigs;

    /**
     * Constructs from a bunch of {@link BundleConfig}.
     * 
     * @param bundleConfigs
     *            configs.
     */
    public DefaultConfig(BundleConfig... bundleConfigs) {
        this.bundleConfigs = bundleConfigs;
    }

    /**
     * Constructs using a collection of configs.
     * 
     * @param <T>
     *            type of collection
     * @param configs
     *            configs
     */
    public <T extends Collection<BundleConfig>> DefaultConfig(T configs) {
        this.bundleConfigs = configs.toArray(new BundleConfig[configs.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleConfig[] getBundleConfigs() {
        return bundleConfigs;
    }

}
