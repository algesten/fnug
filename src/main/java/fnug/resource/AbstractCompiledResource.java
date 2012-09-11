package fnug.resource;


public abstract class AbstractCompiledResource extends DefaultResource implements HasBundle {

    private Bundle bundle;

    private volatile byte[] compiledBytes;


    /**
     * Constructs setting base path and path.
     * 
     * @param basePath
     *            The base path of the resource. See {@link #getBasePath()}.
     * @param path
     *            The path of the resource. See {@link #getPath()}.
     */
    public AbstractCompiledResource(String basePath, String path) {
        super(basePath, path);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getBundle() {
        return bundle;
    }


    /**
     * (Optionally) sets the bundle.
     * 
     * @param bundle
     *            the bundle to associate resource with.
     */
    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }


    /**
     * Returns the compiled bytes.
     * 
     * {@inheritDoc}
     */
    @Override
    public byte[] getBytes() {

        if (compiledBytes == null) {
            synchronized (this) {
                if (compiledBytes == null) {

                    byte[] sourceBytes = super.getBytes();

                    compiledBytes = compile(sourceBytes);

                }
            }
        }


        return compiledBytes;

    }


    /**
     * Ditches the compiled bytes if the superclass indicates that modified has changed.
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean checkModified() {

        boolean result = super.checkModified();

        if (result) {
            compiledBytes = null;
        }

        return result;

    }


    /**
     * Subclasses must implement to do the compilation of this resource.
     * 
     * @param source
     *            the source bytes.
     * @return the compiled bytes`
     */
    protected abstract byte[] compile(byte[] source);

}
