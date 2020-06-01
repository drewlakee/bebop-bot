package github.drewlakee.app;

import github.drewlakee.telegram.TelegramBot;
import github.drewlakee.vk.singletons.VkGroupPool;
import github.drewlakee.vk.domain.groups.VkGroupsConfiguration;
import github.drewlakee.vk.domain.groups.VkGroupsFileConfiguration;

public class Application {

    public static void main(String[] args) {
        VkGroupsConfiguration vkGroupsConfiguration = new VkGroupsFileConfiguration();
        VkGroupPool.add(vkGroupsConfiguration.loadGroups());
        TelegramBot.run();
    }
}
