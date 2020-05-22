package telegram.commands.callbacks;

import lombok.Getter;

@Getter
public class Callback {

    private final String name;
    private final String command;

    public Callback(String name, String command) {
        this.name = name;
        this.command = command;
    }

    @Override
    public String toString() {
        return "Callback{" +
                "name='" + name + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
