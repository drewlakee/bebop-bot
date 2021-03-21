package github.drewlakee.telegram;

import github.drewlakee.telegram.commands.BotCommand;
import github.drewlakee.telegram.commands.devs.NotFoundCommand;
import github.drewlakee.telegram.commands.handlers.CallbackQueryHandler;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Objects;

@Component
public class BebopBot extends TelegramLongPollingBot {

    private String botUsername;
    private String botToken;

    private final HashMap<String, BotCommand> commands;

    @Autowired
    public BebopBot(DefaultBotOptions options, HashMap<String, BotCommand> commands) {
        super(options);
        this.commands = commands;
    }

    @Override
    public void onUpdateReceived(Update update) {
        exe.submit(() -> handleReceivedEvent(update));
    }

    private void handleReceivedEvent(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleReceivedMessage(update.getMessage());
        }

        if (update.hasCallbackQuery()) {
            handleReceivedCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleReceivedMessage(Message message) {
        String command = message.getText();
        BotCommand botCommand = commands.getOrDefault(command, commands.get("/not_found"));
        ((MessageHandler) botCommand).handle(this, message);
    }

    /**
     * Receives telegram callback such a keyboard click or something else.
     *
     * Callbacks must be divided by their data. For example, "/post_<callback_name>" and handler
     * of that callback must be in commands pool.
     *
     * @param callbackQuery Telegram callback
     */
    private void handleReceivedCallbackQuery(CallbackQuery callbackQuery) {
        String command = callbackQuery.getData().split("_")[0];
        BotCommand botCommand = commands.getOrDefault(command, commands.get("/not_found"));
        ((CallbackQueryHandler) botCommand).handle(this, callbackQuery);
    }

    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BebopBot bebopBot = (BebopBot) o;
        return Objects.equals(botUsername, bebopBot.botUsername) && Objects.equals(botToken, bebopBot.botToken) && Objects.equals(commands, bebopBot.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(botUsername, botToken, commands);
    }
}
