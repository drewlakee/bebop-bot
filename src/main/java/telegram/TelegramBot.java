package telegram;

import app.Environment;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot {

    public static void run() {
        ApiContextInitializer.init();
        Bot bot = initializeCustomBot();

        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static Bot initializeCustomBot() {
        DefaultBotOptions customOptions = new DefaultBotOptions();

        if (Environment.PROPERTIES.containsKey("bot_threads")) {
            customOptions.setMaxThreads(Integer.parseInt(Environment.PROPERTIES.get("bot_threads").toString()));
        } else
            customOptions.setMaxThreads(4);

        if (Environment.PROPERTIES.containsKey("bot_proxy_type") &&
                Environment.PROPERTIES.containsKey("bot_proxy_host") &&
                Environment.PROPERTIES.containsKey("bot_proxy_port")) {
            customOptions.setProxyType(DefaultBotOptions.ProxyType.valueOf(Environment.PROPERTIES.get("bot_proxy_type").toString()));
            customOptions.setProxyHost(Environment.PROPERTIES.get("bot_proxy_host").toString());
            customOptions.setProxyPort(Integer.parseInt(Environment.PROPERTIES.get("bot_proxy_port").toString()));
        }

        return new Bot(customOptions);
    }
}
