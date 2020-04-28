package vk;

import com.google.common.collect.ComparisonChain;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import vk.domain.groups.VkGroupObjective;
import vk.domain.groups.VkCustomGroup;
import vk.services.VkGroupsConfigurationService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class VkGroupsConfigurationTest {

    @Test
    public void testGroupLoadingFromFile() throws IOException {
        VkGroupObjective vkGroupObjective = VkGroupObjective.values()[ThreadLocalRandom.current().nextInt(VkGroupObjective.values().length)];
        String name = RandomStringUtils.randomAlphabetic(10);
        int groupId = ThreadLocalRandom.current().nextInt(-100000, 100000);
        String url = "https://vk.com/id" + groupId;
        String[] groupAttributes = new String[] {
                "<" + vkGroupObjective.toString() + ">",
                "<" + name + ">",
                "<" + groupId + ">",
                "<" + url + ">"
        };

        File testFile = new File("TestFile.txt");
        testFile.deleteOnExit();
        Writer in = new FileWriter(testFile);
        in.write(String.join(" ", groupAttributes));
        in.close();

        HashMap<String, VkCustomGroup> groupsFromFile = VkGroupsConfigurationService.loadGroups(testFile);
        VkCustomGroup group = groupsFromFile.get(name);

        Assert.assertEquals(0, ComparisonChain.start()
                .compare(vkGroupObjective, group.getVkGroupObjective())
                .compare(name, group.getName())
                .compare(groupId, group.getId().intValue())
                .compare(url, group.getUrl())
                .result());
    }
}
