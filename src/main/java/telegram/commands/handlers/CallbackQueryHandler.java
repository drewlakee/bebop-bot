package telegram.commands.handlers;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface CallbackQueryHandler {

    void handle(AbsSender sender, CallbackQuery callbackQuery);
}
