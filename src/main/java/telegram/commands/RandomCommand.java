package telegram.commands;

import com.vk.api.sdk.objects.photos.PhotoSizes;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import telegram.ResponseMessageDispatcher;
import telegram.commands.handlers.BotCommand;
import telegram.commands.handlers.CallbackQueryHandler;
import telegram.commands.handlers.MessageHandler;
import telegram.commands.keyboards.InlineKeyboardBuilder;
import telegram.commands.statics.Callbacks;
import telegram.commands.statics.Commands;
import telegram.commands.statics.MessageBodyKeys;
import telegram.utils.MessageKeysParser;
import vk.domain.groups.VkCustomGroup;
import vk.domain.groups.VkGroupPool;
import vk.domain.random.VkRandomAudioContent;
import vk.domain.random.VkRandomPhotoContent;
import vk.domain.vkObjects.VkAttachment;
import vk.domain.vkObjects.VkCustomAudio;
import vk.domain.vkObjects.VkCustomPhoto;
import vk.services.VkContentService;
import vk.services.VkWallPostService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RandomCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    private final static String CANCEL_REQUEST_CALLBACK = "0";
    private final static String SEND_CONSTRUCTED_POST_CALLBACK = "1";
    private final static String GROUP_CALLBACK = "2";
    private final static String CHANGE_PHOTO_CALLBACK = "3";
    private final static String CHANGE_AUDIO_CALLBACK = "4";
    private final static String RANDOM_MODE_CALLBACK = "5";
    private final static String MANUAL_MODE_CALLBACK = "6";

    public RandomCommand() {
        super(Commands.RANDOM);
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String handleWay = callbackQuery.getData();

        boolean isDataHaveVkGroupId = Integer.parseInt(data) < 0;
        if (isDataHaveVkGroupId)
            handleWay = GROUP_CALLBACK;

        switch (handleWay) {
            case GROUP_CALLBACK:
                handleChosenGroup(sender, callbackQuery);
                break;
            case CHANGE_PHOTO_CALLBACK:
                changePhoto(sender, callbackQuery);
                break;
            case CHANGE_AUDIO_CALLBACK:
                changeAudio(sender, callbackQuery);
                break;
            case SEND_CONSTRUCTED_POST_CALLBACK:
                sendTelegramConstructedPost(sender, callbackQuery);
                break;
            case CANCEL_REQUEST_CALLBACK:
                deleteMessage(sender, callbackQuery);
                break;
            default:
                handleChosenMode(sender, callbackQuery);
                break;
        }
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage responseMessage = new SendMessage();
        responseMessage.setText(Callbacks.CHOOSE_MODE_RANDOM_POST);
        responseMessage.setReplyMarkup(buildAskModeKeyboard());
        responseMessage.setChatId(message.getChatId());

        ResponseMessageDispatcher.send(sender, responseMessage);
    }

    private void handleChosenGroup(AbsSender sender, CallbackQuery callbackQuery) {
        deleteMessage(sender, callbackQuery);

        String groupId = callbackQuery.getData();
        HashMap<String, String> messageBodyParams = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
        String mode = messageBodyParams.get(MessageBodyKeys.MODE);

        VkContentService audioService = new VkContentService(new VkRandomAudioContent());
        VkContentService photoService = new VkContentService(new VkRandomPhotoContent());

        Observable<VkAttachment> asyncRandomContentRequests = Observable.merge(
                Observable.fromCallable(() -> audioService.find(1).get(0))
                        .subscribeOn(Schedulers.io()),
                Observable.fromCallable(() -> photoService.find(1).get(0))
                        .subscribeOn(Schedulers.io())
        );

        List<VkAttachment> randomVkAttachments = asyncRandomContentRequests
                .collectInto(new ArrayList<VkAttachment>(), ArrayList::add)
                .blockingGet();

        switch (mode) {
            case MessageBodyKeys.MANUAL:
                constructTelegramPost(sender, callbackQuery, messageBodyParams, randomVkAttachments);
                break;
            case MessageBodyKeys.RANDOM:
                constructRandomVkPost(sender, callbackQuery, groupId, randomVkAttachments);
        }
    }

    private void constructTelegramPost(AbsSender sender, CallbackQuery callbackQuery, Map<String, String> messageBodyKeys, List<VkAttachment> attachments) {
        SendPhoto telegramPostMessage = new SendPhoto();
        telegramPostMessage.setChatId(callbackQuery.getMessage().getChatId());

        VkCustomAudio randomAudio = (VkCustomAudio) attachments.stream()
                .filter(x -> x instanceof VkCustomAudio)
                .findFirst()
                .get();
        VkCustomPhoto randomPhoto = (VkCustomPhoto) attachments.stream()
                .filter(x -> x instanceof VkCustomPhoto)
                .findFirst()
                .get();

        VkCustomGroup chosenGroup = VkGroupPool.getHostGroup(Integer.parseInt(callbackQuery.getData()));
        StringBuilder messageBody = new StringBuilder();
        messageBody.append(MessageBodyKeys.MODE + ": ").append(messageBodyKeys.get(MessageBodyKeys.MODE))
                .append("\n");
        messageBody.append(MessageBodyKeys.GROUP + ": ")
                .append(chosenGroup.getId())
                .append(" (").append(chosenGroup.getName()).append(")")
                .append("\n");
        messageBody.append(MessageBodyKeys.PHOTO + ": ")
                .append(randomPhoto.toAttachmentString())
                .append("\n");
        messageBody.append(MessageBodyKeys.AUDIO + ": ")
                .append(randomAudio.toAttachmentString())
                .append(" (").append(randomAudio.toPrettyString()).append(")")
                .append("\n");

        telegramPostMessage.setPhoto(randomPhoto.getLargestSize().getUrl().toString());
        telegramPostMessage.setCaption(messageBody.toString());
        telegramPostMessage.setReplyMarkup(buildPostConstructKeyboard());

        ResponseMessageDispatcher.send(sender, telegramPostMessage);
    }

    private void constructRandomVkPost(AbsSender sender, CallbackQuery callbackQuery, String groupId, List<VkAttachment> attachments) {
        VkCustomGroup group = VkGroupPool.getHostGroup(Integer.parseInt(groupId));
        List<String> stringAttachments = new ArrayList<>();
        for (VkAttachment attachment : attachments) {
            stringAttachments.add(attachment.toAttachmentString());
        }

        sendVkPost(sender, callbackQuery, group, stringAttachments);
    }

    private void changePhoto(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageMedia changePhotoMessage = new EditMessageMedia();
        changePhotoMessage.setChatId(callbackQuery.getMessage().getChatId());
        changePhotoMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        InputMediaPhoto newPhoto = new InputMediaPhoto();
        VkContentService photoService = new VkContentService(new VkRandomPhotoContent());
        VkCustomPhoto randomPhoto = (VkCustomPhoto) photoService.find(1).get(0);
        newPhoto.setMedia(randomPhoto.getLargestSize().getUrl().toString());

        String[] params = callbackQuery.getMessage().getCaption().split("\n");
        for (int i = 0; i < params.length; i++)
            if (params[i].startsWith(MessageBodyKeys.PHOTO))
                params[i] = MessageBodyKeys.PHOTO + ": " + randomPhoto.toAttachmentString();
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

        VkContentService audioService = new VkContentService(new VkRandomAudioContent());
        VkCustomAudio randomAudio = (VkCustomAudio) audioService.find(1).get(0);
        String[] params = callbackQuery.getMessage().getCaption().split("\n");
        for (int i = 0; i < params.length; i++)
            if (params[i].startsWith(MessageBodyKeys.AUDIO))
                params[i] = MessageBodyKeys.AUDIO + ": "
                        + randomAudio.toAttachmentString()
                        + " (" + randomAudio.toPrettyString() + ")";

        newTrackWithOldPhoto.setCaption(String.join("\n", params));
        changeAudioMessage.setMedia(newTrackWithOldPhoto);
        changeAudioMessage.setReplyMarkup(buildPostConstructKeyboard());

        ResponseMessageDispatcher.send(sender, changeAudioMessage);
    }

    private void sendTelegramConstructedPost(AbsSender sender, CallbackQuery callbackQuery) {
        Map<String, String> messageBodyParams = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getCaption());
        int groupId = Integer.parseInt(messageBodyParams.get(MessageBodyKeys.GROUP));
        VkCustomGroup group = VkGroupPool.getHostGroup(groupId);
        List<String> attachments = List.of(
                messageBodyParams.get(MessageBodyKeys.PHOTO),
                messageBodyParams.get(MessageBodyKeys.AUDIO)
        );

        sendVkPost(sender, callbackQuery, group, attachments);
    }

    private void handleChosenMode(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageText groupChooseMessage = new EditMessageText();
        StringBuilder messageBody = new StringBuilder();

        if (callbackQuery.getData().equals(MANUAL_MODE_CALLBACK))
            messageBody.append(MessageBodyKeys.MANUAL_MODE);
        else
            messageBody.append(MessageBodyKeys.RANDOM_MODE);
        messageBody.append("\n\n");
        messageBody.append(Callbacks.CHOOSE_GROUP_RANDOM_POST);

        groupChooseMessage.setText(messageBody.toString());
        groupChooseMessage.setChatId(callbackQuery.getMessage().getChatId());
        groupChooseMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        groupChooseMessage.setReplyMarkup(buildHostGroupsKeyboard());

        ResponseMessageDispatcher.send(sender, groupChooseMessage);
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
        if (isSend)
            responseMessage.setText("Post was send to group!");
        else
            responseMessage.setText("Something goes wrong...");
        responseMessage.setReplyMarkup(buildKeyboardWithGroupUrl(group.getUrl()));

        ResponseMessageDispatcher.send(sender, responseMessage);
    }

    private void deleteMessage(AbsSender sender, CallbackQuery callbackQuery) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        ResponseMessageDispatcher.send(sender, deleteMessage);
    }

    private InlineKeyboardMarkup buildKeyboardWithGroupUrl(String url) {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Link to group").setUrl(url))
                .build();
    }

    private InlineKeyboardMarkup buildHostGroupsKeyboard() {
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();

        for (VkCustomGroup group : VkGroupPool.getHostGroups())
            keyboardBuilder.addButton(new InlineKeyboardButton().setText(group.getName()).setCallbackData(String.valueOf(group.getId()))).nextLine();
        keyboardBuilder.addButton(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_REQUEST_CALLBACK));

        return keyboardBuilder.build();
    }

    private InlineKeyboardMarkup buildAskModeKeyboard() {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Yes").setCallbackData(MANUAL_MODE_CALLBACK))
                .addButton(new InlineKeyboardButton().setText("No").setCallbackData(RANDOM_MODE_CALLBACK))
                .build();
    }

    private InlineKeyboardMarkup buildPostConstructKeyboard() {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Change picture").setCallbackData(CHANGE_PHOTO_CALLBACK))
                .addButton(new InlineKeyboardButton().setText("Change track").setCallbackData(CHANGE_AUDIO_CALLBACK))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Send post").setCallbackData(SEND_CONSTRUCTED_POST_CALLBACK))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_REQUEST_CALLBACK))
                .build();
    }
}
