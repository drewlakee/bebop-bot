package telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            new Thread(
                    () -> handleMessage(update.getMessage())
            ).start();
        }
    }

    private void handleMessage(Message message) {
        try {
            execute(new SendMessage().setChatId(message.getChatId()).setText(Thread.currentThread().getName()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return System.getProperty("botUsername");
    }

    @Override
    public String getBotToken() {
        return System.getProperty("token");
    }
}
