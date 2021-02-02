package github.drewlakee.telegram.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
public class TelegramBotConfiguration {

    private final Environment env;

    @Autowired
    public TelegramBotConfiguration(Environment environment) {
        this.env = environment;
    }

    @Bean
    public DefaultBotOptions configureBebopBot() {
        DefaultBotOptions options = new DefaultBotOptions();

        options.setMaxThreads(env.getProperty("bot_threads", Integer.class, 1));

        boolean isProxyConfigurationSet =
                env.containsProperty("bot_proxy_type") &&
                env.containsProperty("bot_proxy_host") &&
                env.containsProperty("bot_proxy_port");

        if (isProxyConfigurationSet) {
            options.setProxyType(DefaultBotOptions.ProxyType.valueOf(env.getProperty("bot_proxy_type")));
            options.setProxyHost(env.getProperty("bot_proxy_host"));
            options.setProxyPort(env.getProperty("bot_proxy_port", Integer.class));
        }

        return options;
    }
}
