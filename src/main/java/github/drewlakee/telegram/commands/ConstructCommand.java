package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.callbacks.ConstructCommandCallback;
import github.drewlakee.telegram.commands.callbacks.GlobalCallback;
import github.drewlakee.telegram.commands.handlers.BotCommand;
import github.drewlakee.telegram.commands.handlers.CallbackQueryHandler;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.telegram.commands.keyboards.InlineKeyboardBuilder;
import github.drewlakee.telegram.utils.MessageKeysParser;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import github.drewlakee.vk.domain.groups.VkCustomGroup;
import github.drewlakee.vk.domain.groups.VkGroupObjective;
import github.drewlakee.vk.domain.vkObjects.VkCustomAudio;
import github.drewlakee.vk.domain.vkObjects.VkCustomPhoto;
import github.drewlakee.vk.services.VkContentStrategyService;
import github.drewlakee.vk.services.VkWallPostService;
import github.drewlakee.vk.services.random.VkRandomAudioContent;
import github.drewlakee.vk.services.random.VkRandomPhotoContent;
import github.drewlakee.vk.singletons.VkGroupPool;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;
import java.util.Map;

public class ConstructCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    public ConstructCommand() {
        super(ConstructOnlyPhotosCommand.COMMAND_NAME);
    }

    private static final String ONLY_PHOTOS = "only photos";

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        ConstructCommandCallback callback;

        if (callbackQuery.getData().matches("^" + "TEST" + "_group_id-[0-9]*")) {
            callback = ConstructCommandCallback.GROUP_CALLBACK;
        }

     //   if (callbackQuery.getData().matches())

