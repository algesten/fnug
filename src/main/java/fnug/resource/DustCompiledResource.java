package fnug.resource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import ro.isdc.wro.extensions.processor.js.DustJsProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;


public class DustCompiledResource extends AbstractCompiledResource {

    /**
     * Constructs setting base path and path.
     * 
     * @param basePath
     *            The base path of the resource. See {@link #getBasePath()}.
     * @param path
     *            The path of the resource. See {@link #getPath()}.
     */
    public DustCompiledResource(String basePath, String path) {
        super(basePath, path);
    }


    /**
     * Dust templates are JS. So return true.
     * 
     * @return true
     */
    @Override
    public boolean isJs() {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected byte[] compile(byte[] source) {

        try {

            DustJsProcessor processor = new DustJsProcessor();

            StringReader reader = new StringReader(new String(source, "utf-8"));
            StringWriter writer = new StringWriter();

            processor.process(Resource.create(getFullPath(), ResourceType.JS), reader, writer);

            return writer.toString().getBytes("utf-8");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
