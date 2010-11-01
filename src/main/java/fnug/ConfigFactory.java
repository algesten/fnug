package fnug;

import java.util.List;

public class ConfigFactory {

    private static JsonConfigParser jsonConfigParser = new JsonConfigParser();

    public static List<Config> read(Resource resource) {

        if (resource.isJs()) {
            return jsonConfigParser.parse(resource);
        }

        throw new IllegalArgumentException("Can't parse configs of content type: " + resource.getContentType());

    }

}
