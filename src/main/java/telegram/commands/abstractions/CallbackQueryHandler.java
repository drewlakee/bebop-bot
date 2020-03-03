package telegram.commands.abstractions;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CallbackQueryHandler {

    void handle(AbsSender sender, CallbackQuery callbackQuery);

    default void sendAnswerCallbackQueryMessage(AbsSender sender, AnswerCallbackQuery answerCallbackQuery) {
        try {
            sender.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    default void sendEditContent(AbsSender sender, BotApiMethod editMessageReplyMarkup) {
        try {
            sender.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
