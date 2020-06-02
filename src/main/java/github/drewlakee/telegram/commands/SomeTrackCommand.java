package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.handlers.BotCommand;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import github.drewlakee.vk.domain.vkObjects.VkCustomAudio;
import github.drewlakee.vk.services.random.VkRandomAudioContent;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class SomeTrackCommand extends BotCommand implements MessageHandler {

    public static final String COMMAND_NAME = "/some_track";

    public SomeTrackCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        VkCustomAudio audio = (VkCustomAudio) new VkRandomAudioContent().find(1).get(0);
        response.setText(audio.toPrettyString());
        ResponseMessageDispatcher.send(sender, response);
    }
}
