package fnug.resource;

import com.googlecode.jslint4java.JSLintResult;

/**
 * Tagging interface for entities that has a JSLint result.
 * 
 * @author Martin Algesten
 * 
 */
public interface HasJSLintResult {

    /**
     * Returns the jslint result. If the entity does not have a lint result,
     * null is returned.
     */
    JSLintResult getJSLintResult();

}
