package telegram.commands.abstractions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface EditContentHandler {

    default void sendEditContent(AbsSender sender, BotApiMethod editContent) {
        try {
            sender.execute(editContent);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
