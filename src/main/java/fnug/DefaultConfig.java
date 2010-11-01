package fnug;

import java.util.Collection;

public class DefaultConfig implements Config {

    private BundleConfig[] bundleConfigs;

    public DefaultConfig(BundleConfig... bundleConfigs) {
        this.bundleConfigs = bundleConfigs;
    }

    public <T extends Collection<BundleConfig>> DefaultConfig(T configs) {
        this.bundleConfigs = configs.toArray(new BundleConfig[configs.size()]);
    }

    @Override
    public BundleConfig[] getBundleConfigs() {
        return bundleConfigs;
    }

}
