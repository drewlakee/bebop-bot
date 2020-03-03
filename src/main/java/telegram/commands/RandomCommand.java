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
import telegram.commands.abstractions.BotCommand;
import telegram.commands.abstractions.CallbackQueryHandler;
import telegram.commands.abstractions.EditContentHandler;
import telegram.commands.abstractions.MessageHandler;
import vk.api.VkApi;
import vk.api.VkUserActor;
import vk.domain.groups.VkGroup;
import vk.domain.groups.VkGroupPool;
import vk.domain.vkObjects.VkCustomAudio;
import vk.services.VkContentFinder;

import java.util.ArrayList;
import java.util.List;

public class RandomCommand extends BotCommand implements CallbackQueryHandler, MessageHandler, EditContentHandler {

    public RandomCommand() {
        super("/random");
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        List<VkGroup> groups = VkGroupPool.getHostGroups();

        boolean isDataHaveGroup = groups.stream().anyMatch(group -> group.getUrl().equals(data));
        if (isDataHaveGroup) {
            VkGroup vkGroup = groups.stream().filter(group -> group.getUrl().equals(data)).findFirst().get();
            sendRandomVkPost(sender, callbackQuery.getMessage(), vkGroup);
        } else if (data.equals("exit")) {
            sendCancel(sender, callbackQuery);
        }
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage answer = new SendMessage();
        setHostGroupsInlineKeyboardMarkup(answer);
        answer.setChatId(message.getChatId());
        answer.setText("Выбери группу, в которую хочешь пост:");

        sendAnswerMessage(sender, answer);
    }

    private void setHostGroupsInlineKeyboardMarkup(SendMessage message) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> groupsButtonsLine = new ArrayList<>();
        List<InlineKeyboardButton> manageButtonsLine = new ArrayList<>();

        for (VkGroup group : VkGroupPool.getHostGroups())
            groupsButtonsLine.add(new InlineKeyboardButton().setText(group.getName()).setCallbackData(group.getUrl()));
        manageButtonsLine.add(new InlineKeyboardButton().setText("Отмена").setCallbackData("exit"));
        buttons.add(groupsButtonsLine);
        buttons.add(manageButtonsLine);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        message.setReplyMarkup(markupKeyboard);
    }

    private void sendRandomVkPost(AbsSender sender, Message message, VkGroup group) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("");

        try {
            Photo randomPhoto = VkContentFinder.findRandomPhoto();
            VkCustomAudio randomAudio = VkContentFinder.findRandomAudio();
            String photoAttachment = "photo" + randomPhoto.getOwnerId() + "_" + randomPhoto.getId();
            String audioAttachment = "audio" + randomAudio.getOwnerId() + "_" + randomAudio.getId();
            VkApi.instance()
                    .wall()
                    .post(VkUserActor.instance())
                    .ownerId(group.getGroupId())
                    .attachments(photoAttachment, audioAttachment)
                    .execute();
        } catch (ClientException | ApiException e) {
            sendMessage.setText("Что-то по пути сломалось...");
        }

        boolean isOk = sendMessage.getText().isEmpty();
        if (isOk)
            sendMessage.setText("Готово, чекай группу \uD83D\uDE38\n" + group.getUrl());
        sendAnswerMessage(sender, sendMessage);
    }

    private void sendCancel(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(callbackQuery.getMessage().getChatId());
        editMessageText.setInlineMessageId(callbackQuery.getInlineMessageId());
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageText.setText("Запрос на отправку поста отменен.");
        editMessageText.setReplyMarkup(null);

        sendEditContent(sender, editMessageText);
    }
}
