package github.drewlakee.telegram.commands.devs;

import github.drewlakee.telegram.commands.BotCommand;
import github.drewlakee.telegram.commands.handlers.CallbackQueryHandler;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class NotFoundCommand extends BotCommand implements MessageHandler, CallbackQueryHandler {

    private static final Logger log = LoggerFactory.getLogger(NotFoundCommand.class);

    public NotFoundCommand() {
        super("/undefined");
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText(String.format("'%s' is wrong command", message.getText()));

        try {
            sender.execute(response);
        } catch (TelegramApiException e) {
            log.error(getClass().getSimpleName() + ": Can't send response message: Cause " + e.getCause());
            e.printStackTrace();
        }
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        SendMessage response = new SendMessage();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setText(String.format("'%s' is wrong callback", callbackQuery.getData()));

        try {
            sender.execute(response);
        } catch (TelegramApiException e) {
            log.error(getClass().getSimpleName() + ": Can't send response message: Cause " + e.getCause());
            e.printStackTrace();
        }
    }
}
