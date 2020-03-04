package telegram.commands.handlers;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

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
        } catch (TelegramApiRequestException e) {
            e.getApiResponse();
        } catch (TelegramApiException e) {
            e.getStackTrace();
        }
    }
}
