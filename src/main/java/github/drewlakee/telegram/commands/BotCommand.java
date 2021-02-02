package github.drewlakee.telegram.commands;

import java.util.Objects;

public abstract class BotCommand {

    protected final String commandName;

    protected BotCommand(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotCommand that = (BotCommand) o;
        return Objects.equals(commandName, that.commandName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandName);
    }

    @Override
    public String toString() {
        return "BotCommand{" +
                "commandName='" + commandName + '\'' +
                '}';
    }
}
