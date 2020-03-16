package telegram;

import app.AppEnvironment;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.commands.*;

public class TelegramBot {

    public static void run() {
        ApiContextInitializer.init();
        DefaultBotOptions customOptions = initializeCustomOptions();
        Bot bot = new Bot(customOptions);
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            CommandsPool.register(new MyGroupsCommand());
            CommandsPool.register(new RandomCommand());
            CommandsPool.register(new StatusCommand());
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static DefaultBotOptions initializeCustomOptions() {
        DefaultBotOptions customOptions = new DefaultBotOptions();

        if (AppEnvironment.containsKey("bot_threads")) {
            customOptions.setMaxThreads(Integer.parseInt(AppEnvironment.getAppProperty("bot_threads")));
        } else
            customOptions.setMaxThreads(1);

        if (AppEnvironment.containsKey("bot_proxy_type") &&
                AppEnvironment.containsKey("bot_proxy_host") &&
                AppEnvironment.containsKey("bot_proxy_port")) {
            customOptions.setProxyType(DefaultBotOptions.ProxyType.valueOf(AppEnvironment.getAppProperty("bot_proxy_type")));
            customOptions.setProxyHost(AppEnvironment.getAppProperty("bot_proxy_host"));
            customOptions.setProxyPort(Integer.parseInt(AppEnvironment.getAppProperty("bot_proxy_port")));
        }

        return customOptions;
    }
}
