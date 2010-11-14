package fnug.servlet;

import java.util.LinkedList;

import org.codehaus.jackson.annotate.JsonProperty;

import fnug.resource.Bundle;
import fnug.resource.ResourceCollection;

public class JsonBundle {

    @JsonProperty
    LinkedList<JsonResourceCollection> colls = new LinkedList<JsonResourceCollection>();

    public JsonBundle(Bundle bundle) {

        ResourceCollection[] tmp = bundle.getResourceCollections();
        for (ResourceCollection c : tmp) {
            colls.add(new JsonResourceCollection(c));
        }

    }
}