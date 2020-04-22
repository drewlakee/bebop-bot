package telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.commands.CommandsPool;
import telegram.commands.MyGroupsCommand;
import telegram.commands.RandomCommand;

public class TelegramBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private static final String TELEGRAM_CONFIG_ADD = "[TELEGRAM BOT] TELEGRAM CONFIG: ADD ";

    public static void run() {
        ApiContextInitializer.init();
        DefaultBotOptions customOptions = initializeCustomOptions();
        Bot bot = new Bot(customOptions);
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            CommandsPool.register(new MyGroupsCommand());
            CommandsPool.register(new RandomCommand());
            botsApi.registerBot(bot);
            log.info("[TELEGRAM BOT] Commands registered pool: {}", CommandsPool.getRegisteredCommandsSet());
        } catch (TelegramApiException e) {
            log.error("[TELEGRAM BOT] ERROR: Launch failed.");
            e.printStackTrace();
        }
    }

    private static DefaultBotOptions initializeCustomOptions() {
        DefaultBotOptions customOptions = new DefaultBotOptions();

        if (System.getenv().containsKey("bot_threads")) {
            customOptions.setMaxThreads(Integer.parseInt(System.getenv("bot_threads")));
        } else
            customOptions.setMaxThreads(1);
        log.info(TELEGRAM_CONFIG_ADD + "maximum threads - " + customOptions.getMaxThreads());

        if (System.getenv().containsKey("bot_proxy_type") &&
                System.getenv().containsKey("bot_proxy_host") &&
                System.getenv().containsKey("bot_proxy_port")) {
            log.info(TELEGRAM_CONFIG_ADD + "bot proxy - " + System.getenv("bot_proxy_type"));
            customOptions.setProxyType(DefaultBotOptions.ProxyType.valueOf(System.getenv("bot_proxy_type")));
            log.info(TELEGRAM_CONFIG_ADD + "bot proxy host - " + System.getenv("bot_proxy_host"));
            customOptions.setProxyHost(System.getenv("bot_proxy_host"));
            log.info(TELEGRAM_CONFIG_ADD + "bot proxy port - " + System.getenv("bot_proxy_port"));
            customOptions.setProxyPort(Integer.parseInt(System.getenv("bot_proxy_port")));
        }

        return customOptions;
    }
}
