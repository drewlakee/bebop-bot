package telegram.commands.handlers;

public abstract class BotCommand {

    protected final String commandName;

    protected BotCommand(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
