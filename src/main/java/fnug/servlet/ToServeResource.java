package fnug.servlet;

import java.io.UnsupportedEncodingException;

import fnug.ResourceServlet;
import fnug.resource.AbstractResource;
import fnug.resource.DefaultCompressedResource;
import fnug.resource.Resource;

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

public class ToServeResource implements ToServe {

    private static final String MIME_TEXT = "text/";
    private Resource res;
    private boolean isJsonP;
    private byte[] bytes;

    public ToServeResource(Resource res, String jsonp) {
        this.res = res;
        isJsonP = jsonp != null && isText(res.getContentType());

        if (isJsonP) {
            String result = jsonp + "('";
            result += escapeJsonP(res.getBytes());
            result += "');";
            try {
                bytes = result.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                // dum de dum
            }
        } else {
            bytes = res.getBytes();
        }

    }

    private boolean isText(String contentType) {
        return contentType != null && contentType.startsWith(MIME_TEXT);
    }

    private String escapeJsonP(byte[] bytes) {

        String s = null;

        try {
            s = new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // so tiring...
        }

        s = s.replace("'", "\\'");
        s = s.replaceAll("\r", "\\\\r");
        s = s.replaceAll("\n", "\\\\n");

        return s;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
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
        
        if (res.isJs()) {
            return AbstractResource.CONTENT_TYPE_TEXT_JAVASCRIPT;
        } else {
            return isJsonP ? ResourceServlet.CONTENT_TYPE_JS : res.getContentType();
        }
        
    }
    
    @Override
    public String getCharacterEncoding() {
        return ResourceServlet.UTF_8;
    }

}