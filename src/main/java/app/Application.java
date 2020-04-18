package app;

import telegram.TelegramBot;
import vk.services.VkGroupsConfiguration;

public class Application {

    public static void main(String[] args) {
        VkGroupsConfiguration.loadVkGroups();
        TelegramBot.run();
    }
}
