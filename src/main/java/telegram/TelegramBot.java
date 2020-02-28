package telegram;

import app.Environment;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.services.SendService;

public class TelegramBot {

    private static HandlerBot bot;

    public static void run() {
        if (isEmpty()) {
            ApiContextInitializer.init();
            initBot();

            TelegramBotsApi botsApi = new TelegramBotsApi();
            try {
                botsApi.registerBot(bot);
                SendService.sendVkAuthNotification();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initBot() {
        DefaultBotOptions options = new DefaultBotOptions();

        if (Environment.PROPERTIES.containsKey("bot_threads")) {
            options.setMaxThreads(Integer.parseInt(Environment.PROPERTIES.get("bot_threads").toString()));
        } else
            options.setMaxThreads(4);

        if (Environment.PROPERTIES.containsKey("bot_proxy_host") &&
                    Environment.PROPERTIES.containsKey("bot_proxy_port")) {
            options.setProxyType(DefaultBotOptions.ProxyType.valueOf(Environment.PROPERTIES.get("bot_proxy_type").toString()));
            options.setProxyHost(Environment.PROPERTIES.get("bot_proxy_host").toString());
            options.setProxyPort(Integer.parseInt(Environment.PROPERTIES.get("bot_proxy_port").toString()));
        }

        bot = new HandlerBot(options);
    }

    public static HandlerBot instance() {
        if (isEmpty())
            throw new NullPointerException();
        else
            return bot;
    }

    private static boolean isEmpty() {
        return bot == null;
    }
}
