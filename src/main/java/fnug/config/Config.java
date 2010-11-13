package fnug.config;

/**
 * Product of {@link ConfigParser#parse(fnug.resource.Resource)}. One of these
 * objects is created for each config file parsed.
 * 
 * @author Martin Algesten
 * 
 */
public interface Config {

    /**
     * The {@link BundleConfig} found in the config file.
     * 
     * @return the found bundle configs.
     */
    BundleConfig[] getBundleConfigs();

}
