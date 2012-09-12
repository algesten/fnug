package fnug.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.FilenameUtils;

import ro.isdc.wro.extensions.processor.js.JsTemplateCompilerProcessor;
import ro.isdc.wro.extensions.processor.support.dustjs.DustJs;
import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;


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

            MyDustJsProcessor processor = new MyDustJsProcessor();

            StringReader reader = new StringReader(new String(source, "utf-8"));
            StringWriter writer = new StringWriter();

            processor.process(Resource.create(getFullPath(), ResourceType.CSS), reader, writer);

            return writer.toString().getBytes("utf-8");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    
    @SupportedResourceType(ResourceType.JS)
    class MyDustJsProcessor extends JsTemplateCompilerProcessor {
      public static final String ALIAS = "dustJs";

      @Override
      protected AbstractJsTemplateCompiler createCompiler() {
        return new DustJs() {
            @Override
            protected InputStream getCompilerAsStream() {
                return MyDustJsProcessor.class.getResourceAsStream("/fnug/fnug/dustjs-9015c2c-full.js");
            }
        };
      }

      @Override
      protected String getArgument(Resource resource) {
        final String name = resource == null ? "" : FilenameUtils.getBaseName(resource.getUri());
        return String.format("'%s'", name);
      }
    }

    
}
