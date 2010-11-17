package fnug.servlet;

import java.io.UnsupportedEncodingException;

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

public class Bootstrap implements ToServe {

    private static final String TOKEN_BASE_URL = "/***baseUrl***/";
    private static final String TOKEN_BUNDLES = "\"/***bundles***/\"";

    private ObjectMapper mapper;
    private byte[] bytes;
    private long lastModified;

    public Bootstrap(ObjectMapper mapper, String baseUrl, Bundle bundle) {
        this.mapper = mapper;
        initBytes(baseUrl, bundle);
    }

    private void initBytes(String baseUrl, Bundle bundle) {

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() -1);
        }
        
        Bundle fnug = ResourceResolver.getInstance().getBundle("fnug");

        // when developing bootstrap, turn on checkModified in /fnug/bundles.js.
        fnug.checkModified();

        // no strange utf-8 chars in bootstrap
        String bootstrapJs = new String(fnug.getResourceCollections()[0].getCompressedJs().getBytes());
        //String bootstrapJs = new String(fnug.getResourceCollections()[0].getJs());

        this.lastModified = Math.max(bundle.getLastModified(), fnug.getLastModified());

        JsonBundle jb = new JsonBundle(bundle);
        String jbs;

        try {
            jbs = mapper.writeValueAsString(jb.colls);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate json", e);
        }

        // Spliced in as string and parsed with JSON.parse().
        jbs = jbs.replace("\\", "\\\\");
        jbs = "\"" + jbs.replace("\"", "\\\"") + "\"";

        String result = bootstrapJs;
        result = result.replace(TOKEN_BASE_URL, baseUrl);
        result = result.replace(TOKEN_BUNDLES, jbs);

        try {
            bytes = result.getBytes(ResourceServlet.UTF_8);
        } catch (UnsupportedEncodingException e) {
            // nope
        }

    }


    public byte[] getBytes() {
        return bytes;
    }


    public long getLastModified() {
        return lastModified;
    }


    @Override
    public boolean futureExpires() {
        return false;
    }


    @Override
    public String getContentType() {
        return ResourceServlet.CONTENT_TYPE_JS;
    }

}