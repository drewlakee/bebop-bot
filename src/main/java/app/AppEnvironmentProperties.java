package app;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  App properties definer and collector
 *
 *  Made for easier debug with heroku deploy (needed System.getenv()) and local (needed System.getProperty()).
 */
public class AppEnvironmentProperties {

    private final static Set<String> neededProperties = Set.of(
            "bot_token",
            "bot_username",
            "host_username",
            "vk_token",
            "vk_user_id"
    );

    private static Map<String, String> appProperties = defineAppProperties();

    private static Map<String, String> defineAppProperties() {
        Map<String, String> properties = new HashMap<>();

        Set<String> envProperties = System.getenv().keySet()
                .stream()
                .filter(neededProperties::contains)
                .collect(Collectors.toSet());

        if (envProperties.equals(neededProperties)) {
            properties = System.getenv();
        } else {
            Map<String, String> finalProperties = properties;
            System.getProperties().keySet().forEach(key -> finalProperties.put(key.toString(), System.getProperty(key.toString())));
        }

        return properties;
    }

    public static String getAppProperty(String property) {
        return appProperties.get(property);
    }

    public static boolean containsProperty(String property) {
        return appProperties.containsKey(property);
    }
}
