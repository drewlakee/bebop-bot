package telegram.commands.singletons;

import telegram.commands.callbacks.Callback;
import telegram.commands.statics.Commands;

import java.util.HashMap;
import java.util.List;

public class CallbacksPool {

    private static HashMap<String, String> pool;

    public static void register(Callback... callbacks) {
        if (isEmpty())
            pool = new HashMap<>();

        for (Callback callback : callbacks) {
            pool.put(callback.getName(), callback.getCommand());
        }
    }

    public static void register(List<Callback> callbacks) {
        for (Callback callback : callbacks) {
            register(callback);
        }
    }

    public static String defineHandler(String data) {
        String command = pool.get(data);
        return command == null ? Commands.UNKNOWN : command;
    }

    private static boolean isEmpty() {
        return pool == null;
    }
}
