package telegram.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.commands.handlers.BotCommand;
import telegram.commands.handlers.MessageHandler;
import telegram.fun.Greetings;

public class StatusCommand extends BotCommand implements MessageHandler {

    public StatusCommand() {
        super("/status");
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(Greetings.getRandomGreeting(message.getChat().getUserName()));

        try {
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
