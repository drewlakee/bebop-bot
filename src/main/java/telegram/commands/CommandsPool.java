package telegram.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import telegram.commands.handlers.BotCommand;
import telegram.commands.handlers.CallbackQueryHandler;
import telegram.commands.handlers.MessageHandler;

import java.util.HashMap;
import java.util.Set;

public class CommandsPool {

    private static final Logger log = LoggerFactory.getLogger(CommandsPool.class);

    private static HashMap<String, BotCommand> pool;

    public static void register(BotCommand command) {
        if (isEmpty())
            pool = new HashMap<>();

        pool.put(command.getCommandName(), command);
    }

    public static void handleCommand(String command, AbsSender sender, CallbackQuery method) {
        try {
            ((CallbackQueryHandler) pool.get(command)).handle(sender, method);
        } catch (NullPointerException e) {
            log.error("ERROR: COMMAND HANDLER NOT FOUND FOR CALLBACK QUERY!!!");
            e.printStackTrace();
        }
    }

    public static void handleCommand(String command, AbsSender sender, Message method) {
        try {
            ((MessageHandler) pool.get(command)).handle(sender, method);
        } catch (NullPointerException e) {
            log.error("ERROR: COMMAND HANDLER NOT FOUND FOR MESSAGE!!!");
            e.printStackTrace();
        }
    }

    public static Set<String> getRegisteredCommandsSet() {
        return pool.keySet();
    }

    private static boolean isEmpty() {
        return pool == null;
    }
}
