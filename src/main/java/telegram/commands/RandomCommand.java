package telegram.commands;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
import vk.services.VkContentFinder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RandomCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    private static ConcurrentHashMap<Integer, RandomCommandDialog> bufferedStorageMessageIdToDialogProcess = new ConcurrentHashMap<>();

    private final static String CANCEL = "0";
    private final static String OK = "1";
    private final static String GROUP = "2";
    private final static String TRY_AGAIN = "3";

    public RandomCommand() {
        super("/random");
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String handleWay = callbackQuery.getData();

        boolean isDataHaveGroup = Integer.parseInt(data) < 0;
        if (isDataHaveGroup)
            handleWay = GROUP;

        Photo randomPhoto;
        String textAnswer;
        switch (handleWay) {
            case GROUP:
                RandomCommandDialog startedDialog = new RandomCommandDialog(callbackQuery.getMessage().getMessageId());
                VkGroup chosenVkGroup = VkGroupPool.getHostGroups()
                        .stream()
                        .filter(group -> group.getGroupId() == Integer.parseInt(data))
                        .findFirst()
                        .get();
                randomPhoto = VkContentFinder.findRandomPhoto();
                startedDialog.setVkGroup(chosenVkGroup);
                startedDialog.setPhoto(randomPhoto);
                bufferedStorageMessageIdToDialogProcess.put(callbackQuery.getMessage().getMessageId(), startedDialog);
                sendPhotoChooseInlineKeyboard(sender, callbackQuery, randomPhoto);
                break;
            case OK:
                if (bufferedStorageMessageIdToDialogProcess.containsKey(callbackQuery.getMessage().getMessageId())) {
                    RandomCommandDialog endingDialog = bufferedStorageMessageIdToDialogProcess.get(callbackQuery.getMessage().getMessageId());
                    bufferedStorageMessageIdToDialogProcess.remove(callbackQuery.getMessage().getMessageId(), endingDialog);
                    VkCustomAudio randomAudio = VkContentFinder.findRandomAudio();
                    textAnswer = "Запрос на отправку поста обработан.";
                    hideReplyMarkup(sender, callbackQuery, textAnswer);
                    sendRandomVkPost(sender, callbackQuery.getMessage(), randomAudio, endingDialog);
                }
                break;
            case TRY_AGAIN:
                if (bufferedStorageMessageIdToDialogProcess.containsKey(callbackQuery.getMessage().getMessageId())) {
                    RandomCommandDialog dialogInProcess = bufferedStorageMessageIdToDialogProcess.get(callbackQuery.getMessage().getMessageId());
                    randomPhoto = VkContentFinder.findRandomPhoto();
                    dialogInProcess.setPhoto(randomPhoto);
                    sendPhotoChooseInlineKeyboard(sender, callbackQuery, randomPhoto);
                }
                break;
            case CANCEL:
                bufferedStorageMessageIdToDialogProcess.remove(callbackQuery.getMessage().getMessageId());
                textAnswer = "Запрос на отправку поста отменен.";
                hideReplyMarkup(sender, callbackQuery, textAnswer);
                break;
        }
    }

    private void sendPhotoChooseInlineKeyboard(AbsSender sender, CallbackQuery callbackQuery, Photo randomPhoto) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(callbackQuery.getMessage().getChatId());
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        URL imageUrl = randomPhoto.getSizes().get(randomPhoto.getSizes().size() - 1).getUrl();
        editMessageText.setText(imageUrl.toString());

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> answersGroup = new ArrayList<>();
        List<InlineKeyboardButton> cancelButtonsLine = new ArrayList<>();

        answersGroup.add(new InlineKeyboardButton().setText("Пойдет").setCallbackData(OK));
        answersGroup.add(new InlineKeyboardButton().setText("Не пойдет").setCallbackData(TRY_AGAIN));
        cancelButtonsLine.add(new InlineKeyboardButton().setText("Прервать").setCallbackData(CANCEL));
        buttons.add(answersGroup);
        buttons.add(cancelButtonsLine);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        editMessageText.setReplyMarkup(markupKeyboard);
        send(sender, editMessageText);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage answer = new SendMessage();
        setHostGroupsInlineKeyboardMarkup(answer);
        answer.setChatId(message.getChatId());
        answer.setText("Выбери группу, в которую хочешь пост:");
        send(sender, answer);
    }

    private void setHostGroupsInlineKeyboardMarkup(SendMessage message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> groupsButtonsLine = new ArrayList<>();
        List<InlineKeyboardButton> manageButtonsLine = new ArrayList<>();

        for (VkGroup group : VkGroupPool.getHostGroups())
            groupsButtonsLine.add(new InlineKeyboardButton().setText(group.getName()).setCallbackData(String.valueOf(group.getGroupId())));
        manageButtonsLine.add(new InlineKeyboardButton().setText("Отмена").setCallbackData(CANCEL));
        buttons.add(groupsButtonsLine);
        buttons.add(manageButtonsLine);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        message.setReplyMarkup(markupKeyboard);
    }

    private void sendRandomVkPost(AbsSender sender, Message message, VkCustomAudio randomAudio, RandomCommandDialog endedDialog) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("");
        VkGroup chosenGroup = endedDialog.getVkGroup();
        Photo chosenPhoto = endedDialog.getPhoto();


        try {
            String photoAttachment = "photo" + chosenPhoto.getOwnerId() + "_" + chosenPhoto.getId();
            String audioAttachment = "audio" + randomAudio.getOwnerId() + "_" + randomAudio.getId();
            VkApi.instance()
                    .wall()
                    .post(VkUserActor.instance())
                    .ownerId(chosenGroup.getGroupId())
                    .attachments(photoAttachment, audioAttachment)
                    .execute();
        } catch (ClientException | ApiException e) {
            sendMessage.setText("Что-то по пути сломалось...");
        }

        boolean isOk = sendMessage.getText().isEmpty();
        if (isOk)
            sendMessage.setText("Готово, чекай группу \uD83D\uDE38\n" + chosenGroup.getUrl());
        send(sender, sendMessage);
    }

    private void hideReplyMarkup(AbsSender sender, CallbackQuery callbackQuery, String textAnswer) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(callbackQuery.getMessage().getChatId());
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageText.setText(textAnswer);
        editMessageText.setReplyMarkup(null);
        send(sender, editMessageText);
    }
}
