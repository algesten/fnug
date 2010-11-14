package fnug.servlet;

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