//        switch (callback) {
//            case ONLY_PHOTO_MODE_CALLBACK:
//                sendOnlyPhotoNumpad(sender, callbackQuery);
//        }
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Choose mode: ");
        sendMessage.setReplyMarkup(buildConstructModeKeyboard());
        ResponseMessageDispatcher.send(sender, sendMessage);
    }

    private String fillMessageBody(Map<String, String> keys) {

        return null;
    }

    private void sendOnlyPhotoNumpad(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageText onlyPhotoMessageWithNumpad = new EditMessageText();
        onlyPhotoMessageWithNumpad.setChatId(callbackQuery.getMessage().getChatId());
        onlyPhotoMessageWithNumpad.setMessageId(callbackQuery.getMessage().getMessageId());
        String bodyText =  "mode: " + ONLY_PHOTOS + "\n\nChoose photos quantity:";
        onlyPhotoMessageWithNumpad.setText(bodyText);
        onlyPhotoMessageWithNumpad.setReplyMarkup(buildNumpad(10));
        ResponseMessageDispatcher.send(sender, onlyPhotoMessageWithNumpad);
    }

    private void changePhoto(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageMedia changePhotoMessage = new EditMessageMedia();
        changePhotoMessage.setChatId(callbackQuery.getMessage().getChatId());
        changePhotoMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        InputMediaPhoto newPhoto = new InputMediaPhoto();
        VkContentStrategyService photoService = new VkContentStrategyService(new VkRandomPhotoContent());
        VkCustomPhoto randomPhoto = (VkCustomPhoto) photoService.find(1).get(0);
        newPhoto.setMedia(randomPhoto.getLargestSize().getUrl().toString());

        String[] params = callbackQuery.getMessage().getCaption().split("\n");
        for (int i = 0; i < params.length; i++)
            if (params[i].startsWith("picture"))
                params[i] = "picture: " + randomPhoto.toAttachmentString();
        newPhoto.setCaption(String.join("\n", params));

        changePhotoMessage.setMedia(newPhoto);
        changePhotoMessage.setReplyMarkup(buildPostConstructKeyboard());

        ResponseMessageDispatcher.send(sender,changePhotoMessage);
    }

    private void changeAudio(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageMedia changeAudioMessage = new EditMessageMedia();
        changeAudioMessage.setChatId(callbackQuery.getMessage().getChatId());
        changeAudioMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        InputMediaPhoto newTrackWithOldPhoto = new InputMediaPhoto();
        newTrackWithOldPhoto.setMedia(callbackQuery.getMessage().getPhoto().get(0).getFileId());

        VkContentStrategyService audioService = new VkContentStrategyService(new VkRandomAudioContent());
        VkCustomAudio randomAudio = (VkCustomAudio) audioService.find(1).get(0);
        String[] params = callbackQuery.getMessage().getCaption().split("\n");
        for (int i = 0; i < params.length; i++)
            if (params[i].startsWith("track"))
                params[i] = "track: "
                        + randomAudio.toAttachmentString()
                        + " (" + randomAudio.toPrettyString() + ")";

        newTrackWithOldPhoto.setCaption(String.join("\n", params));
        changeAudioMessage.setMedia(newTrackWithOldPhoto);
        changeAudioMessage.setReplyMarkup(buildPostConstructKeyboard());

        ResponseMessageDispatcher.send(sender, changeAudioMessage);
    }

    private void sendTelegramConstructedPost(AbsSender sender, CallbackQuery callbackQuery) {
        Map<String, String> messageBodyParams = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getCaption());
        int groupId = Integer.parseInt(messageBodyParams.get("group"));
        VkCustomGroup vkGroup = VkGroupPool.getConcreteGroups(VkGroupObjective.HOST).stream()
                .filter(group -> group.getId() == groupId)
                .findFirst()
                .get();

        List<String> attachments = List.of(
                messageBodyParams.get("picture"),
                messageBodyParams.get("track")
        );

        sendVkPost(sender, callbackQuery, vkGroup, attachments);
    }

    private void sendVkPost(AbsSender sender, CallbackQuery callbackQuery, VkCustomGroup group, List<String> attachments) {
        VkWallPostService postService = new VkWallPostService();
        boolean isSend = postService.makePost(group, attachments);

        if (isTelegramConstructedPost(callbackQuery))
            sendConstructedPostResponse(sender, callbackQuery, group, isSend);
        else
            sendRandomPostResponse(sender, callbackQuery, group, isSend);
    }

    private boolean isTelegramConstructedPost(CallbackQuery callbackQuery) {
        return callbackQuery.getMessage().getPhoto() != null
                && callbackQuery.getMessage().getCaption() != null;
    }

    private void sendConstructedPostResponse(AbsSender sender, CallbackQuery callbackQuery, VkCustomGroup group, boolean isSend) {
        EditMessageMedia responseMessage = new EditMessageMedia();
        responseMessage.setChatId(callbackQuery.getMessage().getChatId());
        responseMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        String messageResponseBody;
        if (isSend)
            messageResponseBody = callbackQuery.getMessage().getCaption() + "\n\nPost was send to group!";
        else
            messageResponseBody = "Something goes wrong...";

        InputMediaPhoto postPhoto = new InputMediaPhoto();
        postPhoto.setMedia(callbackQuery.getMessage().getPhoto().get(0).getFileId());
        postPhoto.setCaption(messageResponseBody);
        responseMessage.setMedia(postPhoto);
        responseMessage.setReplyMarkup(buildKeyboardWithGroupUrl(group.getUrl()));

        ResponseMessageDispatcher.send(sender, responseMessage);
    }

    private void sendRandomPostResponse(AbsSender sender, CallbackQuery callbackQuery, VkCustomGroup group, boolean isSend) {
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(callbackQuery.getMessage().getChatId());
        if (isSend) {
            responseMessage.setText("Post was send to " + group.getName() + " group!");
            responseMessage.setReplyMarkup(buildKeyboardWithGroupUrl(group.getUrl()));
        } else {
            responseMessage.setText("Something goes wrong...");
        }

        ResponseMessageDispatcher.send(sender, responseMessage);
    }

    private InlineKeyboardMarkup buildKeyboardWithGroupUrl(String url) {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Link to group").setUrl(url))
                .build();
    }

    private InlineKeyboardMarkup buildHostGroupsKeyboard() {
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();

        for (VkCustomGroup group : VkGroupPool.getConcreteGroups(VkGroupObjective.HOST))
            keyboardBuilder.addButton(new InlineKeyboardButton().setText(group.getName()).setCallbackData("TEST" + "_group_id" + group.getId()))
                    .nextLine();
        keyboardBuilder.addButton(new InlineKeyboardButton().setText("Cancel").setCallbackData(GlobalCallback.DELETE_MESSAGE.name()));

        return keyboardBuilder.build();
    }

    private InlineKeyboardMarkup buildConstructModeKeyboard() {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Only music post").setCallbackData(ConstructCommandCallback.ONLY_MUSIC_MODE_CALLBACK.toCallbackString()))
                .addButton(new InlineKeyboardButton().setText("Only photo post").setCallbackData(ConstructCommandCallback.ONLY_PHOTO_MODE_CALLBACK.toCallbackString()))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Multi-media post").setCallbackData(ConstructCommandCallback.MULTI_MEDIA_CALLBACK.toCallbackString()))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Cancel").setCallbackData(GlobalCallback.DELETE_MESSAGE.name()))
                .build();
    }

    private InlineKeyboardMarkup buildNumpad(int quantity) {
        InlineKeyboardBuilder keyboard = new InlineKeyboardBuilder();
        int maxRows = (quantity % 4 == 0) ? quantity / 4 : quantity / 4 + 1;
        int maxColumns = 4;
        int count = 1;
        for (int rows = 0; rows < maxRows; rows++) {
            for (int columns = 0; count <= quantity && columns < maxColumns; columns++) {
                keyboard.addButton(new InlineKeyboardButton().setText(String.valueOf(count)).setCallbackData("TEST" + "_numpad" + count));
                count++;
            }
            keyboard.nextLine();
        }
        keyboard.addButton(new InlineKeyboardButton().setText("Cancel").setCallbackData(GlobalCallback.DELETE_MESSAGE.name()));
        return keyboard.build();
    }

    private InlineKeyboardMarkup buildPostConstructKeyboard() {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Change photos").setCallbackData(ConstructCommandCallback.CHANGE_PHOTO_CALLBACK.toCallbackString()))
                .addButton(new InlineKeyboardButton().setText("Change tracks").setCallbackData(ConstructCommandCallback.CHANGE_AUDIO_CALLBACK.toCallbackString()))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Send post").setCallbackData(ConstructCommandCallback.SEND_CONSTRUCTED_POST_CALLBACK.toCallbackString()))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Cancel").setCallbackData(GlobalCallback.DELETE_MESSAGE.name()))
                .build();
    }
}
