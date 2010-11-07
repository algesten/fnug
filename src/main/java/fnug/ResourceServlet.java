package fnug;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

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

@SuppressWarnings("serial")
public class ResourceServlet extends HttpServlet {

    private static final String UTF_8 = "utf-8";

    private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    private static final String CONTENT_TYPE_JS = "text/javascript; charset=utf8";

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
                String basePath = s.substring(0, s.lastIndexOf(File.separator));
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

        String path = req.getPathInfo();

        reqEntry.set(new RequestEntry(req.getServletPath(), path));

        super.service(req, resp);

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

        private String servletPath;

        private String path;
        private String file;
        private String suffix;

        private Object toServe;

        public RequestEntry(String servletPath, String path) {

            this.servletPath = servletPath;

            initPathFileSuffix(path);

            initToServe();

        }

        private void initPathFileSuffix(String path2) {
            this.path = normalizePath(path);
            int lastDot = path.lastIndexOf(".");
            if (lastDot > path.lastIndexOf("/")) {
                file = path.substring(0, lastDot);
                suffix = path.substring(lastDot + 1);
            } else {
                file = path;
                suffix = "";
            }
        }

        private String normalizePath(String path) {
            if (path == null) {
                path = "";
            }

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            if (path.endsWith("/")) {
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
                        if (suffix.equals("")) {
                            toServe = bundle;
                        } else if (suffix.equals("js")) {
                            toServe = new Bootstrap(servletPath, bundle);
                        } else {
                            toServe = null;
                        }
                    }
                }
                if (toServe == null) {
                    Resource r = resolver.resolve(path);
                    toServe = r == null ? null : new ToServeResource(r);
                }

            } catch (IllegalArgumentException iae) {
                toServe = new BadArg(iae.getMessage());
            } catch (IllegalStateException ise) {
                toServe = new BadArg(ise.getMessage());
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

            resp.setContentType(toServe.getContentType());
            byte[] b = toServe.getBytes();
            resp.setContentLength(b.length);
            resp.setDateHeader("Last-Modified", toServe.getLastModified());
            if (!head) {
                resp.getOutputStream().write(b);
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

        private static final String TOKEN_BUNDLE = "/***bundle***/";
        private static final String TOKEN_BASE_URL = "/***baseUrl***/";
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
                jbs = mapper.writeValueAsString(jb);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate json", e);
            }

            String subst = bootstrapJs;
            subst = subst.replaceAll(TOKEN_BASE_URL, baseUrl);
            subst = subst.replaceAll(TOKEN_BUNDLE, jbs);

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

    @JsonPropertyOrder({ "compJs", "compCss", "files" })
    @SuppressWarnings("unused")
    private class JsonResourceCollection {
        @JsonProperty
        private String compJs;
        @JsonProperty
        private String compCss;
        @JsonProperty
        private LinkedList<String> files = new LinkedList<String>();

        public JsonResourceCollection(ResourceCollection c) {
            compJs = c.getCompressedJs().getFullPath();
            compCss = c.getCompressedCss().getFullPath();
            for (Resource r : c.getAggregates()) {
                files.add(r.getFullPath());
            }
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
