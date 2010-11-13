package fnug.resource;

import fnug.config.BundleConfig;

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
 * Extension of {@link DefaultResource} that implements {@link HasBundle}.
 * 
 * @author Martin Algesten
 * 
 */
public class DefaultBundleResource extends DefaultResource implements HasBundle {

    private Bundle bundle;

    /**
     * Constructs setting the necessary bundle and path. The
     * {@link BundleConfig#basePath()} will be used as {@link #getBasePath()}.
     * The {@link BundleConfig#checkModifiedInterval()} will be used for check
     * intervals of the resource.
     * 
     * @param bundle
     *            the bundle to construct from and which base path to use.
     * @param path
     *            the local path. See {@link #getPath()}.
     */
    public DefaultBundleResource(Bundle bundle, String path) {
        super(bundle.getConfig().basePath(), path, bundle.getConfig().checkModifiedInterval());
        this.bundle = bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getBundle() {
        return bundle;
    }

}
