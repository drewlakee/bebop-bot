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
    private final HashMap<Integer, RandomCommandDialog> bufferedStorageMessageIdToMessageDialogState;

    private final static String CANCEL = "0";
    private final static String SEND_POST = "1";
    private final static String GROUP_IS_CHOSEN = "2";
    private final static String PHOTO_TRY_AGAIN = "3";
    private final static String AUDIO_TRY_AGAIN = "4";
    private final static String DIDNT_WANT_CHOOSE_POST = "5";
    private final static String WANT_CHOOSE_POST = "6";

    public RandomCommand() {
        super(Commands.RANDOM);

        bufferedStorageMessageIdToMessageDialogState = new HashMap<>();
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String handleWay = callbackQuery.getData();

        boolean isDataHaveGroup = Integer.parseInt(data) < 0;
        if (isDataHaveGroup)
            handleWay = GROUP_IS_CHOSEN;

        switch (handleWay) {
            case GROUP_IS_CHOSEN:
                handleRequestMessageDialog(sender, callbackQuery, data);
                break;
            case PHOTO_TRY_AGAIN:
                pickAnotherPhoto(sender, callbackQuery);
                break;
            case CANCEL:
                endMessageDialog(sender, callbackQuery);
                break;
            default:
                startMessageDialog(sender, callbackQuery, data);
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

    private void handleRequestMessageDialog(AbsSender sender, CallbackQuery callbackQuery, String data) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = bufferedStorageMessageIdToMessageDialogState.get(messageId);
                VkGroup chosenVkGroup = VkGroupPool.getHostGroup(Integer.parseInt(data));
                if (!dialog.hasVkGroup())
                    dialog.setVkGroup(chosenVkGroup);

                if (dialog.getPhotoChooseAnswer().equals(WANT_CHOOSE_POST)) {
                    sendPickedTelegramPost(sender, callbackQuery, dialog);
                } else
                    sendRandomVkPost(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void pickAnotherPhoto(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = bufferedStorageMessageIdToMessageDialogState.get(messageId);
                sendAnotherPhoto(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void sendAnotherPhoto(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        Photo anotherPhoto = VkRandomContentFinder.findRandomPhoto();
        sendPostChooseKeyboard(sender, callbackQuery, anotherPhoto, dialog.getAudio());
    }

    private void startMessageDialog(AbsSender sender, CallbackQuery callbackQuery, String data) {
        int messageId = callbackQuery.getMessage().getMessageId();

        synchronized (this) {
            if (!isStorageContainMessageId(messageId)) {
                RandomCommandDialog dialog = new RandomCommandDialog(messageId);
                if (!dialog.hasPhotoChooseAnswer())
                    dialog.setPhotoChooseAnswer(data);
                bufferedStorageMessageIdToMessageDialogState.put(messageId, dialog);
            }
        }

        EditMessageText groupChooseMessage = new EditMessageText();
        groupChooseMessage.setText(Callbacks.CHOOSE_GROUP);
        groupChooseMessage.setChatId(callbackQuery.getMessage().getChatId());
        groupChooseMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        setHostGroupsInlineKeyboardMarkup(groupChooseMessage);

        send(sender, groupChooseMessage);
    }

    private synchronized void endMessageDialog(AbsSender sender, CallbackQuery callbackQuery) {
        bufferedStorageMessageIdToMessageDialogState.remove(callbackQuery.getMessage().getMessageId());
        deleteHandledMessage(sender, callbackQuery);
    }

    private void setHostGroupsInlineKeyboardMarkup(EditMessageText message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> groupsButtonsLine = new ArrayList<>();
        List<InlineKeyboardButton> cancelButtonLine = new ArrayList<>();

        for (VkGroup group : VkGroupPool.getHostGroups())
            groupsButtonsLine.add(new InlineKeyboardButton().setText(group.getName()).setCallbackData(String.valueOf(group.getGroupId())));
        cancelButtonLine.add(new InlineKeyboardButton().setText("Отмена").setCallbackData(CANCEL));
        buttons.add(groupsButtonsLine);
        buttons.add(cancelButtonLine);
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        message.setText(Callbacks.CHOOSE_GROUP);
        message.setReplyMarkup(markupKeyboard);
    }

    private void setAskChoosePostInlineKeyboardMarkup(SendMessage message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> questionButtonsLine = new ArrayList<>();

        questionButtonsLine.add(new InlineKeyboardButton().setText("Да").setCallbackData(WANT_CHOOSE_POST));
        questionButtonsLine.add(new InlineKeyboardButton().setText("Нет").setCallbackData(DIDNT_WANT_CHOOSE_POST));
        buttons.add(questionButtonsLine);
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        message.setText(Callbacks.ASK_CHOOSE_POST);
        message.setReplyMarkup(markupKeyboard);
    }

    private void sendPickedTelegramPost(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        Photo randomPhoto = VkRandomContentFinder.findRandomPhoto();
        VkCustomAudio randomAudio = VkRandomContentFinder.findRandomAudio();
        dialog.setAudio(randomAudio);
        dialog.setPhoto(randomPhoto);
        bufferedStorageMessageIdToMessageDialogState.put(callbackQuery.getMessage().getMessageId(), dialog);

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

        if (!isStorageContainMessageId(messageId)) {
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
        messageBody.append("Пикча: ").append(imageUrl.toString())
                   .append("\n")
                   .append("Трек: ").append(randomAudio.toPrettyString());
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
        contentAnswersButtonsLine.add(new InlineKeyboardButton().setText("Поменять пикчу").setCallbackData(PHOTO_TRY_AGAIN));
        contentAnswersButtonsLine.add(new InlineKeyboardButton().setText("Поменять трек").setCallbackData(AUDIO_TRY_AGAIN));
        buttons.add(contentAnswersButtonsLine);
    }

    private void setSendPostKeyboardButton(List<List<InlineKeyboardButton>> buttons) {
        List<InlineKeyboardButton> sendButtonLine = new ArrayList<>();
        sendButtonLine.add(new InlineKeyboardButton().setText("Запостить").setCallbackData(CANCEL));
        buttons.add(sendButtonLine);
    }

    private void setCancelKeyboardButton(List<List<InlineKeyboardButton>> buttons) {
        List<InlineKeyboardButton> cancelButtonLine = new ArrayList<>();
        cancelButtonLine.add(new InlineKeyboardButton().setText("Отменить запрос").setCallbackData(CANCEL));
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

    private boolean isStorageContainMessageId(int messageId) {
        return bufferedStorageMessageIdToMessageDialogState.containsKey(messageId);
    }
}
