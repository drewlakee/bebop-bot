package telegram.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import telegram.commands.handlers.BotCommand;
import telegram.commands.handlers.MessageHandler;
import telegram.commands.statics.Commands;
import telegram.fun.Greetings;

public class StatusCommand extends BotCommand implements MessageHandler {

    public StatusCommand() {
        super(Commands.STATUS);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(Greetings.getRandomGreeting(message.getChat().getUserName()));
        send(sender, sendMessage);
    }
}
