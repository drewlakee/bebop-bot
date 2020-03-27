package telegram.utils;

import java.util.HashMap;
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
    private final static Pattern keyPattern = Pattern.compile("[\\w\\p{IsCyrillic}].+(?=:)");

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

    /**
     *  Message body parser
     *
     *  Example:
     *          textBody = "key1: value1\nkey2: some value2\nkey3: some value3 (not used value)"
     *
     * @param textBody
     * @return @see HashMap<String, String>
     */
    private static HashMap<String, String> parseMessageKeysBody(String textBody) {
        String[] lines = textBody.split("\n");
        HashMap<String, String> map = new HashMap<>();

        Matcher keyMatcher;
        Matcher valueMatcher;
        for (String line : lines) {
            keyMatcher = keyPattern.matcher(line);
            valueMatcher = valuePattern.matcher(line);

            if (keyMatcher.find() && valueMatcher.find())
                map.put(keyMatcher.group(), valueMatcher.group().strip());
        }

        return map;
    }
}
