package telegram;

import app.AppEnvironment;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.commands.Callbacks;
import telegram.commands.Commands;
import telegram.commands.CommandsPool;

public class Bot extends TelegramLongPollingBot {

    public Bot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public void onUpdateReceived(Update update) {
        exe.submit(() -> handleUpdate(update));
    }

    private void handleUpdate(Update update) {
        if (isHostRequest(update))
            if (update.hasMessage() && update.getMessage().hasText())
                handleReceivedMessage(update.getMessage());
            else if (update.hasCallbackQuery())
                handleReceivedCallbackQuery(update.getCallbackQuery());
    }

    private boolean isHostRequest(Update update) {
        String hostUsername = AppEnvironment.getAppProperty("host_username");

        if (update.hasMessage())
            return update.getMessage().getChat().getUserName().equals(hostUsername);
        else if (update.hasCallbackQuery())
            return update.getCallbackQuery().getMessage().getChat().getUserName().equals(hostUsername);

        return false;
    }

    private void handleReceivedMessage(Message message) {
        String handleCommand = message.getText();

        switch (handleCommand) {
            case Commands.RANDOM:
                CommandsPool.handleCommand(Commands.RANDOM, this, message);
                break;
            case Commands.STATUS:
                CommandsPool.handleCommand(Commands.STATUS, this, message);
                break;
        }
    }

    private void handleReceivedCallbackQuery(CallbackQuery callbackQuery) {
        String recentMessage = callbackQuery.getMessage().getText();

        if (recentMessage.startsWith("https") && recentMessage.endsWith("jpg"))
            recentMessage = Callbacks.CHOOSE_PHOTO;

        switch (recentMessage) {
            case Callbacks.ASK_CHOOSE_PHOTO:
            case Callbacks.CHOOSE_GROUP:
            case Callbacks.CHOOSE_PHOTO:
                CommandsPool.handleCommand(Commands.RANDOM, this, callbackQuery);
                break;
        }
    }

    @Override
    public String getBotUsername() {
        return AppEnvironment.getAppProperty("bot_username");
    }

    @Override
    public String getBotToken() {
        return AppEnvironment.getAppProperty(("bot_token"));
    }
}
