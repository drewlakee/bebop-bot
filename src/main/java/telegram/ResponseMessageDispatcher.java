package telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ResponseMessageDispatcher {

    private final AbsSender sender;

    public ResponseMessageDispatcher(AbsSender sender) {
        this.sender = sender;
    }

    public void send(SendMessage sendMessage) {
        try {
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(SendPhoto photoMessage) {
        try {
            sender.execute(photoMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(EditMessageMedia editMessageMedia) {
        try {
            sender.execute(editMessageMedia);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(EditMessageText editMessageText) {
        try {
            sender.execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(DeleteMessage deleteMessage) {
        try {
            sender.execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
