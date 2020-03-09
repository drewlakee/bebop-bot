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
import java.util.concurrent.ConcurrentHashMap;

public class RandomCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    @GuardedBy("this")
    private final HashMap<Integer, RandomCommandDialog> bufferedStorageMessageIdToMessageDialogState;

    private final static String CANCEL = "0";
    private final static String PHOTO_OK = "1";
    private final static String GROUP_IS_CHOSEN = "2";
    private final static String PHOTO_TRY_AGAIN = "3";
    private final static String DIDNT_WANT_CHOOSE_PHOTO = "4";
    private final static String WANT_CHOOSE_PHOTO = "5";

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
            case PHOTO_OK:
                handlePositiveAnswerPhotoChoose(sender, callbackQuery);
                break;
            case PHOTO_TRY_AGAIN:
                handleNegativeAnswerPhotoChoose(sender, callbackQuery);
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
        setPhotoAskInlineKeyboardMarkup(answer);
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

                if (dialog.getPhotoChooseAnswer().equals(WANT_CHOOSE_PHOTO)) {
                    sendPhoto(sender, callbackQuery, dialog);
                } else
                    sendRandomPost(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void handlePositiveAnswerPhotoChoose(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = bufferedStorageMessageIdToMessageDialogState.get(messageId);
                endMessageDialog(sender, callbackQuery);

                VkCustomAudio randomAudio = VkRandomContentFinder.findRandomAudio();
                VkGroup chosenGroup = dialog.getVkGroup();
                Photo chosenPhoto = dialog.getPhoto();

                sendVkPost(sender, callbackQuery.getMessage(), randomAudio, chosenPhoto, chosenGroup);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void handleNegativeAnswerPhotoChoose(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = bufferedStorageMessageIdToMessageDialogState.get(messageId);
                sendPhoto(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private synchronized void endMessageDialog(AbsSender sender, CallbackQuery callbackQuery) {
        bufferedStorageMessageIdToMessageDialogState.remove(callbackQuery.getMessage().getMessageId());
        deleteHandledMessage(sender, callbackQuery);
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

    private void setPhotoAskInlineKeyboardMarkup(SendMessage message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> questionButtonsLine = new ArrayList<>();

        questionButtonsLine.add(new InlineKeyboardButton().setText("Да").setCallbackData(WANT_CHOOSE_PHOTO));
        questionButtonsLine.add(new InlineKeyboardButton().setText("Нет").setCallbackData(DIDNT_WANT_CHOOSE_PHOTO));
        buttons.add(questionButtonsLine);
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        message.setText(Callbacks.ASK_CHOOSE_PHOTO);
        message.setReplyMarkup(markupKeyboard);
    }

    private void sendPhoto(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        Photo randomPhoto = VkRandomContentFinder.findRandomPhoto();
        dialog.setPhoto(randomPhoto);
        bufferedStorageMessageIdToMessageDialogState.put(callbackQuery.getMessage().getMessageId(), dialog);

        sendPhotoChooseKeyboard(sender, callbackQuery, randomPhoto);
    }

    private void sendRandomPost(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
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

    private void sendPhotoChooseKeyboard(AbsSender sender, CallbackQuery callbackQuery, Photo randomPhoto) {
        EditMessageText newPhotoMessage = new EditMessageText();
        newPhotoMessage.setChatId(callbackQuery.getMessage().getChatId());
        newPhotoMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        PhotoSizes bigResolutionPhoto = randomPhoto.getSizes().get(randomPhoto.getSizes().size() - 1);
        URL imageUrl = bigResolutionPhoto.getUrl();
        newPhotoMessage.setText(imageUrl.toString());
        setPhotoChooseInlineKeyboard(newPhotoMessage);

        send(sender, newPhotoMessage);
    }

    private void setPhotoChooseInlineKeyboard(EditMessageText newPhotoMessage) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> answersButtonsLine = new ArrayList<>();
        List<InlineKeyboardButton> cancelButtonLine = new ArrayList<>();

        answersButtonsLine.add(new InlineKeyboardButton().setText("Пойдет").setCallbackData(PHOTO_OK));
        answersButtonsLine.add(new InlineKeyboardButton().setText("Не пойдет").setCallbackData(PHOTO_TRY_AGAIN));
        cancelButtonLine.add(new InlineKeyboardButton().setText("Прервать").setCallbackData(CANCEL));
        buttons.add(answersButtonsLine);
        buttons.add(cancelButtonLine);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        newPhotoMessage.setReplyMarkup(markupKeyboard);
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
