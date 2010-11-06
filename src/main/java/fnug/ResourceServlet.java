package fnug;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fnug.resource.Bundle;
import fnug.resource.DefaultResource;
import fnug.resource.Resource;
import fnug.resource.ResourceResolver;

@SuppressWarnings("serial")
public class ResourceServlet extends HttpServlet {

    private static ThreadLocal<RequestEntry> reqEntry = new ThreadLocal<RequestEntry>();

    private ResourceResolver resolver;

    private String bootstrapJs;

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

        reqEntry.set(new RequestEntry(path));

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

        private String path;
        private String file;
        private String suffix;

        private Object toServe;

        public RequestEntry(String path) {

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

        private void initToServe() {

            try {
                if (Bundle.BUNDLE_ALLOWED_CHARS.matcher(file).matches()) {
                    Bundle bundle = resolver.getBundle(file);
                    if (bundle != null) {
                        if (suffix.equals("")) {
                            toServe = bundle;
                        } else if (suffix.equals("js")) {
                            toServe = new Bootstrap(bundle);
                        } else {
                            toServe = null;
                        }
                    }
                }

                toServe = resolver.resolve(path);

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

            } else if (toServe instanceof Bundle) {

                serveBundle(resp, head, (Bundle) toServe);

            } else if (toServe instanceof Bootstrap) {

                serveBootstrap(resp, head, (Bootstrap) toServe);

            } else if (toServe instanceof Resource) {

                serveResource(resp, head, (Resource) toServe);

            }

        }

        private void serve400(HttpServletResponse resp, String msg) throws IOException {

            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);

        }

        private void serve404(HttpServletResponse resp) throws IOException {

            resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);

        }

        private void serveBootstrap(HttpServletResponse resp, boolean head, Bootstrap bootstrap) {
            // TODO Auto-generated method stub

        }

        private void serveBundle(HttpServletResponse resp, boolean head, Bundle bundle) {
            // TODO Auto-generated method stub

        }

        private void serveResource(HttpServletResponse resp, boolean head, Resource resource) {
            // TODO Auto-generated method stub

        }

        public long getLastModified() {
            // TODO Auto-generated method stub
            return 0;
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
    }

    private class Bootstrap {

        Bundle bundle;

        public Bootstrap(Bundle bundle) {
            this.bundle = bundle;
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

}
