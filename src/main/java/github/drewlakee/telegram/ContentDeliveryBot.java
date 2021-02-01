package github.drewlakee.telegram;

import github.drewlakee.telegram.commands.PostCommand;
import github.drewlakee.telegram.commands.GroupsCommand;
import github.drewlakee.telegram.commands.singletons.CommandsPool;

import github.drewlakee.vk.domain.groups.VkGroupsCustodian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class ContentDeliveryBot {

    private static final Logger log = LoggerFactory.getLogger(ContentDeliveryBot.class);

    private final DefaultBotOptions options;

    private final PostCommand postCommand;
    private final GroupsCommand groupsCommand;

    @Autowired
    public ContentDeliveryBot(DefaultBotOptions options, PostCommand postCommand, GroupsCommand groupsCommand) {
        this.options = options;
        this.postCommand = postCommand;
        this.groupsCommand = groupsCommand;
    }

    public void run() {
        ApiContextInitializer.init();
        ContentDeliveryHandler bot = new ContentDeliveryHandler(this.options);
        TelegramBotsApi botsApi = new TelegramBotsApi();

        CommandsPool.register(groupsCommand);
        CommandsPool.register(postCommand);

        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            log.error(String.format("%s: Bot launch fail", this.getClass().getSimpleName()));
            e.printStackTrace();
        }
    }

}
