package github.drewlakee.telegram;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import github.drewlakee.telegram.utils.MessageKeysParser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MessageKeysParserTest {

    @Test
    public void testMessageParse() {
        int pairCount = ThreadLocalRandom.current().nextInt(100);
        HashMap<String, String> sourceMap = new HashMap<>();

        for (int i = 0; i < pairCount; i++) {
            String key = RandomStringUtils.randomAlphabetic(10) + i;
            String value = RandomStringUtils.random(10, " abc123").trim();

            sourceMap.put(key, value);
        }

        StringBuilder messageBody = new StringBuilder();
        sourceMap.forEach((keyMap, valueMap) -> messageBody.append(keyMap).append(": ").append(valueMap).append("\n"));
        Map<String, String> parsedMap = MessageKeysParser.parseMessageKeysBody(messageBody.toString());

        Assert.assertEquals(sourceMap, parsedMap);
    }
}
