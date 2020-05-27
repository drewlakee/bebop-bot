package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.callbacks.ConstructCallback;
import github.drewlakee.telegram.commands.callbacks.GlobalCallback;
import github.drewlakee.telegram.commands.handlers.BotCommand;
import github.drewlakee.telegram.commands.handlers.CallbackQueryHandler;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.telegram.commands.keyboards.HostGroupKeyboard;
import github.drewlakee.telegram.commands.keyboards.InlineKeyboardBuilder;
import github.drewlakee.telegram.commands.keyboards.NumpadKeyboardBuilder;
import github.drewlakee.telegram.utils.MessageKeysParser;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import github.drewlakee.vk.domain.groups.VkCustomGroup;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.domain.vkObjects.VkAttachment;
import github.drewlakee.vk.domain.vkObjects.VkCustomAudio;
import github.drewlakee.vk.services.VkContentStrategyService;
import github.drewlakee.vk.services.VkWallPostService;
import github.drewlakee.vk.services.random.VkRandomAudioContent;
import github.drewlakee.vk.singletons.VkGroupPool;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConstructOnlyAudiosCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    public static final String COMMAND_NAME = "/constructOnlyAudios";

    public ConstructOnlyAudiosCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText("Choose quantity of audios: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4, 10);
        response.setReplyMarkup(numpad.build(COMMAND_NAME, true));
        ResponseMessageDispatcher.send(sender, response);
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        ConstructCallback handleCallback = ConstructCallback.DEFAULT;
        int audiosQuantity = 0;

        if (data.contains(COMMAND_NAME + "_numpad")) {
            audiosQuantity = Integer.parseInt(data.replace(COMMAND_NAME + "_numpad", ""));
        }

        if (data.contains(ConstructCallback.CHANGE_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME))) {
            handleCallback = ConstructCallback.CHANGE_QUANTITY_CALLBACK;
        }

        if (data.contains(ConstructCallback.CHANGE_SET_CALLBACK.toCallbackString(COMMAND_NAME))) {
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            audiosQuantity = Integer.parseInt(keys.get("quantity"));
        }

        if (data.contains(ConstructCallback.SEND_CALLBACK.toCallbackString(COMMAND_NAME))) {
            handleCallback = ConstructCallback.SEND_CALLBACK;
        }

        int groupId = 0;
        if (data.contains(COMMAND_NAME + "_group_id")) {
            handleCallback = ConstructCallback.GROUP_CALLBACK;
            groupId = Integer.parseInt(data.replace(COMMAND_NAME + "_group_id", ""));
        }

        switch (handleCallback) {
            case CHANGE_QUANTITY_CALLBACK:
                sendChangeQuantityKeyboard(sender, callbackQuery);
                break;
            case SEND_CALLBACK:
                sendGroupKeyboard(sender, callbackQuery);
                break;
            case GROUP_CALLBACK:
                sendSetToGroup(sender, callbackQuery, groupId);
                break;
            default:
                sendSetOfAudios(sender, callbackQuery, audiosQuantity);
        }
    }

    private void sendChangeQuantityKeyboard(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setText("Change quantity: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4, 10);
        response.setReplyMarkup(numpad.build(COMMAND_NAME, true));
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendGroupKeyboard(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageReplyMarkup response = new EditMessageReplyMarkup();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setReplyMarkup(new HostGroupKeyboard().build(COMMAND_NAME, true));
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendSetToGroup(AbsSender sender, CallbackQuery callbackQuery, int groupId) {
        Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
        List<String> audiosKeys = keys.keySet().stream().filter(key -> key.startsWith("audio")).collect(Collectors.toList());
        List<String> vkAttachments = new ArrayList<>();
        for (String key : audiosKeys) {
            vkAttachments.add(keys.get(key));
        }

        VkCustomGroup vkGroup = VkGroupPool.getConcreteGroups(VkGroupObjective.HOST).stream()
                .filter(group -> group.getId() == groupId)
                .findFirst()
                .orElse(VkCustomGroup.EMPTY_INSTANCE);

        EditMessageReplyMarkup response = new EditMessageReplyMarkup();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        VkWallPostService service = new VkWallPostService();
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();
        boolean isRequestSuccessful = service.makePost(vkGroup, vkAttachments);
        if (isRequestSuccessful) {
            keyboardBuilder.addButton(new InlineKeyboardButton()
                    .setText("Post was send to " + vkGroup.getName() + " group!")
                    .setUrl(vkGroup.getUrl()));
        } else {
            keyboardBuilder.addButton(new InlineKeyboardButton()
                    .setText("Oops... something was broken on the way!"));
        }
        response.setReplyMarkup(keyboardBuilder.build());

        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendSetOfAudios(AbsSender sender, CallbackQuery callbackQuery, int quantity) {
        VkContentStrategyService audioService = new VkContentStrategyService(new VkRandomAudioContent());
        List<VkAttachment> vkAttachments = audioService.find(quantity);

        StringBuilder bodyText = new StringBuilder();
        bodyText.append("quantity: ").append(quantity).append("\n\n");

        int count = 1;
        for (VkAttachment attachment : vkAttachments) {
            VkCustomAudio audio = (VkCustomAudio) attachment;
            String prettyNameOfAudio = audio.toPrettyString();
            String vkAttachmentFormat = audio.toAttachmentString();

            bodyText.append("audio_" + count +": ")
                    .append(vkAttachmentFormat).append(" (").append(prettyNameOfAudio).append(")")
                    .append("\n");
            count++;
        }

        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setParseMode(ParseMode.HTML);
        response.setText(bodyText.toString());
        response.setReplyMarkup(buildConstructKeyboard());
        ResponseMessageDispatcher.send(sender, response);
    }

    public InlineKeyboardMarkup buildConstructKeyboard() {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton()
                        .setText("Change quantity")
                        .setCallbackData(ConstructCallback.CHANGE_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME)))
                .addButton(new InlineKeyboardButton()
                        .setText("Change set")
                        .setCallbackData(ConstructCallback.CHANGE_SET_CALLBACK.toCallbackString(COMMAND_NAME)))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Send")
                        .setCallbackData(ConstructCallback.SEND_CALLBACK.toCallbackString(COMMAND_NAME)))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Cancel")
                        .setCallbackData(GlobalCallback.DELETE_MESSAGE.name()))
                .build();
    }

}
