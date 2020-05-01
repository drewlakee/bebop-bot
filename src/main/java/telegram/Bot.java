package telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.commands.CommandsPool;
import telegram.commands.statics.Callbacks;
import telegram.commands.statics.Commands;
import telegram.commands.statics.MessageBodyKeys;

public class Bot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    public Bot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public void onUpdateReceived(Update update) {
        exe.submit(() -> handleUpdate(update));
    }

    private void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("[TELEGRAM BOT] User [{}] request: {}", update.getMessage().getFrom().getUserName(),
                    "[message text] " + update.getMessage().getText());

            handleReceivedMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            log.info("[TELEGRAM BOT] User [{}] request: {}", update.getCallbackQuery().getFrom().getUserName(),
                    "[callback data] " + update.getCallbackQuery().getData());

            handleReceivedCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleReceivedMessage(Message message) {
        String handleCommand = message.getText();

        switch (handleCommand) {
            case Commands.RANDOM:
                CommandsPool.handleCommand(Commands.RANDOM, this, message);
                break;
            case Commands.MY_GROUPS:
                CommandsPool.handleCommand(Commands.MY_GROUPS, this, message);
                break;
        }
    }

    private void handleReceivedCallbackQuery(CallbackQuery callbackQuery) {
        String messageBody;
        String handleCommand = Commands.UNKNOWN;

        if (callbackQuery.getMessage().getText() == null)
            messageBody = callbackQuery.getMessage().getCaption();
        else
            messageBody = callbackQuery.getMessage().getText();

        if (isContainsRandomCommandCallbacks(messageBody))
            handleCommand = Commands.RANDOM;

        switch (handleCommand) {
            case Commands.RANDOM:
                CommandsPool.handleCommand(Commands.RANDOM, this, callbackQuery);
                break;
        }
    }

    private boolean isContainsRandomCommandCallbacks(String messageBody) {
        return messageBody.contains(Callbacks.CHOOSE_MODE_RANDOM_POST)
                || messageBody.contains(Callbacks.CHOOSE_GROUP_RANDOM_POST)
                || messageBody.contains(MessageBodyKeys.MODE);
    }

    @Override
    public String getBotUsername() {
        return System.getenv("bot_username");
    }

    @Override
    public String getBotToken() {
        return System.getenv(("bot_token"));
    }
}
