package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.handlers.BotCommand;
import github.drewlakee.telegram.commands.handlers.CallbackQueryHandler;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class ConstructOnlyAudiosCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {
    protected ConstructOnlyAudiosCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {

    }

    @Override
    public void handle(AbsSender sender, Message message) {

    }
}
