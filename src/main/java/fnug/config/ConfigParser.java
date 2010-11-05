package fnug.config;

import fnug.Config;
import fnug.Resource;


public interface ConfigParser {

    Config parse(Resource res);

}
