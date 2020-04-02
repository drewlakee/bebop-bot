package telegram;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import telegram.utils.MessageKeysParser;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class MessageKeysParserTest {

    @Test
    public void testMessageParse() {
        int pairCount = ThreadLocalRandom.current().nextInt(100);
        HashMap<String, String> sourceMap = new HashMap<>();

        String key;
        String value;
        for (int i = 0; i < pairCount; i++) {
            key = RandomStringUtils.randomAlphabetic(10) + i;
            value = RandomStringUtils.random(10, " abc123").trim();
            sourceMap.put(key, value);
        }

        StringBuilder messageBody = new StringBuilder();
        sourceMap.forEach((keyMap, valueMap) -> messageBody.append(keyMap).append(": ").append(valueMap).append("\n"));

        HashMap<String, String> parsedMap = MessageKeysParser.parseMessageKeysBody(messageBody.toString());

        Assert.assertEquals(sourceMap, parsedMap);
    }
}
