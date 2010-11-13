package fnug.config;

import java.util.regex.Pattern;

import fnug.resource.Bundle;
import fnug.resource.JsCompressor;
import fnug.resource.Resource;

/**
 * Config for a {@link Bundle}.
 * 
 * @author Martin Algesten
 * 
 */
public interface BundleConfig {

    /**
     * Default value for {@link #checkModified()}. Set to {@value} .
     */
    static final boolean DEFAULT_CHECK_MODIFIED = true;

    /**
     * Default value for {@link #jsLint()}. Set to {@value}
     */
    static final boolean DEFAULT_JS_LINT = true;

    /**
     * Returns the resource responsible for this configuration.
     * 
     * @return associated resource.
     */
    Resource configResource();

    /**
     * Name of the bundle.
     * 
     * @return Bundle name. Must match {@link Bundle#BUNDLE_ALLOWED_CHARS}.
     */
    String name();

    /**
     * Base path of the bundle. See {@link Resource#getBasePath()}.
     * 
     * @return bundle base path
     */
    String basePath();

    /**
     * Additional patterns to match for this bundle.
     * 
     * @return additional match patterns.
     */
    Pattern[] matches();

    /**
     * Tells if jslint is turned on for this bundle.
     * 
     * @return True if jslint is turned on.
     */
    boolean jsLint();

    /**
     * Tells if we are to check modified dates in this bundle.
     * 
     * @return True if we are to check modified dates.
     */
    boolean checkModified();

    /**
     * Additional compilation arguments passed to {@link JsCompressor}.
     * 
     * @return arguments passed to the javascript compressor.
     */
    String[] jsCompileArgs();

    /**
     * Files that comprises this bundle. Additional dependencies will be
     * discovered using {@link Resource#findRequiresTags()}.
     * 
     * @return Starting point files of this bundle.
     */
    String[] files();

}
