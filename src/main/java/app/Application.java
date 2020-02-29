package app;

import telegram.TelegramBot;
import vk.services.VkFileLoader;

public class Application {

    public static void main(String[] args) {
        VkFileLoader.loadVkGroups();
        TelegramBot.run();
    }
}
