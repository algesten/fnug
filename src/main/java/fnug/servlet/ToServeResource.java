package fnug.servlet;

import fnug.resource.DefaultCompressedResource;
import fnug.resource.Resource;

public class ToServeResource implements ToServe {

    private Resource res;

    public ToServeResource(Resource res) {
        this.res = res;
    }

    @Override
    public byte[] getBytes() {
        return res.getBytes();
    }

    @Override
    public long getLastModified() {
        return res.getLastModified();
    }

    @Override
    public boolean futureExpires() {
        return res instanceof DefaultCompressedResource;
    }

    @Override
    public String getContentType() {
        return res.getContentType();
    }

}