package fnug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.ObjectMapper;

import fnug.resource.Bundle;
import fnug.resource.DefaultResource;
import fnug.resource.HasLastModifiedBytes;
import fnug.resource.Resource;
import fnug.resource.ResourceCollection;
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

/**
 * Servlet serving resources.
 * 
 * @author Martin Algesten
 * 
 */
@SuppressWarnings("serial")
public class ResourceServlet extends HttpServlet {

    private static final String UTF_8 = "utf-8";

    private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    private static final String CONTENT_TYPE_JS = "text/javascript; charset=utf8";

    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    private static final String HEADER_CONTENT_ENCODING = "Content-Encoding";

    private static final String VALUE_GZIP = "gzip";

    private static ThreadLocal<RequestEntry> reqEntry = new ThreadLocal<RequestEntry>();

    private ResourceResolver resolver;
    private String bootstrapJs;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        initResolver(config);

        initBootstrapJs();

    }

    private void initBootstrapJs() {

        Resource res = new DefaultResource("/fnug/", "bootstrap.js");

        if (res.getLastModified() == -1) {
            throw new IllegalStateException("Missing classpath resource: " + res.getFullPath());
        }

        // encoding not interesting since bootstrap.js contains no strange chars
        bootstrapJs = new String(res.getBytes());

    }

    private void initResolver(ServletConfig config) throws ServletException {

        String configStr = config.getInitParameter("config");
        if (configStr == null) {
            throw new ServletException("Missing config parameter 'config'");
        }

        String contextKey = ResourceResolver.class.getName() + "_" + configStr.hashCode();

        resolver = (ResourceResolver) config.getServletContext().getAttribute(contextKey);
        if (resolver == null) {

            String[] configs = configStr.split("\\s*,\\s*");

            LinkedList<Resource> resources = new LinkedList<Resource>();
            for (String s : configs) {
                String basePath = s.substring(0, s.lastIndexOf(File.separator) + 1);
                String path = s.substring(s.lastIndexOf(File.separator) + 1);
                resources.add(new DefaultResource(basePath, path));
            }

            resolver = new ResourceResolver(resources);
            config.getServletContext().setAttribute(contextKey, resolver);

        }

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resolver.setThreadLocal();
        resolver.checkModified();

        String path = req.getPathInfo();

        boolean gzip = req.getHeader(HEADER_ACCEPT_ENCODING).indexOf(VALUE_GZIP) >= 0;

        RequestEntry entry = new RequestEntry(req.getServletPath(), path, gzip);
        reqEntry.set(entry);

        super.service(req, resp);

        // when the servlet container does a 304 not modified, Content-Type is
        // set to null, this often results in the Content-Type being set to a
        // default by the servlet container or a web server/cache in front of
        // the servlet container. That deafult content type is wrong about the
        // original resource (text/plain or similar). by always setting the
        // "correct" content type, we ensure to not pollute caches etc.
        // according to the HTTP spec, t's okay to set any meta header about the
        // content as long as they are true for the original resource.
        if (resp.getContentType() == null) {
            entry.setContentTypeAndLength(resp);
        }

    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return reqEntry.get().getLastModified();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        reqEntry.get().serve(resp, false);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        reqEntry.get().serve(resp, true);
    }

    private class RequestEntry {

        private static final String CHAR_SLASH = "/";
        private static final String CHAR_DOT = ".";

        private String servletPath;

        /**
         * Full path: /some/path/to/file.js
         */
        private String path;

        /**
         * The filename without suffix: file
         */
        private String file;

        /**
         * The suffix: js
         */
        private String suffix;
        private Object toServe;
        private byte[] toServeBytes;
        private boolean gzip;

        public RequestEntry(String servletPath, String path, boolean gzip) {

            this.servletPath = servletPath.endsWith(CHAR_SLASH) ? servletPath.substring(0, servletPath.length() - 1)
                    : servletPath;
            this.gzip = gzip;

            initPathFileSuffix(path);

            initToServe();
            initToServeBytes();

        }

        private void initPathFileSuffix(String inpath) {
            if (inpath == null) {
                inpath = "";
            }
            inpath = normalizePath(inpath);
            int lastDot = inpath.lastIndexOf(CHAR_DOT);
            path = inpath;
            if (lastDot > inpath.lastIndexOf(CHAR_SLASH)) {
                file = inpath.substring(0, lastDot);
                suffix = inpath.substring(lastDot + 1);
            } else {
                file = inpath;
                suffix = "";
            }
        }

        private String normalizePath(String path) {

            if (path.startsWith(CHAR_SLASH)) {
                path = path.substring(1);
            }

            if (path.endsWith(CHAR_SLASH)) {
                path = path.substring(0, path.length() - 1);
            }
            return path;
        }

        private void initToServe() {

            try {

                if (path.equals("")) {
                    toServe = new BundleNames();
                } else if (Bundle.BUNDLE_ALLOWED_CHARS.matcher(file).matches()) {
                    Bundle bundle = resolver.getBundle(file);
                    if (bundle != null) {
                        bundle.checkModified();
                        if (suffix.equals("")) {
                            toServe = new ToServeBundle(bundle);
                        } else if (suffix.equals("js")) {
                            toServe = new Bootstrap(servletPath, bundle);
                        } else {
                            toServe = null;
                        }
                    }
                }
                if (toServe == null) {
                    Resource r = resolver.resolve(path);
                    if (r != null) {
                        r.checkModified();
                    }
                    toServe = r == null || r.getLastModified() == -1 ? null : new ToServeResource(r);
                }

            } catch (IllegalArgumentException iae) {
                toServe = new BadArg(iae.getMessage());
            } catch (IllegalStateException ise) {
                toServe = new BadArg(ise.getMessage());
            }

        }

        private void initToServeBytes() {
            if (toServe != null && toServe instanceof ToServe) {
                toServeBytes = ((ToServe) toServe).getBytes();
                if (gzip) {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        GZIPOutputStream os = new GZIPOutputStream(baos);
                        os.write(toServeBytes);
                        os.close();
                        toServeBytes = baos.toByteArray();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to comress gzip", e);
                    }
                }
            }
        }

        public void serve(HttpServletResponse resp, boolean head) throws IOException {

            if (toServe == null) {

                serve404(resp);

            } else if (toServe instanceof BadArg) {

                serve400(resp, ((BadArg) toServe).getMessage());

            } else if (toServe instanceof ToServe) {

                serveDefault(resp, head, (ToServe) toServe);

            }

        }

        private void serve404(HttpServletResponse resp) throws IOException {

            resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);

        }

        private void serve400(HttpServletResponse resp, String msg) throws IOException {

            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);

        }

        private void serveDefault(HttpServletResponse resp, boolean head, ToServe toServe) throws IOException {

            setContentTypeAndLength(resp);
            resp.setDateHeader("Last-Modified", toServe.getLastModified());
            if (gzip) {
                resp.setHeader(HEADER_CONTENT_ENCODING, VALUE_GZIP);
            }
            if (!head) {
                OutputStream os = resp.getOutputStream();
                os.write(toServeBytes);
            }

        }

        public void setContentTypeAndLength(HttpServletResponse resp) {

            if (toServe != null && toServe instanceof ToServe) {
                ToServe t = (ToServe) toServe;
                resp.setContentType(t.getContentType());
                resp.setContentLength(toServeBytes.length);
            }

        }

        public long getLastModified() {

            if (toServe != null && toServe instanceof ToServe) {
                return ((ToServe) toServe).getLastModified();
            }

            return -1;

        }
    }

    private class BundleNames implements ToServe {

        byte[] bytes;
        long lastModified;

        public BundleNames() {

            List<Bundle> bundles = resolver.getBundles();

            lastModified = resolver.getLastModified();

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
        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public String getContentType() {
            return CONTENT_TYPE_JSON;
        }

    }

    private class JsonBundleNames {

        @JsonProperty
        private LinkedList<String> bundles = new LinkedList<String>();

        public JsonBundleNames(List<Bundle> bs) {

            for (Bundle b : bs) {
                bundles.add(b.getName());
            }

        }

    }

    private class Bootstrap implements ToServe {

        private static final String TOKEN_BASE_URL = "/\\*\\*\\*baseUrl\\*\\*\\*/";
        private static final String TOKEN_BUNDLES = "\\[/\\*\\*\\*bundles\\*\\*\\*/\\]";
        byte[] bytes;
        long lastModified;

        public Bootstrap(String baseUrl, Bundle bundle) {
            initBytes(baseUrl, bundle);
        }

        private void initBytes(String baseUrl, Bundle bundle) {

            this.lastModified = bundle.getLastModified();

            JsonBundle jb = new JsonBundle(bundle);
            String jbs;

            try {
                jbs = mapper.writeValueAsString(jb.colls);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate json", e);
            }

            String subst = bootstrapJs;
            subst = subst.replaceAll(TOKEN_BASE_URL, baseUrl);
            subst = subst.replaceAll(TOKEN_BUNDLES, jbs);

            try {
                bytes = subst.getBytes(UTF_8);
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
        public String getContentType() {
            return CONTENT_TYPE_JS;
        }

    }

    private class JsonBundle {

        @JsonProperty
        private LinkedList<JsonResourceCollection> colls = new LinkedList<JsonResourceCollection>();

        public JsonBundle(Bundle bundle) {

            ResourceCollection[] tmp = bundle.getResourceCollections();
            for (ResourceCollection c : tmp) {
                colls.add(new JsonResourceCollection(c));
            }

        }
    }

    @JsonPropertyOrder({ "name", "compJs", "compCss", "files" })
    @SuppressWarnings("unused")
    private class JsonResourceCollection {
        @JsonProperty
        String name;
        @JsonProperty
        String compJs;
        @JsonProperty
        String compCss;
        @JsonProperty
        LinkedList<String> files = new LinkedList<String>();

        public JsonResourceCollection(ResourceCollection c) {
            name = c.getBundle().getName();
            if (c.getCompressedJs().getLastModified() > 0) {
                compJs = c.getCompressedJs().getFullPath();
            }
            if (c.getCompressedCss().getLastModified() > 0) {
                compCss = c.getCompressedCss().getFullPath();
            }
            for (Resource r : c.getAggregates()) {
                files.add(r.getFullPath());
            }
        }
    }

    private class ToServeBundle implements ToServe {

        byte[] bytes;
        long lastModified;

        public ToServeBundle(Bundle bundle) {

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
        public String getContentType() {
            return CONTENT_TYPE_JSON;
        }
    }

    private class ToServeResource implements ToServe {

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
        public String getContentType() {
            return res.getContentType();
        }

    }

    private class BadArg {

        String message;

        public BadArg(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private interface ToServe extends HasLastModifiedBytes {

        String getContentType();

    }

}
