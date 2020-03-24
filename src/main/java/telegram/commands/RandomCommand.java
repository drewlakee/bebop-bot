package telegram.commands;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import telegram.commands.dialog.RandomCommandDialog;
import telegram.commands.handlers.BotCommand;
import telegram.commands.handlers.CallbackQueryHandler;
import telegram.commands.handlers.MessageHandler;
import telegram.commands.statics.Callbacks;
import telegram.commands.statics.Commands;
import vk.api.VkApi;
import vk.api.VkUserActor;
import vk.domain.groups.VkGroup;
import vk.domain.groups.VkGroupPool;
import vk.domain.vkObjects.VkCustomAudio;
import vk.services.VkRandomContentFinder;

import javax.annotation.concurrent.GuardedBy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RandomCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    @GuardedBy("this")
    private final HashMap<Integer, RandomCommandDialog> messageIdToMessageDialogStorage = new HashMap<>();

    private final static String CANCEL_REQUEST = "0";
    private final static String SEND_CONSTRUCTED_POST = "1";
    private final static String GROUP_CALLBACK = "2";
    private final static String CHANGE_PHOTO = "3";
    private final static String CHANGE_AUDIO = "4";
    private final static String RANDOM_MODE = "5";
    private final static String MANUAL_MODE = "6";

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
                handleChosenMode(sender, callbackQuery);
                break;
            case CHANGE_PHOTO:
                changePhoto(sender, callbackQuery);
                break;
            case CHANGE_AUDIO:
                changeAudio(sender, callbackQuery);
                break;
            case SEND_CONSTRUCTED_POST:
                sendConstructedPost(sender, callbackQuery);
            case CANCEL_REQUEST:
                endMessageDialog(sender, callbackQuery);
                break;
            default:
                startMessageDialog(sender, callbackQuery);
                break;
        }
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage answer = new SendMessage();
        setAskChoosePostInlineKeyboardMarkup(answer);
        answer.setChatId(message.getChatId());
        send(sender, answer);
    }

    private void handleChosenMode(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainsMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = messageIdToMessageDialogStorage.get(messageId);
                VkGroup chosenVkGroup = VkGroupPool.getHostGroup(Integer.parseInt(data));
                if (!dialog.hasVkGroup())
                    dialog.setVkGroup(chosenVkGroup);

                if (dialog.getConstructMode().equals(MANUAL_MODE)) {
                    sendTelegramPost(sender, callbackQuery, dialog);
                } else
                    sendRandomVkPost(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void changePhoto(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainsMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = messageIdToMessageDialogStorage.get(messageId);
                sendAnotherPhoto(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void sendAnotherPhoto(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        Photo anotherPhoto = VkRandomContentFinder.findRandomPhoto();
        dialog.setPhoto(anotherPhoto);
        sendPostChooseKeyboard(sender, callbackQuery, anotherPhoto, dialog.getAudio());
    }

    private void changeAudio(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainsMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = messageIdToMessageDialogStorage.get(messageId);
                sendAnotherAudio(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void sendAnotherAudio(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        VkCustomAudio anotherAudio = VkRandomContentFinder.findRandomAudio();
        dialog.setAudio(anotherAudio);
        sendPostChooseKeyboard(sender, callbackQuery, dialog.getPhoto(), anotherAudio);
    }

    private void sendConstructedPost(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainsMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = messageIdToMessageDialogStorage.get(messageId);
                endMessageDialog(sender, callbackQuery);
                sendVkPost(sender, callbackQuery.getMessage(), dialog.getAudio(), dialog.getPhoto(), dialog.getVkGroup());
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void startMessageDialog(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();

        synchronized (this) {
            if (!isStorageContainsMessageId(messageId)) {
                RandomCommandDialog dialog = new RandomCommandDialog(messageId);
                if (!dialog.hasPhotoChooseAnswer())
                    dialog.setConstructMode(data);
                messageIdToMessageDialogStorage.put(messageId, dialog);
            }
        }

        EditMessageText groupChooseMessage = new EditMessageText();
        groupChooseMessage.setText(Callbacks.CHOOSE_GROUP_RANDOM_COMMAND);
        groupChooseMessage.setChatId(callbackQuery.getMessage().getChatId());
        groupChooseMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        setHostGroupsInlineKeyboardMarkup(groupChooseMessage);

        send(sender, groupChooseMessage);
    }

    private void endMessageDialog(AbsSender sender, CallbackQuery callbackQuery) {
        synchronized (this) {
            messageIdToMessageDialogStorage.remove(callbackQuery.getMessage().getMessageId());
            deleteHandledMessage(sender, callbackQuery);
        }
    }

    private void setHostGroupsInlineKeyboardMarkup(EditMessageText message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> groupsButtonsLine = new ArrayList<>();
        List<InlineKeyboardButton> cancelButtonLine = new ArrayList<>();

        for (VkGroup group : VkGroupPool.getHostGroups())
            groupsButtonsLine.add(new InlineKeyboardButton().setText(group.getName()).setCallbackData(String.valueOf(group.getGroupId())));
        cancelButtonLine.add(new InlineKeyboardButton().setText("Отмена").setCallbackData(CANCEL_REQUEST));
        buttons.add(groupsButtonsLine);
        buttons.add(cancelButtonLine);
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        message.setText(Callbacks.CHOOSE_GROUP_RANDOM_COMMAND);
        message.setReplyMarkup(markupKeyboard);
    }

    private void setAskChoosePostInlineKeyboardMarkup(SendMessage message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> questionButtonsLine = new ArrayList<>();
   
        questionButtonsLine.add(new InlineKeyboardButton().setText("Да").setCallbackData(MANUAL_MODE));
        questionButtonsLine.add(new InlineKeyboardButton().setText("Нет").setCallbackData(RANDOM_MODE));
        buttons.add(questionButtonsLine);
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        message.setText(Callbacks.ASK_CHOOSE_POST_RANDOM_COMMAND);
        message.setReplyMarkup(markupKeyboard);
    }

    private void sendTelegramPost(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        Photo randomPhoto = VkRandomContentFinder.findRandomPhoto();
        VkCustomAudio randomAudio = VkRandomContentFinder.findRandomAudio();
        dialog.setAudio(randomAudio);
        dialog.setPhoto(randomPhoto);
        messageIdToMessageDialogStorage.put(callbackQuery.getMessage().getMessageId(), dialog);

        sendPostChooseKeyboard(sender, callbackQuery, randomPhoto, randomAudio);
    }

    private void sendRandomVkPost(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        endMessageDialog(sender, callbackQuery);

        Photo randomPhoto = VkRandomContentFinder.findRandomPhoto();
        VkCustomAudio randomAudio = VkRandomContentFinder.findRandomAudio();
        VkGroup chosenVkGroup = dialog.getVkGroup();

        sendVkPost(sender, callbackQuery.getMessage(), randomAudio, randomPhoto, chosenVkGroup);
    }

    private void deleteHandledMessage(AbsSender sender, CallbackQuery callbackQuery) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(callbackQuery.getMessage().getChatId());
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        send(sender, deleteMessage);
    }

    private void sendNotificationAboutOldMessage(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();

        if (!isStorageContainsMessageId(messageId)) {
            EditMessageText oldMessage = new EditMessageText();
            oldMessage.setChatId(callbackQuery.getMessage().getChatId());
            oldMessage.setMessageId(callbackQuery.getMessage().getMessageId());
            oldMessage.setText("Запрос на отправку случайного поста устарел.");

            send(sender, oldMessage);
        }
    }

    private void sendPostChooseKeyboard(AbsSender sender, CallbackQuery callbackQuery, Photo randomPhoto, VkCustomAudio randomAudio) {
        EditMessageText pickedPost = new EditMessageText();
        pickedPost.setChatId(callbackQuery.getMessage().getChatId());
        pickedPost.setMessageId(callbackQuery.getMessage().getMessageId());

        StringBuilder messageBody = new StringBuilder();
        PhotoSizes bigResolutionPhoto = randomPhoto.getSizes().get(randomPhoto.getSizes().size() - 1);
        URL imageUrl = bigResolutionPhoto.getUrl();
        messageBody.append("Пикча: \n").append(imageUrl.toString())
                   .append("\n")
                   .append("Трек: \n").append(randomAudio.toPrettyString());
        pickedPost.setText(messageBody.toString());
        setPostChooseInlineKeyboard(pickedPost);

        send(sender, pickedPost);
    }

    private void setPostChooseInlineKeyboard(EditMessageText postMessage) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        setContentChooseKeyboardButtons(buttons);
        setSendPostKeyboardButton(buttons);
        setCancelKeyboardButton(buttons);
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        postMessage.setReplyMarkup(markupKeyboard);
    }

    private void setContentChooseKeyboardButtons(List<List<InlineKeyboardButton>> buttons) {
        List<InlineKeyboardButton> contentAnswersButtonsLine = new ArrayList<>();
        contentAnswersButtonsLine.add(new InlineKeyboardButton().setText("Поменять пикчу").setCallbackData(CHANGE_PHOTO));
        contentAnswersButtonsLine.add(new InlineKeyboardButton().setText("Поменять трек").setCallbackData(CHANGE_AUDIO));
        buttons.add(contentAnswersButtonsLine);
    }

    private void setSendPostKeyboardButton(List<List<InlineKeyboardButton>> buttons) {
        List<InlineKeyboardButton> sendButtonLine = new ArrayList<>();
        sendButtonLine.add(new InlineKeyboardButton().setText("Запостить").setCallbackData(SEND_CONSTRUCTED_POST));
        buttons.add(sendButtonLine);
    }

    private void setCancelKeyboardButton(List<List<InlineKeyboardButton>> buttons) {
        List<InlineKeyboardButton> cancelButtonLine = new ArrayList<>();
        cancelButtonLine.add(new InlineKeyboardButton().setText("Отменить запрос").setCallbackData(CANCEL_REQUEST));
        buttons.add(cancelButtonLine);
    }

    private void sendVkPost(AbsSender sender, Message message, VkCustomAudio audio, Photo photo, VkGroup group) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Готово, чекай группу \uD83D\uDE38\n" + group.getUrl());
        sendMessage.disableWebPagePreview();

        try {
            String photoAttachment = "photo" + photo.getOwnerId() + "_" + photo.getId();
            String audioAttachment = "audio" + audio.getOwnerId() + "_" + audio.getId();
            VkApi.instance()
                    .wall()
                    .post(VkUserActor.instance())
                    .ownerId(group.getGroupId())
                    .attachments(photoAttachment, audioAttachment)
                    .execute();
        } catch (ClientException | ApiException e) {
            sendMessage.setText("Что-то по пути сломалось...");
        }

        send(sender, sendMessage);
    }

    private boolean isStorageContainsMessageId(int messageId) {
        return messageIdToMessageDialogStorage.containsKey(messageId);
    }
}
