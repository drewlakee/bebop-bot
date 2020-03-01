package telegram.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import telegram.fun.Greetings;

public class StatusCommand extends AbstractCommand {

    public static void sendStatus(AbsSender sender, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(Greetings.getRandomGreeting(message.getChat().getUserName()));
        execute(sender, sendMessage);
    }
}
