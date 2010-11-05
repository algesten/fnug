package fnug.config;

import fnug.resource.Resource;


public interface ConfigParser {

    Config parse(Resource res);

}
