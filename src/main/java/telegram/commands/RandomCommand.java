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
import telegram.commands.keyboards.InlineKeyboardBuilder;
import telegram.commands.statics.Callbacks;
import telegram.commands.statics.Commands;
import telegram.utils.MessageKeysParser;
import vk.api.VkApi;
import vk.api.VkUserActor;
import vk.domain.groups.VkGroup;
import vk.domain.groups.VkGroupPool;
import vk.domain.vkObjects.VkCustomAudio;
import vk.services.VkRandomContentFinder;

import javax.annotation.concurrent.GuardedBy;
import java.net.URL;
import java.util.HashMap;

public class RandomCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    @GuardedBy("this")
    private final HashMap<Integer, RandomCommandDialog> messageIdToMessageDialogStorage;

    private final static String CANCEL_REQUEST = "0";
    private final static String SEND_CONSTRUCTED_POST = "1";
    private final static String GROUP_CALLBACK = "2";
    private final static String CHANGE_PHOTO = "3";
    private final static String CHANGE_AUDIO = "4";
    private final static String RANDOM_MODE = "5";
    private final static String MANUAL_MODE = "6";

    public RandomCommand() {
        super(Commands.RANDOM);
        messageIdToMessageDialogStorage = new HashMap<>();
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
                deleteMessage(sender, callbackQuery);
                break;
            default:
                pickMode(sender, callbackQuery);
                break;
        }
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage answer = new SendMessage();
        setAskModeKeyboard(answer);
        answer.setChatId(message.getChatId());

        send(sender, answer);
    }

    private void handleChosenMode(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        HashMap<String, String> params = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());

    }

    private void changePhoto(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainsMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = messageIdToMessageDialogStorage.get(messageId);
                Photo anotherPhoto = VkRandomContentFinder.findRandomPhoto();
                dialog.setPhoto(anotherPhoto);
                sendTelegramPost(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void changeAudio(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainsMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = messageIdToMessageDialogStorage.get(messageId);
                VkCustomAudio anotherAudio = VkRandomContentFinder.findRandomAudio();
                dialog.setAudio(anotherAudio);
                sendTelegramPost(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void sendConstructedPost(AbsSender sender, CallbackQuery callbackQuery) {
        int messageId = callbackQuery.getMessage().getMessageId();
        boolean isContain;

        synchronized (this) {
            isContain = isStorageContainsMessageId(messageId);
            if (isContain) {
                RandomCommandDialog dialog = messageIdToMessageDialogStorage.get(messageId);
                sendVkPost(sender, callbackQuery, dialog);
            }
        }

        if (!isContain)
            sendNotificationAboutOldMessage(sender, callbackQuery);
    }

    private void pickMode(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageText groupChooseMessage = new EditMessageText();
        StringBuilder messageBody = new StringBuilder();

        if (callbackQuery.getData().equals(MANUAL_MODE))
            messageBody.append("Режим: ручной");
        else
            messageBody.append("Режим: рандом");
        messageBody.append("\n\n");
        messageBody.append(Callbacks.CHOOSE_GROUP_RANDOM_COMMAND);

        groupChooseMessage.setChatId(callbackQuery.getMessage().getChatId());
        groupChooseMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        setHostGroupsKeyboard(groupChooseMessage);

        send(sender, groupChooseMessage);
    }

    private void setHostGroupsKeyboard(EditMessageText message) {
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();

        for (VkGroup group : VkGroupPool.getHostGroups())
            keyboardBuilder
                    .addButton(new InlineKeyboardButton().setText(group.getName()).setCallbackData(String.valueOf(group.getGroupId())))
                    .nextLine();
        keyboardBuilder.addButton(new InlineKeyboardButton().setText("Отмена").setCallbackData(CANCEL_REQUEST));

        InlineKeyboardMarkup keyboardMarkup = keyboardBuilder.build();
        message.setReplyMarkup(keyboardMarkup);
        message.setText(Callbacks.CHOOSE_GROUP_RANDOM_COMMAND);
    }

    private void setAskModeKeyboard(SendMessage message) {
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Да").setCallbackData(MANUAL_MODE))
                .addButton(new InlineKeyboardButton().setText("Нет").setCallbackData(RANDOM_MODE))
                .build();

        message.setText(Callbacks.ASK_CHOOSE_POST_RANDOM_COMMAND);
        message.setReplyMarkup(markupKeyboard);
    }

    private void constructRandomPost(CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        Photo randomPhoto = VkRandomContentFinder.findRandomPhoto();
        VkCustomAudio randomAudio = VkRandomContentFinder.findRandomAudio();
        dialog.setAudio(randomAudio);
        dialog.setPhoto(randomPhoto);
        messageIdToMessageDialogStorage.put(callbackQuery.getMessage().getMessageId(), dialog);
    }

    private void setPostConstructKeyboard(EditMessageText postMessage) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton().setText("Поменять пикчу").setCallbackData(CHANGE_PHOTO))
                .addButton(new InlineKeyboardButton().setText("Поменять трек").setCallbackData(CHANGE_AUDIO))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Запостить").setCallbackData(SEND_CONSTRUCTED_POST))
                .nextLine()
                .addButton(new InlineKeyboardButton().setText("Отменить запрос").setCallbackData(CANCEL_REQUEST))
                .build();

        postMessage.setReplyMarkup(keyboardMarkup);
    }

    private void deleteMessage(AbsSender sender, CallbackQuery callbackQuery) {
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

    private void sendTelegramPost(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        EditMessageText constructedPost = new EditMessageText();
        constructedPost.setChatId(callbackQuery.getMessage().getChatId());
        constructedPost.setMessageId(callbackQuery.getMessage().getMessageId());

        StringBuilder messageBody = new StringBuilder();
        PhotoSizes bigResolutionPhoto = dialog.getPhoto().getSizes().get(dialog.getPhoto().getSizes().size() - 1);
        URL imageUrl = bigResolutionPhoto.getUrl();
        messageBody.append("Пикча: \n").append(imageUrl.toString())
                .append("\n")
                .append("Трек: \n").append(dialog.getAudio().toPrettyString());
        constructedPost.setText(messageBody.toString());
        setPostConstructKeyboard(constructedPost);

        send(sender, constructedPost);
    }

    private void sendVkPost(AbsSender sender, CallbackQuery callbackQuery, RandomCommandDialog dialog) {
        endMessageDialog(sender, callbackQuery);

        SendMessage sendMessage = new SendMessage();
        VkGroup group = dialog.getVkGroup();
        Photo photo = dialog.getPhoto();
        VkCustomAudio audio = dialog.getAudio();

        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
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

    private void endMessageDialog(AbsSender sender, CallbackQuery callbackQuery) {
        synchronized (this) {
            messageIdToMessageDialogStorage.remove(callbackQuery.getMessage().getMessageId());
            deleteMessage(sender, callbackQuery);
        }
    }

    private boolean isStorageContainsMessageId(int messageId) {
        return messageIdToMessageDialogStorage.containsKey(messageId);
    }
}
