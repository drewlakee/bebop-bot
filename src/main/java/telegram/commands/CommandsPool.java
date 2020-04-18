package telegram.commands;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import telegram.commands.handlers.BotCommand;
import telegram.commands.handlers.CallbackQueryHandler;
import telegram.commands.handlers.MessageHandler;

import java.util.HashMap;

public class CommandsPool {

    private static HashMap<String, BotCommand> pool;

    public static void register(BotCommand command) {
        if (isEmpty())
            pool = new HashMap<>();

        pool.put(command.getCommandName(), command);
    }

    public static void handleCommand(String command, AbsSender sender, Object method) {
        try {
            if (method instanceof CallbackQuery)
                ((CallbackQueryHandler) pool.get(command)).handle(sender, (CallbackQuery) method);

            if (method instanceof Message)
                ((MessageHandler) pool.get(command)).handle(sender, (Message) method);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static boolean isEmpty() {
        return pool == null;
    }
}
