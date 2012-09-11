package fnug.servlet;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import fnug.ResourceServlet;
import fnug.resource.Bundle;
import fnug.resource.ResourceResolver;

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

public class BundleNames implements ToServe {

    byte[] bytes;
    long lastModified;
    boolean isJsonP;

    public BundleNames(ObjectMapper mapper, String jsonp) {

        List<Bundle> bundles = ResourceResolver.getInstance().getBundles();

        lastModified = ResourceResolver.getInstance().getLastModified();
        isJsonP = jsonp != null;

        JsonBundleNames jbns = new JsonBundleNames(bundles);

        String result = isJsonP ? jsonp + "(" : "";

        try {
            result += mapper.writeValueAsString(jbns); // uses utf-8
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate json", e);
        }

        if (isJsonP) {
            result += ");";
        }

        try {
            bytes = result.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            // as if
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
        return isJsonP ? ResourceServlet.CONTENT_TYPE_JS : ResourceServlet.CONTENT_TYPE_JSON;
    }

    @Override
    public String getCharacterEncoding() {
        return ResourceServlet.UTF_8;
    }

}