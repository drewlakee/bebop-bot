package github.drewlakee.telegram;

import github.drewlakee.telegram.commands.callbacks.GlobalCallback;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import github.drewlakee.telegram.commands.singletons.CommandsPool;
import github.drewlakee.telegram.commands.statics.Commands;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class HandlerBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(HandlerBot.class);

    public HandlerBot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public void onUpdateReceived(Update update) {
        exe.submit(() -> handleUpdate(update));
    }

    private void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("[TELEGRAM BOT] User [{}] request: {}", update.getMessage().getFrom().getUserName(),
                    "[message text] " + update.getMessage().getText());

            handleReceivedMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            log.info("[TELEGRAM BOT] User [{}] request: {}", update.getCallbackQuery().getFrom().getUserName(),
                    "[callback data] " + update.getCallbackQuery().getData());

            handleReceivedCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleReceivedMessage(Message message) {
        String handleCommand = message.getText();

        switch (handleCommand) {
            case Commands.CONSTRUCT_ONLY_PHOTOS:
                CommandsPool.handleCommand(Commands.CONSTRUCT_ONLY_PHOTOS, this, message);
                break;
            case Commands.MY_GROUPS:
                CommandsPool.handleCommand(Commands.MY_GROUPS, this, message);
        }
    }

    private void handleReceivedCallbackQuery(CallbackQuery callbackQuery) {
        String handleCommand = callbackQuery.getData();

        if (handleCommand.startsWith(Commands.CONSTRUCT_ONLY_PHOTOS)) {
            handleCommand = Commands.CONSTRUCT_ONLY_PHOTOS;
        }

        if (handleCommand.equals(GlobalCallback.DELETE_MESSAGE.name())) {
           handleCommand = "delete_message";
        }

        switch (handleCommand) {
            case Commands.CONSTRUCT_ONLY_PHOTOS:
                CommandsPool.handleCommand(Commands.CONSTRUCT_ONLY_PHOTOS, this, callbackQuery);
                break;
            case "delete_message":
                deleteMessage(this, callbackQuery);
        }
    }

    private void deleteMessage(AbsSender sender, CallbackQuery callbackQuery) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        ResponseMessageDispatcher.send(sender, deleteMessage);
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
