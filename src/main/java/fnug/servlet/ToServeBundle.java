package fnug.servlet;

import java.io.UnsupportedEncodingException;

import org.codehaus.jackson.map.ObjectMapper;

import fnug.ResourceServlet;
import fnug.resource.Bundle;

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

public class ToServeBundle implements ToServe {

    byte[] bytes;
    long lastModified;
    boolean isJsonP;

    public ToServeBundle(ObjectMapper mapper, Bundle bundle, String jsonp) {

        lastModified = bundle.getLastModified();
        isJsonP = jsonp != null;

        JsonBundle jb = new JsonBundle(bundle);

        String result = isJsonP ? jsonp + "(" : "";

        try {
            result += mapper.writeValueAsString(jb);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate json", e);
        }

        if (isJsonP) {
            result += ");";
        }

        try {
            bytes = result.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            // ye ye whateva.
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
        return isJsonP ? ResourceServlet.CONTENT_TYPE_JS : ResourceServlet.CONTENT_TYPE_JSON;
    }

    @Override
    public String getCharacterEncoding() {
        return ResourceServlet.UTF_8;
    }

}