package telegram.commands.handlers;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
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

    protected void send(AbsSender sender, PartialBotApiMethod method) {
        try {
            if (method instanceof SendPhoto)
                sender.execute((SendPhoto) method);

            if (method instanceof EditMessageMedia)
                sender.execute((EditMessageMedia) method);
        } catch (TelegramApiException e) {
            e.getStackTrace();
        }
    }

    protected void send(AbsSender sender, BotApiMethod method) {
        try {
            sender.execute(method);
        } catch (TelegramApiException e) {
            e.getStackTrace();
        }
    }
}
