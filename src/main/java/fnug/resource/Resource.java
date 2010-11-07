package fnug.resource;

import java.util.List;

public interface Resource extends HasLastModifiedBytes {

    String getBasePath();
    
    String getPath();
    
    String getFullPath();

    String getContentType();

    boolean isJs();

    boolean isCss();

    byte[] getBytes();

    long getLastModified();

    boolean checkModified();

    List<String> findRequiresTags();

}
