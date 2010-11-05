package fnug;

import java.util.List;

public interface Resource {

    String getBasePath();
    
    String getPath();

    String getContentType();

    boolean isJs();

    boolean isCss();

    byte[] getBytes();

    long getLastModified();

    boolean checkModified();

    List<String> findRequiresTags();

}
