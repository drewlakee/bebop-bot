package telegram;

import app.Environment;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class HandlerBot extends TelegramLongPollingBot {

    public HandlerBot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public void onUpdateReceived(Update update) {
        exe.submit(() -> handleUpdate(update));
    }

    private void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText("ping" + Thread.currentThread().getName());
            sendMessage.setChatId(update.getMessage().getChatId());
            executeSendMessage(sendMessage);
        }
    }

    public void executeSendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
