package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.keyboards.HostGroupKeyboard;
import github.drewlakee.telegram.commands.keyboards.InlineKeyboardBuilder;
import github.drewlakee.telegram.commands.keyboards.NumpadKeyboardBuilder;
import github.drewlakee.telegram.commands.callbacks.ConstructOnlyPhotosCommandCallback;
import github.drewlakee.telegram.commands.callbacks.GlobalCallback;
import github.drewlakee.telegram.commands.handlers.BotCommand;
import github.drewlakee.telegram.commands.handlers.CallbackQueryHandler;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.telegram.commands.statics.Commands;
import github.drewlakee.telegram.utils.MessageKeysParser;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import github.drewlakee.vk.domain.groups.VkCustomGroup;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.domain.vkObjects.VkAttachment;
import github.drewlakee.vk.domain.vkObjects.VkCustomPhoto;
import github.drewlakee.vk.services.VkContentStrategyService;
import github.drewlakee.vk.services.VkWallPostService;
import github.drewlakee.vk.services.random.VkRandomPhotoContent;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: REFACTOR - decompose that class
public class ConstructOnlyPhotosCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    public ConstructOnlyPhotosCommand() {
        super(Commands.CONSTRUCT_ONLY_PHOTOS);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Choose quantity of photos: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4);
        sendMessage.setReplyMarkup(numpad.build(10, Commands.CONSTRUCT_ONLY_PHOTOS, true));
        ResponseMessageDispatcher.send(sender, sendMessage);
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        ConstructOnlyPhotosCommandCallback handleCallback = ConstructOnlyPhotosCommandCallback.UNKNOWN;
        int photosQuantity = 0;

        if (data.contains(ConstructOnlyPhotosCommandCallback.CHANGE_QUANTITY_CALLBACK.toCallbackString())) {
            handleCallback = ConstructOnlyPhotosCommandCallback.CHANGE_QUANTITY_CALLBACK;
        }

        if (data.contains(ConstructOnlyPhotosCommandCallback.CHANGE_SET_CALLBACK.toCallbackString())) {
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            photosQuantity = Integer.parseInt(keys.get("quantity"));
        }

        if (data.contains(Commands.CONSTRUCT_ONLY_PHOTOS + "_numpad")) {
            photosQuantity = Integer.parseInt(data.replace(Commands.CONSTRUCT_ONLY_PHOTOS + "_numpad", ""));
        }

        if (data.contains(ConstructOnlyPhotosCommandCallback.SEND_CALLBACK.toCallbackString())) {
            handleCallback = ConstructOnlyPhotosCommandCallback.SEND_CALLBACK;
        }

        int groupId = 0;
        if (data.contains(Commands.CONSTRUCT_ONLY_PHOTOS + "_group_id")) {
            handleCallback = ConstructOnlyPhotosCommandCallback.GROUP_CALLBACK;
            groupId = Integer.parseInt(data.replace(Commands.CONSTRUCT_ONLY_PHOTOS + "_group_id", ""));
        }

        switch (handleCallback) {
            case CHANGE_QUANTITY_CALLBACK:
                sendChangeQuantityKeyboard(sender, callbackQuery);
                break;
            case SEND_CALLBACK:
                sendGroupKeyboard(sender, callbackQuery);
                break;
            case GROUP_CALLBACK:
                sendSetOfPhotosToGroup(sender, callbackQuery, groupId);
                break;
            default:
                sendSetOfPhotos(sender, callbackQuery, photosQuantity);
        }
    }

    private void sendChangeQuantityKeyboard(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setText("Change quantity: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4);
        response.setReplyMarkup(numpad.build(10, Commands.CONSTRUCT_ONLY_PHOTOS, true));
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendGroupKeyboard(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageReplyMarkup response = new EditMessageReplyMarkup();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setReplyMarkup(new HostGroupKeyboard(Commands.CONSTRUCT_ONLY_PHOTOS, true).build());
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendSetOfPhotosToGroup(AbsSender sender, CallbackQuery callbackQuery, int groupId) {
        Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
        List<String> photosKeys = keys.keySet().stream().filter(key -> key.startsWith("photo")).collect(Collectors.toList());
        List<String> vkAttachments = new ArrayList<>();
        for (String key : photosKeys) {
            vkAttachments.add(keys.get(key));
        }
        VkCustomGroup vkGroup = VkGroupPool.getConcreteGroups(VkGroupObjective.HOST).stream()
                .filter(group -> group.getId() == groupId)
                .findFirst()
                .orElse(VkCustomGroup.EMPTY_INSTANCE);

        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setParseMode(ParseMode.HTML);
        VkWallPostService service = new VkWallPostService();
        if (service.makePost(vkGroup, vkAttachments)) {
            response.setText("Post was send to " + "<a href=\"" + vkGroup.getUrl() + "\">" + vkGroup.getName() + "</a> group!");
        } else {
            response.setText("Oops... something was broken on the way!");
        }
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendSetOfPhotos(AbsSender sender, CallbackQuery callbackQuery, int quantity) {
        VkContentStrategyService photoService = new VkContentStrategyService(new VkRandomPhotoContent());
        List<VkAttachment> vkAttachments = photoService.find(quantity);

        StringBuilder bodyText = new StringBuilder();
        bodyText.append("quantity: ").append(quantity).append("\n\n");

        int count = 1;
        for (VkAttachment attachment : vkAttachments) {
            VkCustomPhoto photo = (VkCustomPhoto) attachment;
            URL photoURL = photo.getLargestSize().getUrl();
            String vkAttachmentFormat = photo.toAttachmentString();

            bodyText.append("photo_" + count +": ")
                    .append("<a href=\"" + photoURL + "\">" + vkAttachmentFormat + "</a>")
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
                        .setCallbackData(ConstructOnlyPhotosCommandCallback.CHANGE_QUANTITY_CALLBACK.toCallbackString()))
                .addButton(new InlineKeyboardButton()
                        .setText("Change set")
                        .setCallbackData(ConstructOnlyPhotosCommandCallback.CHANGE_SET_CALLBACK.toCallbackString()))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Send")
                        .setCallbackData(ConstructOnlyPhotosCommandCallback.SEND_CALLBACK.toCallbackString()))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Cancel")
                        .setCallbackData(GlobalCallback.DELETE_MESSAGE.name()))
                .build();
    }
}
