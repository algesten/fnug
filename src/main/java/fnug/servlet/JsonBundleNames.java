package fnug.servlet;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import fnug.resource.Bundle;

public class JsonBundleNames {

    @JsonProperty
    private LinkedList<String> bundles = new LinkedList<String>();

    public JsonBundleNames(List<Bundle> bs) {

        for (Bundle b : bs) {
            bundles.add(b.getName());
        }

    }

}