package fnug.servlet;

import fnug.resource.HasLastModifiedBytes;

public interface ToServe extends HasLastModifiedBytes {

    String getContentType();

    boolean futureExpires();

}