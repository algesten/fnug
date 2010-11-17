package fnug.resource;

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
 * Extension of {@link Resource} for resources that are products of other
 * resource.
 * 
 * @author Martin Algesten
 * 
 */
public interface AggregatedResource extends Resource {

    /**
     * Base path for an aggregated resource is set to the owning bundle's name prepended with a slash.
     * That is for a aggregated resource beloning to <code>mybundle</code>, the base path with be
     * <code>mybundle/</code>.
     */
    String getBasePath();
    
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
