package telegram;

import app.Environment;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot {

    private static Bot bot;

    public static void run() {
        if (isEmpty()) {
            ApiContextInitializer.init();
            initBot();

            TelegramBotsApi botsApi = new TelegramBotsApi();
            try {
                botsApi.registerBot(bot);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initBot() {
        if (isEmpty()) {
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

            bot = new Bot(customOptions);
        }
    }

    public static Bot instance() {
        if (isEmpty())
            initBot();

        return bot;
    }

    private static boolean isEmpty() {
        return bot == null;
    }
}
