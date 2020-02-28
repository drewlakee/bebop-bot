package telegram.services;

import app.Environment;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import telegram.TelegramBot;

public class SendService {

    public static void sendVkAuthNotification() {
        SendMessage notification = new SendMessage();
        StringBuilder message = new StringBuilder();

        message.append("Слыш, скинь вк токен:");
        message.append("\t\t");
        message.append(Environment.PROPERTIES.get("oauth_link").toString());

        notification.setText(message.toString());
        notification.setChatId(Environment.PROPERTIES.get("chat_id").toString());
        TelegramBot.instance().executeSendMessage(notification);
    }
}
