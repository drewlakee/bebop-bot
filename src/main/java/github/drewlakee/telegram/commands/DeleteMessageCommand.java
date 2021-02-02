package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.handlers.CallbackQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class DeleteMessageCommand extends BotCommand implements CallbackQueryHandler {

    private static final Logger log = LoggerFactory.getLogger(DeleteMessageCommand.class);

    public DeleteMessageCommand() {
        super("/deleteMessage");
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        try {
            sender.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(String.format("%s message error: can't execute", deleteMessage.toString()));
            e.printStackTrace();
        }
    }
}
