package telegram;

import com.google.common.collect.ComparisonChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.commands.singletons.CommandsPool;
import telegram.commands.MyGroupsCommand;
import telegram.commands.RandomCommand;
import telegram.commands.singletons.CallbacksPool;
import telegram.commands.callbacks.RandomCommandCallback;

public class TelegramBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private static final String TELEGRAM_CONFIG_ADD = "[TELEGRAM BOT] TELEGRAM CONFIG: ADD ";

    public static void run() {
        ApiContextInitializer.init();
        DefaultBotOptions customOptions = initializeCustomOptions();
        Bot bot = new Bot(customOptions);
        TelegramBotsApi botsApi = new TelegramBotsApi();

        CommandsPool.register(new MyGroupsCommand());
        CommandsPool.register(new RandomCommand());
        log.info("[TELEGRAM BOT] Commands registered pool: {}", CommandsPool.getRegisteredCommandsSet());

        CallbacksPool.register(RandomCommandCallback.callbacks);

        try {
            botsApi.registerBot(bot);
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

        int hasProxyConfigs = ComparisonChain.start()
                .compareTrueFirst(true, System.getenv().containsKey("bot_proxy_type"))
                .compareTrueFirst(true, System.getenv().containsKey("bot_proxy_host"))
                .compareTrueFirst(true, System.getenv().containsKey("bot_proxy_port"))
                .result();

        if (hasProxyConfigs == 0) {
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
