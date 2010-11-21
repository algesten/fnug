package fnug.servlet;

import java.util.LinkedList;

import org.codehaus.jackson.annotate.JsonProperty;

import fnug.resource.Bundle;
import fnug.resource.ResourceCollection;

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

public class JsonBundle {

    @JsonProperty
    String name;

    @JsonProperty
    LinkedList<JsonResourceCollection> colls = new LinkedList<JsonResourceCollection>();

    public JsonBundle(Bundle bundle) {

        this.name = bundle.getName();

        ResourceCollection[] tmp = bundle.getResourceCollections();
        for (ResourceCollection c : tmp) {
            colls.add(new JsonResourceCollection(c));
        }

    }
}