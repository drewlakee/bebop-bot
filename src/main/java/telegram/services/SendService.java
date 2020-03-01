package telegram.services;

import app.Environment;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import telegram.TelegramBot;
import vk.api.VkApi;
import vk.api.VkUserActor;
import vk.domain.vkObjects.VkCustomAudio;
import vk.services.VkContentFinder;

public class SendService {

    public static void sendBotStatus(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Еее емо роккк \uD83D\uDE3B");
        TelegramBot.instance().sendMessage(sendMessage);
    }

    public static void sendRandomVkPost(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());

        try {
            Photo randomPhoto = VkContentFinder.findRandomPhoto();
            VkCustomAudio randomAudio = VkContentFinder.findRandomAudio();
            String photoAttachment = "photo" + randomPhoto.getOwnerId() + "_" + randomPhoto.getId();
            String audioAttachment = "audio" + randomAudio.getOwnerId() + "_" + randomAudio.getId();
            VkApi.instance().wall().post(VkUserActor.instance()).ownerId(Integer.parseInt(Environment.PROPERTIES.get("my_public").toString())).attachments(photoAttachment, audioAttachment).execute();
        } catch (ClientException | ApiException e) {
            e.printStackTrace();
        }

        sendMessage.setText("Готово, чекай группу \uD83D\uDE38");
        TelegramBot.instance().sendMessage(sendMessage);
    }
}
