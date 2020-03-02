package telegram;

import app.Environment;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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
        if (isHostChat(update))
            if (update.hasMessage() && update.getMessage().hasText())
                handleReceivedMessage(update.getMessage());
            else if (update.hasCallbackQuery())
                handleReceivedCallbackQuery(update.getCallbackQuery());
    }

    private boolean isHostChat(Update update) {
        return update.getMessage().getChatId() == Long.parseLong(Environment.PROPERTIES.get("host_chat_id").toString());
    }

    private void handleReceivedMessage(Message message) {
        String handleCommand = message.getText();

        switch (handleCommand) {
            case "/random":
                CommandsPool.handleCommand("/random", this, message);
                break;
            case "/status":
                CommandsPool.handleCommand("/status", this, message);
                break;
            default:
                break;
        }
    }

    private void handleReceivedCallbackQuery(CallbackQuery callbackQuery) {

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
