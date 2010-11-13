package fnug.config;

import java.util.Collection;

/*
 Copyright 2010 Martin Algesten

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

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
