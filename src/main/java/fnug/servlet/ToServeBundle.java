package fnug.servlet;

import org.codehaus.jackson.map.ObjectMapper;

import fnug.ResourceServlet;
import fnug.resource.Bundle;

public class ToServeBundle implements ToServe {

    byte[] bytes;
    long lastModified;

    public ToServeBundle(ObjectMapper mapper, Bundle bundle) {

        lastModified = bundle.getLastModified();

        JsonBundle jb = new JsonBundle(bundle);

        try {
            bytes = mapper.writeValueAsBytes(jb);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate json", e);
        }

    }

    @Override
    public byte[] getBytes() {
        return bytes;
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
    public String getContentType() {
        return ResourceServlet.CONTENT_TYPE_JSON;
    }
}