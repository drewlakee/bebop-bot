package app;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AppEnvironment {

    private final static Set<String> neededProperties = Set.of(
            "bot_token",
            "bot_username",
            "host_username",
            "vk_token",
            "vk_user_id"
    );

    private static Map<String, String> appProperties = new HashMap<>();

    static {
        Set<String> envProperties = System.getenv().keySet()
                .stream()
                .filter(neededProperties::contains)
                .collect(Collectors.toSet());

        if (envProperties.equals(neededProperties)) {
            appProperties = System.getenv();
        } else
            System.getProperties().forEach((key, value) -> appProperties.put((String) key, (String) value));
    }

    public static String getAppProperty(String property) {
        return appProperties.get(property);
    }

    public static boolean containsKey(String property) {
        return appProperties.containsKey(property);
    }
}
