package fnug.servlet;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import fnug.ResourceServlet;
import fnug.resource.Bundle;
import fnug.resource.ResourceResolver;

public class BundleNames implements ToServe {

    byte[] bytes;
    long lastModified;

    public BundleNames(ObjectMapper mapper) {

        List<Bundle> bundles = ResourceResolver.getInstance().getBundles();

        lastModified = ResourceResolver.getInstance().getLastModified();

        JsonBundleNames jbns = new JsonBundleNames(bundles);

        try {
            bytes = mapper.writeValueAsBytes(jbns); // uses utf-8
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate json", e);
        }

    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean futureExpires() {
        return false;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String getContentType() {
        return ResourceServlet.CONTENT_TYPE_JSON;
    }

}