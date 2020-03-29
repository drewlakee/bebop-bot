package telegram.commands;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
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
import telegram.commands.handlers.BotCommand;
import telegram.commands.handlers.CallbackQueryHandler;
import telegram.commands.handlers.MessageHandler;
import telegram.commands.keyboards.InlineKeyboardBuilder;
import telegram.commands.statics.Callbacks;
import telegram.commands.statics.Commands;
import telegram.commands.statics.MessageBodyKeys;
import telegram.utils.MessageKeysParser;
import vk.api.VkApi;
import vk.api.VkUserActor;
import vk.domain.groups.VkGroup;
import vk.domain.groups.VkGroupPool;
import vk.domain.vkObjects.VkCustomAudio;
import vk.services.VkRandomContentFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // VK Communities looks like "-123456789" and always start with "-"
        boolean isDataHaveGroup = Integer.parseInt(data) < 0;
        if (isDataHaveGroup)
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
        SendMessage answer = new SendMessage();
        answer.setText(Callbacks.ASK_CHOOSE_POST_RANDOM_COMMAND);
        answer.setReplyMarkup(buildAskModeKeyboard());
        answer.setChatId(message.getChatId());

        send(sender, answer);
    }

    private void handleChosenGroup(AbsSender sender, CallbackQuery callbackQuery) {
        deleteMessage(sender, callbackQuery);

        String groupId = callbackQuery.getData();
        HashMap<String, String> messageBodyParams = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
        String mode = messageBodyParams.get(MessageBodyKeys.MODE);

        if (mode.equals(MessageBodyKeys.MANUAL))
            constructTelegramPost(sender, callbackQuery, messageBodyParams);

        if (mode.equals(MessageBodyKeys.RANDOM))
            constructRandomVkPost(sender, callbackQuery, groupId);
    }

    private void constructRandomVkPost(AbsSender sender, CallbackQuery callbackQuery, String groupId) {
        VkGroup group = VkGroupPool.getHostGroup(Integer.parseInt(groupId));
        Photo randomPhoto = VkRandomContentFinder.findRandomPhoto();
        VkCustomAudio randomAudio = VkRandomContentFinder.findRandomAudio();
        List<String> attachments = List.of(
                "photo" + randomPhoto.getOwnerId() + "_" + randomPhoto.getId(),
                "audio" + randomAudio.getOwnerId() + "_" + randomAudio.getId()
        );

        sendVkPost(sender, callbackQuery, group, attachments);
    }

    private void constructTelegramPost(AbsSender sender, CallbackQuery callbackQuery, Map<String, String> messageBodyKeys) {
        SendPhoto telegramPost = new SendPhoto();
        telegramPost.setChatId(callbackQuery.getMessage().getChatId());
        VkCustomAudio randomAudio = VkRandomContentFinder.findRandomAudio();
        Photo randomPhoto = VkRandomContentFinder.findRandomPhoto();

        PhotoSizes largestResolution = randomPhoto.getSizes().get(randomPhoto.getSizes().size() - 1);
        telegramPost.setPhoto(largestResolution.getUrl().toString());

        VkGroup chosenGroup = VkGroupPool.getHostGroup(Integer.parseInt(callbackQuery.getData()));

        StringBuilder messageBody = new StringBuilder();
        messageBody.append(MessageBodyKeys.MODE + ": " + messageBodyKeys.get(MessageBodyKeys.MODE))
                .append("\n");
        messageBody.append(MessageBodyKeys.GROUP + ": " + chosenGroup.getGroupId() + " (" + chosenGroup.getName() + ")")
                .append("\n");
        messageBody.append("Пикча: ")
                .append("photo" + randomPhoto.getOwnerId() + "_" + randomPhoto.getId())
                .append("\n");
        messageBody.append("Трек: ")
                .append("audio" + randomAudio.getOwnerId() + "_" + randomAudio.getId() + " (" + randomAudio.toPrettyString() + ")")
                .append("\n");

        telegramPost.setCaption(messageBody.toString());
        telegramPost.setReplyMarkup(buildPostConstructKeyboard());

        send(sender, telegramPost);
    }

    private void changePhoto(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageMedia changePhotoMessage = new EditMessageMedia();
        changePhotoMessage.setChatId(callbackQuery.getMessage().getChatId());
        changePhotoMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        InputMediaPhoto newPhoto = new InputMediaPhoto();
        Photo randomPhoto = VkRandomContentFinder.findRandomPhoto();
        PhotoSizes largestResolution = randomPhoto.getSizes().get(randomPhoto.getSizes().size() - 1);
        newPhoto.setMedia(largestResolution.getUrl().toString());

        String newPhotoAttachment = "photo" + randomPhoto.getOwnerId() + "_" + randomPhoto.getId();
        String[] params = callbackQuery.getMessage().getCaption().split("\n");
        for (int i = 0; i < params.length; i++)
            if (params[i].startsWith(MessageBodyKeys.PHOTO))
                params[i] = MessageBodyKeys.PHOTO + ": " + newPhotoAttachment;
        newPhoto.setCaption(String.join("\n", params));

        changePhotoMessage.setMedia(newPhoto);
        changePhotoMessage.setReplyMarkup(buildPostConstructKeyboard());

        send(sender, changePhotoMessage);
    }

    private void changeAudio(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageMedia changeAudioMessage = new EditMessageMedia();
        changeAudioMessage.setChatId(callbackQuery.getMessage().getChatId());
        changeAudioMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        InputMediaPhoto newTrackWithOldPhoto = new InputMediaPhoto();
        newTrackWithOldPhoto.setMedia(callbackQuery.getMessage().getPhoto().get(0).getFileId());

        VkCustomAudio newAudio = VkRandomContentFinder.findRandomAudio();
        String newAudioAttachment = "audio" + newAudio.getOwnerId() + "_" + newAudio.getId() + " (" + newAudio.toPrettyString() + ")";
        String[] params = callbackQuery.getMessage().getCaption().split("\n");
        for (int i = 0; i < params.length; i++)
            if (params[i].startsWith(MessageBodyKeys.AUDIO))
                params[i] = MessageBodyKeys.AUDIO + ": " + newAudioAttachment;
        newTrackWithOldPhoto.setCaption(String.join("\n", params));

        changeAudioMessage.setMedia(newTrackWithOldPhoto);
        changeAudioMessage.setReplyMarkup(buildPostConstructKeyboard());

        send(sender, changeAudioMessage);
    }

    private void sendTelegramConstructedPost(AbsSender sender, CallbackQuery callbackQuery) {
        deleteMessage(sender, callbackQuery);

        Map<String, String> messageBodyParams = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getCaption());
        int groupId = Integer.parseInt(messageBodyParams.get(MessageBodyKeys.GROUP));
        VkGroup group = VkGroupPool.getHostGroup(groupId);
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
        messageBody.append(Callbacks.CHOOSE_GROUP_RANDOM_COMMAND);

        groupChooseMessage.setText(messageBody.toString());
        groupChooseMessage.setChatId(callbackQuery.getMessage().getChatId());
        groupChooseMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        groupChooseMessage.setReplyMarkup(buildHostGroupsKeyboard());

        send(sender, groupChooseMessage);
    }

    private InlineKeyboardMarkup buildHostGroupsKeyboard() {
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();

        for (VkGroup group : VkGroupPool.getHostGroups())
            keyboardBuilder
                    .addButton(new InlineKeyboardButton().setText(group.getName()).setCallbackData(String.valueOf(group.getGroupId())))
                    .nextLine();
        keyboardBuilder.addButton(new InlineKeyboardButton().setText("Отмена").setCallbackData(CANCEL_REQUEST_CALLBACK));

        return keyboardBuilder.build();
    }

    private InlineKeyboardMarkup buildAskModeKeyboard() {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Да").setCallbackData(MANUAL_MODE_CALLBACK))
                .addButton(new InlineKeyboardButton().setText("Нет").setCallbackData(RANDOM_MODE_CALLBACK))
                .build();
    }

    private InlineKeyboardMarkup buildPostConstructKeyboard() {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Поменять пикчу").setCallbackData(CHANGE_PHOTO_CALLBACK))
                .addButton(new InlineKeyboardButton().setText("Поменять трек").setCallbackData(CHANGE_AUDIO_CALLBACK))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Запостить").setCallbackData(SEND_CONSTRUCTED_POST_CALLBACK))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Отменить запрос").setCallbackData(CANCEL_REQUEST_CALLBACK))
                .build();
    }

    private void deleteMessage(AbsSender sender, CallbackQuery callbackQuery) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        send(sender, deleteMessage);
    }

    private void sendVkPost(AbsSender sender, CallbackQuery callbackQuery, VkGroup group, List<String> attachments) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
        sendMessage.setText("Готово, чекай группу!\n" + group.getUrl());
        sendMessage.disableWebPagePreview();

        try {
            VkApi.instance()
                    .wall()
                    .post(VkUserActor.instance())
                    .ownerId(group.getGroupId())
                    .attachments(attachments)
                    .execute();
        } catch (ClientException | ApiException e) {
            sendMessage.setText("Что-то по пути сломалось...");
        }

        send(sender, sendMessage);
    }
}
