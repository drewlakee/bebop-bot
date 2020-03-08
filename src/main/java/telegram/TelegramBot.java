package telegram;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.commands.CommandsPool;
import telegram.commands.RandomCommand;
import telegram.commands.StatusCommand;

public class TelegramBot {

    public static void run() {
        ApiContextInitializer.init();
        DefaultBotOptions customOptions = initializeCustomOptions();
        Bot bot = new Bot(customOptions);
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            CommandsPool.register(new RandomCommand());
            CommandsPool.register(new StatusCommand());
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static DefaultBotOptions initializeCustomOptions() {
        DefaultBotOptions customOptions = new DefaultBotOptions();

        if (System.getenv().containsKey("bot_threads")) {
            customOptions.setMaxThreads(Integer.parseInt(System.getenv("bot_threads")));
        } else
            customOptions.setMaxThreads(4);

        if (System.getenv().containsKey("bot_proxy_type") &&
                System.getenv().containsKey("bot_proxy_host") &&
                System.getenv().containsKey("bot_proxy_port")) {
            customOptions.setProxyType(DefaultBotOptions.ProxyType.valueOf(System.getenv("bot_proxy_type")));
            customOptions.setProxyHost(System.getenv("bot_proxy_host"));
            customOptions.setProxyPort(Integer.parseInt(System.getenv("bot_proxy_port")));
        }

        return customOptions;
    }
}
