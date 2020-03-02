package telegram.commands.abstractions;

public abstract class BotCommand {

    private final String commandName;

    protected BotCommand(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
