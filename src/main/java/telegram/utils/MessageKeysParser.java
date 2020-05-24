package telegram.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageKeysParser {

    /**
     * Pattern for keys matching in message body
     *
     * Example:
     *          text = "some_key: some value"
     *          pattern will match string "some_key"
     */
    private final static Pattern keyPattern = Pattern.compile("[\\w\\p{IsCyrillic}]*.(?=:)");

    /**
     * Pattern for value matching in message body
     *
     * Example:
     *          1. text = "some_key: some value"
     *          pattern will match string " some value", but have whitespace on the left side.
     *
     *          2. text = "some_key: value0 value1 (hello there)"
     *          pattern will match string " value0 value1 ", but have whitespaces.
     */
    private final static Pattern valuePattern = Pattern.compile("(?<=:)((\\s*)?([\\w\\p{IsCyrillic}-+]*))*");

    public static Map<String, String> parseMessageKeysBody(String textBody) {
        String[] lines = textBody.split("\n");
        Map<String, String> map = new HashMap<>();

        for (String line : lines) {
            Matcher keyMatcher = keyPattern.matcher(line);
            Matcher valueMatcher = valuePattern.matcher(line);

            if (keyMatcher.find() && valueMatcher.find()) {
                String key = keyMatcher.group();
                String value = valueMatcher.group().strip();

                if (!key.isEmpty() && !value.isEmpty())
                    map.put(key, value);
            }
        }

        return map;
    }
}
