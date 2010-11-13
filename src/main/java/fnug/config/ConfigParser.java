package fnug.config;

import fnug.resource.Resource;

/**
 * Interface for config parsers.
 * 
 * @author Martin Algesten
 * 
 */
public interface ConfigParser {

    /**
     * Parse the given resource into a config.
     * 
     * @param res
     *            the resource to parse.
     * @return the config.
     * @throws ConfigParseException
     *             if the resource parsing fails.
     */
    Config parse(Resource res) throws ConfigParseException;

}
