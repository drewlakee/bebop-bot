package telegram;

import app.AppEnvironmentProperties;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.commands.CommandsPool;
import telegram.commands.MyGroupsCommand;
import telegram.commands.RandomCommand;
import telegram.commands.StatusCommand;

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

        if (AppEnvironmentProperties.containsProperty("bot_threads")) {
            customOptions.setMaxThreads(Integer.parseInt(AppEnvironmentProperties.getAppProperty("bot_threads")));
        } else
            customOptions.setMaxThreads(1);

        if (AppEnvironmentProperties.containsProperty("bot_proxy_type") &&
                AppEnvironmentProperties.containsProperty("bot_proxy_host") &&
                AppEnvironmentProperties.containsProperty("bot_proxy_port")) {
            customOptions.setProxyType(DefaultBotOptions.ProxyType.valueOf(AppEnvironmentProperties.getAppProperty("bot_proxy_type")));
            customOptions.setProxyHost(AppEnvironmentProperties.getAppProperty("bot_proxy_host"));
            customOptions.setProxyPort(Integer.parseInt(AppEnvironmentProperties.getAppProperty("bot_proxy_port")));
        }

        return customOptions;
    }
}
