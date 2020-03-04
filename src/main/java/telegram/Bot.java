package telegram;

import app.Environment;
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
        String hostUsername = Environment.PROPERTIES.getProperty("host_username");

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
            recentMessage = Callbacks.RANDOM_CHOOSE_PHOTO;

        switch (recentMessage) {
            case Callbacks.RANDOM_ASK_CHOOSE_PHOTO:
            case Callbacks.RANDOM_CHOOSE_GROUP:
            case Callbacks.RANDOM_CHOOSE_PHOTO:
                CommandsPool.handleCommand(Commands.RANDOM, this, callbackQuery);
                break;
        }
    }

    @Override
    public String getBotUsername() {
        return Environment.PROPERTIES.get("bot_username").toString();
    }

    @Override
    public String getBotToken() {
        return Environment.PROPERTIES.get(("bot_token")).toString();
    }
}
