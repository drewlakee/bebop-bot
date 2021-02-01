package github.drewlakee.telegram;

import github.drewlakee.telegram.commands.PostCommand;
import github.drewlakee.telegram.commands.GroupsCommand;
import github.drewlakee.telegram.commands.callbacks.HandlerBotCallback;
import github.drewlakee.telegram.commands.singletons.CommandsPool;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class ContentDeliveryHandler extends TelegramLongPollingBot {

    public ContentDeliveryHandler(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public void onUpdateReceived(Update update) {
        exe.submit(() -> handleUpdate(update));
    }

    private void handleUpdate(Update update) {
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
            case PostCommand.COMMAND_NAME:
                CommandsPool.handleCommand(PostCommand.COMMAND_NAME, this, message);
                break;
            case GroupsCommand.COMMAND_NAME:
                CommandsPool.handleCommand(GroupsCommand.COMMAND_NAME, this, message);
        }
    }

    private void handleReceivedCallbackQuery(CallbackQuery callbackQuery) {
        String handleCommand = callbackQuery.getData();

        if (handleCommand.startsWith(PostCommand.COMMAND_NAME)) {
            handleCommand = PostCommand.COMMAND_NAME;
        }

        if (handleCommand.equals(HandlerBotCallback.DELETE_MESSAGE.name())) {
           handleCommand = "delete_message";
        }

        switch (handleCommand) {
            case PostCommand.COMMAND_NAME:
                CommandsPool.handleCommand(PostCommand.COMMAND_NAME, this, callbackQuery);
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
