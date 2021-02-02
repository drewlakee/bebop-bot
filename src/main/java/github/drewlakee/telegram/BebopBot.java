package github.drewlakee.telegram;

import github.drewlakee.telegram.commands.GroupsCommand;
import github.drewlakee.telegram.commands.PostCommand;
import github.drewlakee.telegram.commands.singletons.CommandsPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class BebopBot {

    private static final Logger log = LoggerFactory.getLogger(BebopBot.class);

    private final BebopBotHandler handler;

    private final PostCommand postCommand;
    private final GroupsCommand groupsCommand;

    @Autowired
    public BebopBot(BebopBotHandler handler, PostCommand postCommand, GroupsCommand groupsCommand) {
        this.handler = handler;
        this.postCommand = postCommand;
        this.groupsCommand = groupsCommand;
    }

    public void run() {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        CommandsPool.register(groupsCommand);
        CommandsPool.register(postCommand);

        try {
            botsApi.registerBot(handler);
        } catch (TelegramApiException e) {
            log.error(String.format("%s: Bot launch fail", this.getClass().getSimpleName()));
            e.printStackTrace();
        }
    }

}
