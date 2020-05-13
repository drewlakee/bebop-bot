package telegram.commands.callbacks;

public class Callback {

    private final String name;
    private final String command;

    public Callback(String name, String command) {
        this.name = name;
        this.command = command;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "Callback{" +
                "name='" + name + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
