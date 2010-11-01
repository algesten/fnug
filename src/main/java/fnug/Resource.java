package fnug;

public interface Resource {

    String getPath();

    String getContentType();

    boolean isJs();

    boolean isCss();

    byte[] getBytes();

    long getLastModified();

    boolean checkModified();

}
