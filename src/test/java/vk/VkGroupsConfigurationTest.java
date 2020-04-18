package vk;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import vk.domain.groups.GroupObjective;
import vk.domain.groups.VkCustomGroup;
import vk.services.VkGroupsConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class VkGroupsConfigurationTest {

    @Test
    public void testGroupLoadingFromFile() throws IOException {
        GroupObjective groupObjective = GroupObjective.values()[ThreadLocalRandom.current().nextInt(GroupObjective.values().length)];
        String name = RandomStringUtils.randomAlphabetic(10);
        int groupId = ThreadLocalRandom.current().nextInt(-100000, 100000);
        String url = "https://vk.com/id" + groupId;
        String[] groupAttributes = new String[] {
                "<" + groupObjective.toString() + ">",
                "<" + name + ">",
                "<" + groupId + ">",
                "<" + url + ">"
        };

        File testFile = new File("TestFile.txt");
        Writer in = new FileWriter(testFile);
        in.write(String.join(" ", groupAttributes));
        in.close();

        HashMap<String, VkCustomGroup> groupsFromFile = VkGroupsConfiguration.loadGroups(testFile);
        VkCustomGroup group = groupsFromFile.get(name);
        boolean isFileDeleted = testFile.delete();

        Assert.assertTrue(isFileDeleted);
        Assert.assertEquals(groupObjective, group.getGroupObjective());
        Assert.assertEquals(name, group.getName());
        Assert.assertEquals(groupId, group.getId().intValue());
        Assert.assertEquals(url, group.getUrl());
    }
}
