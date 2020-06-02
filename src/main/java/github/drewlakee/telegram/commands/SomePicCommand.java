package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.handlers.BotCommand;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import github.drewlakee.vk.domain.vkObjects.VkCustomPhoto;
import github.drewlakee.vk.services.random.VkRandomPhotoContent;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class SomePicCommand extends BotCommand implements MessageHandler {

    public static final String COMMAND_NAME = "/some_pic";

    public SomePicCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendPhoto response = new SendPhoto();
        response.setChatId(message.getChatId());
        VkCustomPhoto photo = (VkCustomPhoto) new VkRandomPhotoContent().find(1).get(0);
        response.setPhoto(photo.getLargestSize().getUrl().toString());
        ResponseMessageDispatcher.send(sender, response);
    }
}
