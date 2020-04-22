package app;

import telegram.TelegramBot;
import vk.services.VkGroupsConfigurationService;

public class Application {

    public static void main(String[] args) {
        VkGroupsConfigurationService.loadVkGroups();
        TelegramBot.run();
    }
}
