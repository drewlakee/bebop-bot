package telegram.commands.handlers;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public abstract class BotCommand {

    private final String commandName;

    protected BotCommand(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    protected void send(AbsSender sender, BotApiMethod apiMethod) {
        try {
            sender.execute(apiMethod);
        } catch (TelegramApiException e) {
            e.getStackTrace();
        }
    }
}
