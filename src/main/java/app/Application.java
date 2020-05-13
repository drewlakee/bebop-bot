package app;

import telegram.TelegramBot;
import vk.domain.groups.VkGroupPool;
import vk.domain.groups.VkGroupsConfiguration;
import vk.domain.groups.VkGroupsFileConfiguration;

public class Application {

    public static void main(String[] args) {
        VkGroupsConfiguration vkGroupsConfiguration = new VkGroupsFileConfiguration();
        VkGroupPool.add(vkGroupsConfiguration.loadGroups());

        TelegramBot.run();
    }
}
