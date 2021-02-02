package github.drewlakee.telegram;

import github.drewlakee.telegram.commands.GroupsCommand;
import github.drewlakee.telegram.commands.PostCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class BebopBot extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(BebopBot.class);

    private final PostCommand postCommand;
    private final GroupsCommand groupsCommand;

    @Autowired
    public BebopBot(DefaultBotOptions options, PostCommand postCommand, GroupsCommand groupsCommand) {
        super(options);
        this.postCommand = postCommand;
        this.groupsCommand = groupsCommand;
    }

    @Override
    public void onUpdateReceived(Update update) {
        exe.submit(() -> handleReceivedEvent(update));
    }

    private void handleReceivedEvent(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleReceivedMessage(update.getMessage());
        }

        if (update.hasCallbackQuery()) {
            handleReceivedCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleReceivedMessage(Message message) {
        String handleCommand = message.getText();

        switch (handleCommand) {
            case PostCommand.COMMAND_NAME -> postCommand.handle(this, message);
            case GroupsCommand.COMMAND_NAME -> groupsCommand.handle(this, message);
        }
    }

    private void handleReceivedCallbackQuery(CallbackQuery callbackQuery) {
        String handleCommand = callbackQuery.getData();

        if (handleCommand.startsWith(PostCommand.COMMAND_NAME)) {
            handleCommand = PostCommand.COMMAND_NAME;
        }

        switch (handleCommand) {
            case PostCommand.COMMAND_NAME -> postCommand.handle(this, callbackQuery);
            case "delete_message" -> deleteMessage(callbackQuery);
        }
    }

    private void deleteMessage(CallbackQuery callbackQuery) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(String.format("%s message error: can't execute", deleteMessage.toString()));
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return System.getenv("bot_username");
    }

    @Override
    public String getBotToken() {
        return System.getenv(("bot_token"));
    }
}
