package telegram.commands;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import telegram.commands.abstractions.BotCommand;
import telegram.commands.abstractions.CallbackQueryHandler;
import telegram.commands.abstractions.MessageHandler;

import java.util.HashMap;

public class CommandsPool {

    private static HashMap<String, BotCommand> pool;

    public static void register(BotCommand command) {
        if (isEmpty())
            pool = new HashMap<>();

        pool.put(command.getCommandName(), command);
    }

    public static void handleCommand(String command, AbsSender sender, Message message) {
        handleCommand(command, sender, message, null);
    }

    public static void handleCommand(String command, AbsSender sender, CallbackQuery callbackQuery) {
        handleCommand(command, sender, null, callbackQuery);
    }

    private static void handleCommand(String command, AbsSender sender, Message message, CallbackQuery callbackQuery) {
        try {
            if (message == null)
                ((CallbackQueryHandler) pool.get(command)).handle(sender, callbackQuery);
            else
                ((MessageHandler) pool.get(command)).handle(sender, message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static boolean isEmpty() {
        return pool == null;
    }
}
