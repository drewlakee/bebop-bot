package telegram.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ResponseMessageDispatcher {

    public static void send(AbsSender sender, SendMessage sendMessage) {
        try {
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void send(AbsSender sender, SendPhoto photoMessage) {
        try {
            sender.execute(photoMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void send(AbsSender sender, EditMessageMedia editMessageMedia) {
        try {
            sender.execute(editMessageMedia);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void send(AbsSender sender, EditMessageText editMessageText) {
        try {
            sender.execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void send(AbsSender sender, DeleteMessage deleteMessage) {
        try {
            sender.execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
