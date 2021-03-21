package github.drewlakee.telegram.configs;

import github.drewlakee.telegram.commands.BotCommand;
import github.drewlakee.telegram.commands.devs.DeleteMessageCommand;
import github.drewlakee.telegram.commands.devs.NotFoundCommand;
import github.drewlakee.telegram.commands.users.GroupsCommand;
import github.drewlakee.telegram.commands.users.PostCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.util.HashMap;

@Configuration
public class TelegramBotConfiguration {

    private final Environment env;

    @Autowired
    public TelegramBotConfiguration(Environment environment) {
        this.env = environment;
    }

    @Bean
    public DefaultBotOptions configureBebopBotOptions() {
        DefaultBotOptions options = new DefaultBotOptions();

        options.setMaxThreads(env.getProperty("bot_threads", Integer.class, 1));

        boolean isProxyConfigurationSet =
                env.containsProperty("bot_proxy_type") &&
                env.containsProperty("bot_proxy_host") &&
                env.containsProperty("bot_proxy_port");

        if (isProxyConfigurationSet) {
            options.setProxyType(DefaultBotOptions.ProxyType.valueOf(env.getProperty("bot_proxy_type")));
            options.setProxyHost(env.getProperty("bot_proxy_host"));
            options.setProxyPort(env.getProperty("bot_proxy_port", Integer.class, -1));
        }

        return options;
    }

    @Bean
    public HashMap<String, BotCommand> configureBebopBotCommands(PostCommand post,
                                                                 GroupsCommand groups,
                                                                 DeleteMessageCommand deleteMessage,
                                                                 NotFoundCommand notFoundCommand) {
        HashMap<String, BotCommand> commands = new HashMap<>();
        commands.put(post.getCommandName(), post);
        commands.put(groups.getCommandName(), groups);
        commands.put(deleteMessage.getCommandName(), deleteMessage);
        commands.put(notFoundCommand.getCommandName(), notFoundCommand);
        return commands;
    }
}
